package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.CreateRefundCommand;
import com.manzhushaka.biz.pii.application.result.RefundResult;
import com.manzhushaka.biz.pii.application.service.impl.RefundServiceImpl;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.RefundRecord;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.domain.repository.RefundRecordRepository;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.PaymentGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundResponse;
import com.manzhushaka.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefundServiceTest {
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final RefundRecordRepository refundRecordRepository = mock(RefundRecordRepository.class);
    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
    private final PiiProperties properties = new PiiProperties();
    private final RefundService service = new RefundServiceImpl(
            payOrderRepository, refundRecordRepository, merchantProfileRepository, paymentGateway, properties);

    @Test
    void refundExceedOrderAmountShouldThrow() {
        PayOrder order = paidOrder();
        order.setRefundAmount(5000L);
        when(payOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.create(command(4000L, 100L)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("退款金额超限");
    }

    @Test
    void refundOrderNotPaidShouldThrow() {
        PayOrder order = paidOrder();
        order.setPayStatus("PENDING");
        when(payOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.create(command(1000L, 100L)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("订单状态不允许退款");
    }

    @Test
    void refundMerchantMismatchShouldThrow() {
        when(payOrderRepository.findById(10L)).thenReturn(Optional.of(paidOrder()));

        assertThatThrownBy(() -> service.create(command(1000L, 200L)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("商户不匹配");
    }

    @Test
    void refundValidShouldCreateRecordAndCallGateway() {
        properties.getWechat().setAppId("wx-app");
        properties.getWechat().setAppKey("wx-key");
        properties.getPay().setInstMid("APPDEFAULT");
        when(payOrderRepository.findById(10L)).thenReturn(Optional.of(paidOrder()));
        when(refundRecordRepository.insert(any(RefundRecord.class))).thenReturn(77L);
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        when(paymentGateway.refund(any(RefundRequest.class))).thenReturn(refundResponse());

        RefundResult result = service.create(command(1000L, 100L));

        assertThat(result.getId()).isEqualTo(77L);
        assertThat(result.getOutRefundNo()).startsWith("PIR");
        assertThat(result.getStatus()).isEqualTo("PENDING");

        ArgumentCaptor<RefundRecord> recordCaptor = ArgumentCaptor.forClass(RefundRecord.class);
        verify(refundRecordRepository).insert(recordCaptor.capture());
        RefundRecord record = recordCaptor.getValue();
        assertThat(record.getMerchantId()).isEqualTo(100L);
        assertThat(record.getPayOrderId()).isEqualTo(10L);
        assertThat(record.getAmount()).isEqualTo(1000L);
        assertThat(record.getReason()).isEqualTo("用户申请退款");
        assertThat(record.getOperatorId()).isEqualTo(888L);
        assertThat(record.getTriggerInvoiceReverse()).isEqualTo(1);
        assertThat(record.getStatus()).isEqualTo("PENDING");
        assertThat(record.getCreateTime()).isNotNull();

        ArgumentCaptor<RefundRequest> requestCaptor = ArgumentCaptor.forClass(RefundRequest.class);
        verify(paymentGateway).refund(requestCaptor.capture());
        RefundRequest request = requestCaptor.getValue();
        assertThat(request.getAppId()).isEqualTo("wx-app");
        assertThat(request.getAppKey()).isEqualTo("wx-key");
        assertThat(request.getMerchantId()).isEqualTo("MID");
        assertThat(request.getTerminalId()).isEqualTo("TID");
        assertThat(request.getOutTradeNo()).isEqualTo("ORDER001");
        assertThat(request.getRefundOrderId()).isEqualTo(result.getOutRefundNo());
        assertThat(request.getRefundAmount()).isEqualTo(1000L);
        assertThat(request.getRefundDesc()).isEqualTo("用户申请退款");
        assertThat(request.getInstMid()).isEqualTo("APPDEFAULT");
    }

    private CreateRefundCommand command(Long amount, Long merchantId) {
        return new CreateRefundCommand(merchantId, 10L, amount, "用户申请退款", 888L);
    }

    private PayOrder paidOrder() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setMerchantId(100L);
        order.setOutTradeNo("ORDER001");
        order.setAmount(8800L);
        order.setRefundAmount(0L);
        order.setPayStatus("PAID");
        order.setPayTime(LocalDateTime.of(2026, 7, 3, 12, 0));
        return order;
    }

    private MerchantProfile merchant() {
        MerchantProfile merchant = new MerchantProfile();
        merchant.setId(100L);
        merchant.setUmsMerchantId("MID");
        merchant.setUmsTerminalId("TID");
        return merchant;
    }

    private RefundResponse refundResponse() {
        RefundResponse response = new RefundResponse();
        response.setRefundOrderId("REFUND001");
        response.setRefundStatus("PROCESSING");
        return response;
    }
}
