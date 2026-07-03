package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.service.impl.InvoiceNotifyServiceImpl;
import com.manzhushaka.biz.pii.domain.model.InvoiceNotifyLog;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.InvoiceNotifyLogRepository;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyVerifyResult;
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

class InvoiceNotifyServiceTest {
    private final InvoiceGateway invoiceGateway = mock(InvoiceGateway.class);
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final InvoiceNotifyLogRepository notifyLogRepository = mock(InvoiceNotifyLogRepository.class);
    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final RedisStreamMessagePublisher publisher = mock(RedisStreamMessagePublisher.class);
    private final BiCacheService biCacheService = mock(BiCacheService.class);
    private final InvoiceNotifyService service = new InvoiceNotifyServiceImpl(
            invoiceGateway, payOrderRepository, notifyLogRepository, merchantProfileRepository, publisher, biCacheService);

    @Test
    void notifyShouldVerifyUpdateInvoiceAndPublishEmailMessage() {
        when(payOrderRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.of(order()));
        when(notifyLogRepository.findByOrder("ORDER001", "20260703")).thenReturn(Optional.empty());
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        when(invoiceGateway.verifyAndParse(rawBody(), "INV_KEY")).thenReturn(validResult());

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("SUCCESS");
        verify(payOrderRepository).updateInvoiceStatus(eq(10L), eq("ISSUED"), eq("INV001"),
                eq("CODE001"), eq("https://example.com/invoice.pdf"), any(LocalDateTime.class));
        verify(biCacheService).evictAll();
        verify(notifyLogRepository).markProcessed("ORDER001", "20260703");
        ArgumentCaptor<InvoiceNotifyLog> logCaptor = ArgumentCaptor.forClass(InvoiceNotifyLog.class);
        verify(notifyLogRepository).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getVerifyResult()).isEqualTo(1);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(publisher).publish(eq("pii:invoice:email"), eq("pii.invoice.email"), eq("ORDER001"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue())
                .contains("\"payOrderId\":10")
                .contains("\"to\":\"buyer@example.com\"")
                .contains("\"invoicePdfUrl\":\"https://example.com/invoice.pdf\"");
    }

    @Test
    void notifyShouldReturnSuccessWhenAlreadyLogged() {
        when(notifyLogRepository.findByOrder("ORDER001", "20260703")).thenReturn(Optional.of(new InvoiceNotifyLog()));

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("SUCCESS");
        verify(invoiceGateway, never()).verifyAndParse(any(), any());
    }

    @Test
    void notifyShouldReturnFailWhenInvalid() {
        when(payOrderRepository.findByOutTradeNo("ORDER001")).thenReturn(Optional.of(order()));
        when(notifyLogRepository.findByOrder("ORDER001", "20260703")).thenReturn(Optional.empty());
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        NotifyVerifyResult invalid = new NotifyVerifyResult();
        invalid.setValid(false);
        when(invoiceGateway.verifyAndParse(rawBody(), "INV_KEY")).thenReturn(invalid);

        String result = service.notify(rawBody(), "SIGN");

        assertThat(result).isEqualTo("FAIL");
        verify(payOrderRepository, never()).updateInvoiceStatus(any(), any(), any(), any(), any(), any());
    }

    private String rawBody() {
        return "{\"merOrderId\":\"ORDER001\",\"merOrderDate\":\"20260703\",\"status\":\"ISSUED\"}";
    }

    private NotifyVerifyResult validResult() {
        NotifyPayload payload = new NotifyPayload();
        payload.setMerOrderId("ORDER001");
        payload.setMerOrderDate("20260703");
        payload.setStatus("ISSUED");
        payload.setInvoiceNo("INV001");
        payload.setInvoiceCode("CODE001");
        payload.setPdfUrl("https://example.com/invoice.pdf");
        NotifyVerifyResult result = new NotifyVerifyResult();
        result.setValid(true);
        result.setPayload(payload);
        return result;
    }

    private PayOrder order() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setMerchantId(100L);
        order.setOutTradeNo("ORDER001");
        order.setUmsMerOrderDate("20260703");
        order.setBuyerEmail("buyer@example.com");
        return order;
    }

    private MerchantProfile merchant() {
        MerchantProfile merchant = new MerchantProfile();
        merchant.setId(100L);
        merchant.setUmsInvoiceSignKeyEnc("INV_KEY");
        return merchant;
    }
}
