package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseResponse;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.framework.mq.AbstractRedisStreamMessageHandler;
import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamRecord;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InvoiceReverseHandler extends AbstractRedisStreamMessageHandler {

    private final InvoiceGateway invoiceGateway;

    private final PayOrderRepository payOrderRepository;

    public InvoiceReverseHandler(RedisStreamGateway gateway,
                                 ISysMqMessageLogService logService,
                                 InvoiceGateway invoiceGateway,
                                 PayOrderRepository payOrderRepository) {
        super(gateway, logService);
        this.invoiceGateway = invoiceGateway;
        this.payOrderRepository = payOrderRepository;
    }

    @Override
    public String messageType() {
        return "pii.invoice.reverse";
    }

    @Override
    public String streamKey() {
        return "pii:invoice:reverse";
    }

    @Override
    public String consumerGroup() {
        return "pii-invoice-reverse-group";
    }

    @Override
    public String consumerName() {
        return "pii-invoice-reverse-consumer";
    }

    @Override
    protected void doHandle(RedisStreamRecord record) {
        JSONObject payload = JSON.parseObject(record.getBodyValue("payload"));
        Long payOrderId = payload.getLong("payOrderId");
        if (payOrderId == null) {
            throw new ServiceException("发票红冲消息缺少 payOrderId", 10203);
        }
        ReverseRequest request = payload.toJavaObject(ReverseRequest.class);
        try {
            ReverseResponse response = invoiceGateway.reverse(request);
            payOrderRepository.updateInvoiceReverseStatus(
                    payOrderId,
                    hasText(response.getStatus()) ? response.getStatus() : "REVERSED",
                    LocalDateTime.now()
            );
        } catch (RuntimeException e) {
            payOrderRepository.updateInvoiceReverseStatus(payOrderId, "REVERSE_FAILED", null);
            throw e;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
