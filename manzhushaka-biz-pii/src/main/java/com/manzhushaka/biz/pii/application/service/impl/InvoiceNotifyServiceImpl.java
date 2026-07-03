package com.manzhushaka.biz.pii.application.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.biz.pii.application.service.InvoiceNotifyService;
import com.manzhushaka.biz.pii.domain.model.InvoiceNotifyLog;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.InvoiceNotifyLogRepository;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.email.InvoiceEmailRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.NotifyVerifyResult;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InvoiceNotifyServiceImpl implements InvoiceNotifyService {
    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";
    private static final String ISSUED = "ISSUED";

    private final InvoiceGateway invoiceGateway;
    private final PayOrderRepository payOrderRepository;
    private final InvoiceNotifyLogRepository notifyLogRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final RedisStreamMessagePublisher publisher;

    public InvoiceNotifyServiceImpl(InvoiceGateway invoiceGateway,
                                    PayOrderRepository payOrderRepository,
                                    InvoiceNotifyLogRepository notifyLogRepository,
                                    MerchantProfileRepository merchantProfileRepository,
                                    RedisStreamMessagePublisher publisher) {
        this.invoiceGateway = invoiceGateway;
        this.payOrderRepository = payOrderRepository;
        this.notifyLogRepository = notifyLogRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String notify(String rawBody, String sign) {
        OrderIdentity identity = parseOrderIdentity(rawBody);
        if (notifyLogRepository.findByOrder(identity.umsMerOrderId, identity.umsMerOrderDate).isPresent()) {
            return SUCCESS;
        }
        PayOrder order = payOrderRepository.findByOutTradeNo(identity.umsMerOrderId)
                .orElseThrow(() -> new ServiceException("订单不存在"));
        MerchantProfile merchant = merchantProfileRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new ServiceException("商户不存在"));

        NotifyVerifyResult verifyResult = invoiceGateway.verifyAndParse(rawBody, merchant.getUmsInvoiceSignKeyEnc());
        if (verifyResult == null || !verifyResult.isValid()) {
            insertNotifyLog(order, identity, rawBody, sign, 0, 0);
            return FAIL;
        }

        try {
            insertNotifyLog(order, identity, rawBody, sign, 1, 0);
        } catch (DuplicateKeyException e) {
            return SUCCESS;
        }

        NotifyPayload payload = verifyResult.getPayload();
        if (payload != null && ISSUED.equalsIgnoreCase(payload.getStatus())) {
            payOrderRepository.updateInvoiceStatus(order.getId(), ISSUED, payload.getInvoiceNo(),
                    payload.getInvoiceCode(), payload.getPdfUrl(), LocalDateTime.now());
            notifyLogRepository.markProcessed(identity.umsMerOrderId, identity.umsMerOrderDate);
            publishEmailIfNeeded(order, payload);
        }
        return SUCCESS;
    }

    private void insertNotifyLog(PayOrder order, OrderIdentity identity, String rawBody, String sign,
                                 Integer verifyResult, Integer processed) {
        InvoiceNotifyLog log = new InvoiceNotifyLog();
        log.setUmsMerOrderId(identity.umsMerOrderId);
        log.setUmsMerOrderDate(identity.umsMerOrderDate);
        if (order.getQrcodeId() != null) {
            log.setQrcodeId(String.valueOf(order.getQrcodeId()));
        }
        log.setNotifyPayload(rawBody);
        log.setSign(sign);
        log.setVerifyResult(verifyResult);
        log.setProcessed(processed);
        log.setCreatedAt(LocalDateTime.now());
        notifyLogRepository.insert(log);
    }

    private void publishEmailIfNeeded(PayOrder order, NotifyPayload payload) {
        if (StringUtils.isBlank(order.getBuyerEmail())) {
            return;
        }
        InvoiceEmailRequest request = new InvoiceEmailRequest();
        request.setPayOrderId(order.getId());
        request.setTo(order.getBuyerEmail());
        request.setOutTradeNo(order.getOutTradeNo());
        request.setInvoiceNo(payload.getInvoiceNo());
        request.setInvoiceCode(payload.getInvoiceCode());
        request.setInvoicePdfUrl(payload.getPdfUrl());
        publisher.publish("pii:invoice:email", "pii.invoice.email", order.getOutTradeNo(), JSON.toJSONString(request));
    }

    private OrderIdentity parseOrderIdentity(String rawBody) {
        JSONObject body = JSON.parseObject(rawBody);
        String orderId = firstText(body.getString("merOrderId"),
                firstText(body.getString("umsMerOrderId"), body.getString("outTradeNo")));
        String orderDate = firstText(body.getString("merOrderDate"), body.getString("umsMerOrderDate"));
        if (StringUtils.isBlank(orderId) || StringUtils.isBlank(orderDate)) {
            throw new ServiceException("开票回调缺少订单号或订单日期");
        }
        return new OrderIdentity(orderId, orderDate);
    }

    private String firstText(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }

    private static class OrderIdentity {
        private final String umsMerOrderId;
        private final String umsMerOrderDate;

        private OrderIdentity(String umsMerOrderId, String umsMerOrderDate) {
            this.umsMerOrderId = umsMerOrderId;
            this.umsMerOrderDate = umsMerOrderDate;
        }
    }
}
