package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.handler;

import com.alibaba.fastjson2.JSON;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceResponse;
import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamRecord;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceOpenHandlerTest {

    private final InvoiceGateway invoiceGateway = mock(InvoiceGateway.class);
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final InvoiceOpenHandler handler = new InvoiceOpenHandler(
            mock(RedisStreamGateway.class),
            mock(ISysMqMessageLogService.class),
            invoiceGateway,
            payOrderRepository
    );

    @Test
    void doHandleShouldCallInvoiceGatewayAndMarkOrderIssued() {
        InvoiceResponse response = new InvoiceResponse();
        response.setStatus("ISSUED");
        response.setInvoiceNo("INV001");
        response.setInvoiceCode("CODE001");
        response.setPdfUrl("https://example.com/inv.pdf");
        when(invoiceGateway.invoice(any(InvoiceRequest.class))).thenReturn(response);

        handler.doHandle(record(payload()));

        verify(invoiceGateway).invoice(any(InvoiceRequest.class));
        verify(payOrderRepository).updateInvoiceStatus(
                eq(10L),
                eq("ISSUED"),
                eq("INV001"),
                eq("CODE001"),
                eq("https://example.com/inv.pdf"),
                any(LocalDateTime.class)
        );
    }

    @Test
    void doHandleShouldMarkFailedAndRethrowWhenInvoiceGatewayFails() {
        when(invoiceGateway.invoice(any(InvoiceRequest.class))).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> handler.doHandle(record(payload())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        verify(payOrderRepository).updateInvoiceStatus(10L, "FAILED", null, null, null, null);
    }

    private Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payOrderId", 10L);
        payload.put("merchantId", "MID");
        payload.put("terminalId", "TID");
        payload.put("merOrderId", "ORDER001");
        payload.put("merOrderDate", "20260703");
        payload.put("buyerName", "张三");
        payload.put("amount", 100L);
        payload.put("goodsDetail", "[{\"name\":\"测试\",\"amount\":100}]");
        payload.put("signKey", "K");
        return payload;
    }

    private RedisStreamRecord record(Map<String, Object> payload) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("businessKey", "ORDER001");
        body.put("payload", JSON.toJSONString(payload));
        return new RedisStreamRecord("pii:invoice:open", "1-0", body);
    }
}
