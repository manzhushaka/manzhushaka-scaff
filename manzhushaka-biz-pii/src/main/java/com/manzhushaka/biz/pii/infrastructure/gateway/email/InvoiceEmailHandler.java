package com.manzhushaka.biz.pii.infrastructure.gateway.email;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.framework.mq.AbstractRedisStreamMessageHandler;
import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamRecord;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEmailHandler extends AbstractRedisStreamMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEmailHandler.class);

    private final InvoiceEmailGateway invoiceEmailGateway;

    public InvoiceEmailHandler(RedisStreamGateway gateway,
                               ISysMqMessageLogService logService,
                               InvoiceEmailGateway invoiceEmailGateway) {
        super(gateway, logService);
        this.invoiceEmailGateway = invoiceEmailGateway;
    }

    @Override
    public String messageType() {
        return "pii.invoice.email";
    }

    @Override
    public String streamKey() {
        return "pii:invoice:email";
    }

    @Override
    public String consumerGroup() {
        return "pii-invoice-email-group";
    }

    @Override
    public String consumerName() {
        return "pii-invoice-email-consumer";
    }

    @Override
    protected void doHandle(RedisStreamRecord record) {
        JSONObject payload = JSON.parseObject(record.getBodyValue("payload"));
        InvoiceEmailRequest request = payload.toJavaObject(InvoiceEmailRequest.class);
        try {
            invoiceEmailGateway.send(request);
        } catch (RuntimeException e) {
            log.warn("Invoice email send failed: payOrderId={}, outTradeNo={}, to={}",
                    request.getPayOrderId(), request.getOutTradeNo(), request.getTo(), e);
        }
    }
}
