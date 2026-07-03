package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.handler;

import com.alibaba.fastjson2.JSON;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseResponse;
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

class InvoiceReverseHandlerTest {

    private final InvoiceGateway invoiceGateway = mock(InvoiceGateway.class);
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final InvoiceReverseHandler handler = new InvoiceReverseHandler(
            mock(RedisStreamGateway.class),
            mock(ISysMqMessageLogService.class),
            invoiceGateway,
            payOrderRepository
    );

    @Test
    void doHandleShouldCallReverseGatewayAndMarkOrderReversed() {
        ReverseResponse response = new ReverseResponse();
        response.setStatus("REVERSED");
        when(invoiceGateway.reverse(any(ReverseRequest.class))).thenReturn(response);

        handler.doHandle(record(payload()));

        verify(invoiceGateway).reverse(any(ReverseRequest.class));
        verify(payOrderRepository).updateInvoiceReverseStatus(eq(10L), eq("REVERSED"), any(LocalDateTime.class));
    }

    @Test
    void doHandleShouldMarkFailedAndRethrowWhenReverseFails() {
        when(invoiceGateway.reverse(any(ReverseRequest.class))).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> handler.doHandle(record(payload())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        verify(payOrderRepository).updateInvoiceReverseStatus(10L, "REVERSE_FAILED", null);
    }

    private Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payOrderId", 10L);
        payload.put("merchantId", "MID");
        payload.put("terminalId", "TID");
        payload.put("merOrderId", "ORDER001");
        payload.put("merOrderDate", "20260703");
        payload.put("invoiceNo", "INV001");
        payload.put("invoiceCode", "CODE001");
        payload.put("signKey", "K");
        return payload;
    }

    private RedisStreamRecord record(Map<String, Object> payload) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("businessKey", "ORDER001");
        body.put("payload", JSON.toJSONString(payload));
        return new RedisStreamRecord("pii:invoice:reverse", "1-0", body);
    }
}
