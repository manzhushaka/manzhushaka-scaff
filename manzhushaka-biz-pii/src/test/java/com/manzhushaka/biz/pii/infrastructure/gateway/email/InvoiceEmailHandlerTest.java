package com.manzhushaka.biz.pii.infrastructure.gateway.email;

import com.alibaba.fastjson2.JSON;
import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamRecord;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

class InvoiceEmailHandlerTest {

    private final InvoiceEmailGateway invoiceEmailGateway = mock(InvoiceEmailGateway.class);
    private final InvoiceEmailHandler handler = new InvoiceEmailHandler(
            mock(RedisStreamGateway.class),
            mock(ISysMqMessageLogService.class),
            invoiceEmailGateway
    );

    @Test
    void doHandleShouldSendInvoiceEmail() {
        handler.doHandle(record(payload()));

        verify(invoiceEmailGateway).send(any(InvoiceEmailRequest.class));
    }

    @Test
    void doHandleShouldSwallowGatewayExceptionForMqAck() {
        doThrow(new IllegalStateException("mail down")).when(invoiceEmailGateway).send(any(InvoiceEmailRequest.class));

        handler.doHandle(record(payload()));

        verify(invoiceEmailGateway).send(any(InvoiceEmailRequest.class));
    }

    private Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payOrderId", 10L);
        payload.put("to", "buyer@example.com");
        payload.put("outTradeNo", "ORDER001");
        payload.put("invoiceNo", "INV001");
        payload.put("invoiceCode", "CODE001");
        payload.put("invoicePdfUrl", "https://example.com/inv.pdf");
        return payload;
    }

    private RedisStreamRecord record(Map<String, Object> payload) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("businessKey", "ORDER001");
        body.put("payload", JSON.toJSONString(payload));
        return new RedisStreamRecord("pii:invoice:email", "1-0", body);
    }
}
