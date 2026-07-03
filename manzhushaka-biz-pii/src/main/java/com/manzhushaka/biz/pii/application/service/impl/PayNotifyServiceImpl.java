package com.manzhushaka.biz.pii.application.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.biz.pii.application.service.BiCacheService;
import com.manzhushaka.biz.pii.application.service.PayNotifyService;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.PaymentNotifyLog;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.domain.repository.PaymentNotifyLogRepository;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.InvoiceRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.PaymentGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PayNotifyServiceImpl implements PayNotifyService {
    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";

    private final PaymentGateway paymentGateway;
    private final PayOrderRepository payOrderRepository;
    private final PaymentNotifyLogRepository notifyLogRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final TaxItemRepository taxItemRepository;
    private final RedisStreamMessagePublisher publisher;
    private final PiiProperties properties;
    private final BiCacheService biCacheService;

    public PayNotifyServiceImpl(PaymentGateway paymentGateway,
                                PayOrderRepository payOrderRepository,
                                PaymentNotifyLogRepository notifyLogRepository,
                                MerchantProfileRepository merchantProfileRepository,
                                TaxItemRepository taxItemRepository,
                                RedisStreamMessagePublisher publisher,
                                PiiProperties properties,
                                BiCacheService biCacheService) {
        this.paymentGateway = paymentGateway;
        this.payOrderRepository = payOrderRepository;
        this.notifyLogRepository = notifyLogRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.taxItemRepository = taxItemRepository;
        this.publisher = publisher;
        this.properties = properties;
        this.biCacheService = biCacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String notify(String rawBody, String sign) {
        String outTradeNo = parseOutTradeNo(rawBody);
        PayOrder order = payOrderRepository.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new ServiceException("订单不存在"));
        if (notifyLogRepository.findByOutTradeNo(outTradeNo).isPresent()) {
            return SUCCESS;
        }
        MerchantProfile merchant = merchantProfileRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new ServiceException("商户不存在"));

        NotifyVerifyResult verifyResult = paymentGateway.verifyAndParse(rawBody, merchant.getUmsPaySignKeyEnc());
        if (verifyResult == null || !verifyResult.isValid()) {
            insertNotifyLog(outTradeNo, rawBody, sign, 0, 0);
            return FAIL;
        }

        try {
            insertNotifyLog(outTradeNo, rawBody, sign, 1, 0);
        } catch (DuplicateKeyException e) {
            return SUCCESS;
        }

        NotifyPayload payload = verifyResult.getPayload();
        if (payload != null && SUCCESS.equalsIgnoreCase(payload.getTradeStatus())) {
            payOrderRepository.updatePayStatus(order.getId(), "PAID", payload.getTradeNo(), LocalDateTime.now());
            publishInvoiceOpen(order, merchant);
            notifyLogRepository.markProcessed(outTradeNo);
            biCacheService.evictAll();
        }
        return SUCCESS;
    }

    private void insertNotifyLog(String outTradeNo, String rawBody, String sign, Integer verifyResult, Integer processed) {
        PaymentNotifyLog log = new PaymentNotifyLog();
        log.setOutTradeNo(outTradeNo);
        log.setNotifyPayload(rawBody);
        log.setSign(sign);
        log.setVerifyResult(verifyResult);
        log.setProcessed(processed);
        log.setCreatedAt(LocalDateTime.now());
        notifyLogRepository.insert(log);
    }

    private void publishInvoiceOpen(PayOrder order, MerchantProfile merchant) {
        InvoiceRequest request = buildInvoiceRequest(order, merchant);
        JSONObject payload = (JSONObject) JSON.toJSON(request);
        payload.put("payOrderId", order.getId());
        publisher.publish("pii:invoice:open", "pii.invoice.open", order.getOutTradeNo(), payload.toJSONString());
    }

    private InvoiceRequest buildInvoiceRequest(PayOrder order, MerchantProfile merchant) {
        TaxItem taxItem = taxItemRepository.findById(order.getTaxItemId()).orElse(null);
        InvoiceRequest request = new InvoiceRequest();
        request.setMerchantId(merchant.getUmsMerchantId());
        request.setTerminalId(merchant.getUmsTerminalId());
        request.setMerOrderDate(order.getUmsMerOrderDate());
        request.setMerOrderId(order.getOutTradeNo());
        request.setBuyerName(order.getBuyerName());
        request.setBuyerTaxCode(order.getBuyerTaxCode());
        request.setAmount(order.getAmount());
        request.setGoodsDetail(buildGoodsDetail(order, taxItem));
        request.setNotifyMobileNo(order.getBuyerMobile());
        request.setNotifyEMail(order.getBuyerEmail());
        request.setNotifyUrl(properties.getInvoice().getNotifyUrl());
        request.setMsgSrc(merchant.getInvoiceMsgSrc());
        request.setSignKey(merchant.getUmsInvoiceSignKeyEnc());
        return request;
    }

    private String buildGoodsDetail(PayOrder order, TaxItem taxItem) {
        JSONObject item = new JSONObject();
        item.put("name", taxItem == null || StringUtils.isBlank(taxItem.getName()) ? "商品" : taxItem.getName());
        if (taxItem != null) {
            item.put("taxItemCode", taxItem.getTaxItemCode());
            item.put("taxRate", taxItem.getTaxRate());
        }
        item.put("amount", order.getAmount());
        JSONArray detail = new JSONArray();
        detail.add(item);
        return detail.toJSONString();
    }

    private String parseOutTradeNo(String rawBody) {
        JSONObject body = JSON.parseObject(rawBody);
        String outTradeNo = firstText(body.getString("outTradeNo"), body.getString("merOrderId"));
        if (StringUtils.isBlank(outTradeNo)) {
            throw new ServiceException("支付回调缺少订单号");
        }
        return outTradeNo;
    }

    private String firstText(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }
}
