package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.service.impl.PayNotifyServiceImpl;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.PaymentNotifyLog;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.domain.repository.PaymentNotifyLogRepository;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.PaymentGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayNotifyServiceTest {

    private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final PaymentNotifyLogRepository notifyLogRepository = mock(PaymentNotifyLogRepository.class);
    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final TaxItemRepository taxItemRepository = mock(TaxItemRepository.class);
    private final RedisStreamMessagePublisher publisher = mock(RedisStreamMessagePublisher.class);
    private final BiCacheService biCacheService = mock(BiCacheService.class);
    private final PiiProperties properties = new PiiProperties();
    private final PayNotifyService service = new PayNotifyServiceImpl(paymentGateway, payOrderRepository,
            notifyLogRepository, merchantProfileRepository, taxItemRepository, publisher, properties, biCacheService);

    @Test
    void notifyShouldVerifyPersistUpdateOrderAndPublishInvoiceMessage() {
        properties.getInvoice().setNotifyUrl("https://notify.example.com/pii/invoice/notify");
        when(payOrderRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.of(order()));
        when(notifyLogRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.empty());
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        when(taxItemRepository.findById(20L)).thenReturn(Optional.of(taxItem()));
        when(paymentGateway.verifyAndParse(rawBody(), "PAY_KEY")).thenReturn(validVerifyResult());

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("SUCCESS");
        verify(paymentGateway).verifyAndParse(rawBody(), "PAY_KEY");
        verify(payOrderRepository).updatePayStatus(eq(10L), eq("PAID"), eq("TRADE001"), any(LocalDateTime.class));
        verify(notifyLogRepository).markProcessed("ORDER001");
        verify(biCacheService).evictAll();

        ArgumentCaptor<PaymentNotifyLog> logCaptor = ArgumentCaptor.forClass(PaymentNotifyLog.class);
        verify(notifyLogRepository).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getOutTradeNo()).isEqualTo("ORDER001");
        assertThat(logCaptor.getValue().getSign()).isEqualTo("SIGN");
        assertThat(logCaptor.getValue().getVerifyResult()).isEqualTo(1);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(publisher).publish(eq("pii:invoice:open"), eq("pii.invoice.open"), eq("ORDER001"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue())
                .contains("\"payOrderId\":10")
                .contains("\"merchantId\":\"MID\"")
                .contains("\"merOrderId\":\"ORDER001\"")
                .contains("\"buyerName\":\"张三\"")
                .contains("\"goodsDetail\"");
    }

    @Test
    void notifyShouldReturnSuccessWithoutProcessingWhenAlreadyLogged() {
        when(payOrderRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.of(order()));
        when(notifyLogRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.of(new PaymentNotifyLog()));

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("SUCCESS");
        verify(paymentGateway, never()).verifyAndParse(any(), any());
        verify(payOrderRepository, never()).updatePayStatus(any(), any(), any(), any());
    }

    @Test
    void notifyShouldReturnFailWhenSignatureInvalid() {
        when(payOrderRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.of(order()));
        when(notifyLogRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.empty());
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        NotifyVerifyResult verifyResult = new NotifyVerifyResult();
        verifyResult.setValid(false);
        when(paymentGateway.verifyAndParse(rawBody(), "PAY_KEY")).thenReturn(verifyResult);

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("FAIL");
        verify(payOrderRepository, never()).updatePayStatus(any(), any(), any(), any());
        verify(publisher, never()).publish(any(), any(), any(), any());
    }

    private String rawBody() {
        return "{\"outTradeNo\":\"ORDER001\",\"tradeStatus\":\"SUCCESS\",\"tradeNo\":\"TRADE001\"}";
    }

    private NotifyVerifyResult validVerifyResult() {
        NotifyPayload payload = new NotifyPayload();
        payload.setOutTradeNo("ORDER001");
        payload.setTradeNo("TRADE001");
        payload.setTradeStatus("SUCCESS");
        NotifyVerifyResult result = new NotifyVerifyResult();
        result.setValid(true);
        result.setPayload(payload);
        return result;
    }

    private PayOrder order() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setMerchantId(100L);
        order.setTaxItemId(20L);
        order.setOutTradeNo("ORDER001");
        order.setUmsMerOrderDate("20260703");
        order.setAmount(8800L);
        order.setBuyerName("张三");
        order.setBuyerTaxCode("91330000X");
        order.setBuyerEmail("buyer@example.com");
        order.setBuyerMobile("13800000000");
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

    private TaxItem taxItem() {
        TaxItem taxItem = new TaxItem();
        taxItem.setId(20L);
        taxItem.setName("餐饮服务");
        taxItem.setTaxItemCode("3070401000000000000");
        taxItem.setTaxRate(new BigDecimal("6.00"));
        return taxItem;
    }
}
