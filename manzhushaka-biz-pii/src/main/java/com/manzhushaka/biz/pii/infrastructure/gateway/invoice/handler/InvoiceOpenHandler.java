package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceResponse;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.framework.mq.AbstractRedisStreamMessageHandler;
import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamRecord;
import com.manzhushaka.system.service.ISysMqMessageLogService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InvoiceOpenHandler extends AbstractRedisStreamMessageHandler {

    private final InvoiceGateway invoiceGateway;

    private final PayOrderRepository payOrderRepository;

    public InvoiceOpenHandler(RedisStreamGateway gateway,
                              ISysMqMessageLogService logService,
                              InvoiceGateway invoiceGateway,
                              PayOrderRepository payOrderRepository) {
        super(gateway, logService);
        this.invoiceGateway = invoiceGateway;
        this.payOrderRepository = payOrderRepository;
    }

    @Override
    public String messageType() {
        return "pii.invoice.open";
    }

    @Override
    public String streamKey() {
        return "pii:invoice:open";
    }

    @Override
    public String consumerGroup() {
        return "pii-invoice-open-group";
    }

    @Override
    public String consumerName() {
        return "pii-invoice-open-consumer";
    }

    @Override
    protected void doHandle(RedisStreamRecord record) {
        JSONObject payload = JSON.parseObject(record.getBodyValue("payload"));
        Long payOrderId = payload.getLong("payOrderId");
        if (payOrderId == null) {
            throw new ServiceException("开票消息缺少 payOrderId", 10202);
        }
        if (alreadyFinal(payOrderId)) {
            return;
        }
        InvoiceRequest request = payload.toJavaObject(InvoiceRequest.class);
        try {
            InvoiceResponse response = invoiceGateway.invoice(request);
            payOrderRepository.updateInvoiceStatus(
                    payOrderId,
                    hasText(response.getStatus()) ? response.getStatus() : "ISSUED",
                    response.getInvoiceNo(),
                    response.getInvoiceCode(),
                    response.getPdfUrl(),
                    LocalDateTime.now()
            );
        } catch (RuntimeException e) {
            payOrderRepository.updateInvoiceStatus(payOrderId, "FAILED", null, null, null, null);
            throw e;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean alreadyFinal(Long payOrderId) {
        return payOrderRepository.findById(payOrderId)
                .map(PayOrder::getInvoiceStatus)
                .filter(status -> "ISSUED".equals(status) || "REVERSED".equals(status))
                .isPresent();
    }
}
