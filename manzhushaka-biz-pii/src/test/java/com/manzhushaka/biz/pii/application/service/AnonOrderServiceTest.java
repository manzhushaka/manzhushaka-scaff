package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.result.AnonInvoiceDownloadResult;
import com.manzhushaka.biz.pii.application.result.AnonOrderResult;
import com.manzhushaka.biz.pii.application.service.impl.AnonOrderServiceImpl;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupResponse;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnonOrderServiceTest {
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final InvoiceGateway invoiceGateway = mock(InvoiceGateway.class);
    private final RedisCache redisCache = mock(RedisCache.class);
    private final AnonOrderService service = new AnonOrderServiceImpl(
            payOrderRepository, merchantProfileRepository, invoiceGateway, redisCache);

    @Test
    void getOrderShouldValidateTokenAndReturnOrderStatus() {
        when(redisCache.getCacheObject("pii:order:token:ORDER001")).thenReturn("TOKEN001");
        when(payOrderRepository.findByOutTradeNoAndToken("ORDER001", "TOKEN001")).thenReturn(Optional.of(order()));

        AnonOrderResult result = service.getOrder("ORDER001", "TOKEN001");

        assertThat(result.getOutTradeNo()).isEqualTo("ORDER001");
        assertThat(result.getAmount()).isEqualTo(8800L);
        assertThat(result.getPayStatus()).isEqualTo("PAID");
        assertThat(result.getInvoiceStatus()).isEqualTo("ISSUED");
        assertThat(result.getInvoiceNo()).isEqualTo("INV001");
        assertThat(result.getInvoicePdfUrl()).isEqualTo("https://example.com/invoice.pdf");
    }

    @Test
    void getOrderShouldRejectInvalidTokenBeforeQueryDatabase() {
        when(redisCache.getCacheObject("pii:order:token:ORDER001")).thenReturn("TOKEN001");

        assertThatThrownBy(() -> service.getOrder("ORDER001", "BAD"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("订单访问令牌无效");

        verify(payOrderRepository, never()).findByOutTradeNoAndToken(any(), any());
    }

    @Test
    void downloadInvoiceShouldPickupPdfFromInvoiceGateway() {
        when(redisCache.getCacheObject("pii:order:token:ORDER001")).thenReturn("TOKEN001");
        when(payOrderRepository.findByOutTradeNoAndToken("ORDER001", "TOKEN001")).thenReturn(Optional.of(order()));
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        when(invoiceGateway.pickup(any(PickupRequest.class))).thenReturn(pickupResponse());

        AnonInvoiceDownloadResult result = service.downloadInvoice("ORDER001", "TOKEN001");

        assertThat(result.getFilename()).isEqualTo("ORDER001.pdf");
        assertThat(new String(result.getContent(), StandardCharsets.UTF_8)).isEqualTo("%PDF");
        ArgumentCaptor<PickupRequest> requestCaptor = ArgumentCaptor.forClass(PickupRequest.class);
        verify(invoiceGateway).pickup(requestCaptor.capture());
        PickupRequest request = requestCaptor.getValue();
        assertThat(request.getMerchantId()).isEqualTo("MID");
        assertThat(request.getTerminalId()).isEqualTo("TID");
        assertThat(request.getMerOrderId()).isEqualTo("ORDER001");
        assertThat(request.getMerOrderDate()).isEqualTo("20260703");
        assertThat(request.getInvoiceNo()).isEqualTo("INV001");
        assertThat(request.getInvoiceCode()).isEqualTo("CODE001");
        assertThat(request.getMsgSrc()).isEqualTo("MSG_SRC");
        assertThat(request.getSignKey()).isEqualTo("INV_KEY");
    }

    private PayOrder order() {
        PayOrder order = new PayOrder();
        order.setId(10L);
        order.setMerchantId(100L);
        order.setOutTradeNo("ORDER001");
        order.setUmsMerOrderDate("20260703");
        order.setAmount(8800L);
        order.setBuyerName("张三");
        order.setBuyerTaxCode("91330000X");
        order.setPayStatus("PAID");
        order.setPayTradeNo("PAY001");
        order.setPayTime(LocalDateTime.of(2026, 7, 3, 12, 0));
        order.setInvoiceStatus("ISSUED");
        order.setInvoiceNo("INV001");
        order.setInvoiceCode("CODE001");
        order.setInvoicePdfUrl("https://example.com/invoice.pdf");
        order.setInvoiceIssueTime(LocalDateTime.of(2026, 7, 3, 12, 1));
        order.setOrderToken("TOKEN001");
        return order;
    }

    private MerchantProfile merchant() {
        MerchantProfile merchant = new MerchantProfile();
        merchant.setId(100L);
        merchant.setUmsMerchantId("MID");
        merchant.setUmsTerminalId("TID");
        merchant.setInvoiceMsgSrc("MSG_SRC");
        merchant.setUmsInvoiceSignKeyEnc("INV_KEY");
        return merchant;
    }

    private PickupResponse pickupResponse() {
        PickupResponse response = new PickupResponse();
        response.setResultCode("SUCCESS");
        response.setPdf(Base64.getEncoder().encodeToString("%PDF".getBytes(StandardCharsets.UTF_8)));
        return response;
    }
}
