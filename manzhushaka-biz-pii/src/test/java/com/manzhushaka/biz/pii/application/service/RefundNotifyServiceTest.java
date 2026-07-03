package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.service.impl.RefundNotifyServiceImpl;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.RefundRecord;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.domain.repository.RefundRecordRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.PaymentGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefundNotifyServiceTest {
    private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
    private final RefundRecordRepository refundRecordRepository = mock(RefundRecordRepository.class);
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final RedisStreamMessagePublisher publisher = mock(RedisStreamMessagePublisher.class);
    private final RefundNotifyService service = new RefundNotifyServiceImpl(
            paymentGateway, refundRecordRepository, payOrderRepository, merchantProfileRepository, publisher);

    @Test
    void notifyShouldUpdateRefundOrderAndPublishInvoiceReverseMessage() {
        when(refundRecordRepository.findByOutRefundNo("REFUND001")).thenReturn(Optional.of(refund()));
        when(payOrderRepository.findById(10L)).thenReturn(Optional.of(order()));
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        when(paymentGateway.verifyAndParse(rawBody(), "PAY_KEY")).thenReturn(validResult());

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("SUCCESS");
        verify(refundRecordRepository).updateStatus(eq(20L), eq("SUCCESS"), eq("TRADE001"), any(LocalDateTime.class));
        verify(payOrderRepository).updateRefundAmountAndStatus(10L, 8800L, "REFUNDED");

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(publisher).publish(eq("pii:invoice:reverse"), eq("pii.invoice.reverse"), eq("ORDER001"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue())
                .contains("\"payOrderId\":10")
                .contains("\"merOrderId\":\"ORDER001\"")
                .contains("\"invoiceNo\":\"INV001\"")
                .contains("\"reason\":\"用户申请退款\"");
    }

    @Test
    void notifyShouldReturnSuccessWhenAlreadySuccessful() {
        RefundRecord refund = refund();
        refund.setStatus("SUCCESS");
        when(refundRecordRepository.findByOutRefundNo("REFUND001")).thenReturn(Optional.of(refund));

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("SUCCESS");
        verify(paymentGateway, never()).verifyAndParse(any(), any());
    }

    @Test
    void notifyShouldReturnFailWhenInvalid() {
        when(refundRecordRepository.findByOutRefundNo("REFUND001")).thenReturn(Optional.of(refund()));
        when(payOrderRepository.findById(10L)).thenReturn(Optional.of(order()));
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        NotifyVerifyResult invalid = new NotifyVerifyResult();
        invalid.setValid(false);
        when(paymentGateway.verifyAndParse(rawBody(), "PAY_KEY")).thenReturn(invalid);

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("FAIL");
        verify(refundRecordRepository, never()).updateStatus(any(), any(), any(), any());
        verify(payOrderRepository, never()).updateRefundAmountAndStatus(any(), any(), any());
    }

    private String rawBody() {
        return "{\"refundOrderId\":\"REFUND001\",\"tradeStatus\":\"SUCCESS\",\"tradeNo\":\"TRADE001\",\"refundAmount\":\"8800\"}";
    }

    private NotifyVerifyResult validResult() {
        NotifyPayload payload = new NotifyPayload();
        payload.setRefundOrderId("REFUND001");
        payload.setTradeStatus("SUCCESS");
        payload.setTradeNo("TRADE001");
        payload.setRefundAmount(8800L);
        NotifyVerifyResult result = new NotifyVerifyResult();
        result.setValid(true);
        result.setPayload(payload);
        return result;
    }

    private RefundRecord refund() {
        RefundRecord refund = new RefundRecord();
        refund.setId(20L);
        refund.setMerchantId(100L);
        refund.setPayOrderId(10L);
        refund.setOutRefundNo("REFUND001");
        refund.setAmount(8800L);
        refund.setReason("用户申请退款");
        refund.setStatus("PENDING");
        refund.setTriggerInvoiceReverse(1);
        return refund;
    }

    private PayOrder order() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setMerchantId(100L);
        order.setOutTradeNo("ORDER001");
        order.setUmsMerOrderDate("20260703");
        order.setAmount(8800L);
        order.setRefundAmount(0L);
        order.setInvoiceStatus("ISSUED");
        order.setInvoiceNo("INV001");
        order.setInvoiceCode("CODE001");
        return order;
    }

    private MerchantProfile merchant() {
        MerchantProfile merchant = new MerchantProfile();
        merchant.setId(100L);
        merchant.setUmsMerchantId("MID");
        merchant.setUmsTerminalId("TID");
        merchant.setUmsPaySignKeyEnc("PAY_KEY");
        merchant.setUmsInvoiceSignKeyEnc("INV_KEY");
        merchant.setInvoiceMsgSrc("MSG_SRC");
        return merchant;
    }
}
