package com.manzhushaka.biz.pii.application.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.biz.pii.application.service.BiCacheService;
import com.manzhushaka.biz.pii.application.service.RefundNotifyService;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.RefundRecord;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.domain.repository.RefundRecordRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.ReverseRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.PaymentGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyPayload;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.NotifyVerifyResult;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.mq.RedisStreamMessagePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefundNotifyServiceImpl implements RefundNotifyService {
    private static final String SUCCESS = "SUCCESS";
    private static final String FAIL = "FAIL";

    private final PaymentGateway paymentGateway;
    private final RefundRecordRepository refundRecordRepository;
    private final PayOrderRepository payOrderRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final RedisStreamMessagePublisher publisher;
    private final BiCacheService biCacheService;

    public RefundNotifyServiceImpl(PaymentGateway paymentGateway,
                                   RefundRecordRepository refundRecordRepository,
                                   PayOrderRepository payOrderRepository,
                                   MerchantProfileRepository merchantProfileRepository,
                                   RedisStreamMessagePublisher publisher,
                                   BiCacheService biCacheService) {
        this.paymentGateway = paymentGateway;
        this.refundRecordRepository = refundRecordRepository;
        this.payOrderRepository = payOrderRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.publisher = publisher;
        this.biCacheService = biCacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String notify(String rawBody, String sign) {
        String outRefundNo = parseOutRefundNo(rawBody);
        RefundRecord refund = refundRecordRepository.findByOutRefundNo(outRefundNo)
                .orElseThrow(() -> new ServiceException("退款单不存在"));
        if (SUCCESS.equalsIgnoreCase(refund.getStatus())) {
            return SUCCESS;
        }
        PayOrder order = payOrderRepository.findById(refund.getPayOrderId())
                .orElseThrow(() -> new ServiceException("订单不存在"));
        MerchantProfile merchant = merchantProfileRepository.findById(refund.getMerchantId())
                .orElseThrow(() -> new ServiceException("商户不存在"));

        NotifyVerifyResult verifyResult = paymentGateway.verifyAndParse(rawBody, merchant.getUmsPaySignKeyEnc());
        if (verifyResult == null || !verifyResult.isValid()) {
            return FAIL;
        }

        NotifyPayload payload = verifyResult.getPayload();
        if (payload != null && SUCCESS.equalsIgnoreCase(payload.getTradeStatus())) {
            Long refundAmount = payload.getRefundAmount() == null ? refund.getAmount() : payload.getRefundAmount();
            Long totalRefundAmount = (order.getRefundAmount() == null ? 0L : order.getRefundAmount()) + refundAmount;
            String payStatus = totalRefundAmount >= order.getAmount() ? "REFUNDED" : "REFUNDING";
            refundRecordRepository.updateStatus(refund.getId(), SUCCESS, payload.getTradeNo(), LocalDateTime.now());
            payOrderRepository.updateRefundAmountAndStatus(order.getId(), totalRefundAmount, payStatus);
            publishInvoiceReverseIfNeeded(refund, order, merchant);
            biCacheService.evictAll();
        }
        return SUCCESS;
    }

    private void publishInvoiceReverseIfNeeded(RefundRecord refund, PayOrder order, MerchantProfile merchant) {
        if (!Integer.valueOf(1).equals(refund.getTriggerInvoiceReverse())
                || !"ISSUED".equalsIgnoreCase(order.getInvoiceStatus())
                || StringUtils.isBlank(order.getInvoiceNo())
                || StringUtils.isBlank(order.getInvoiceCode())) {
            return;
        }
        ReverseRequest request = new ReverseRequest();
        request.setMerchantId(merchant.getUmsMerchantId());
        request.setTerminalId(merchant.getUmsTerminalId());
        request.setMerOrderId(order.getOutTradeNo());
        request.setMerOrderDate(order.getUmsMerOrderDate());
        request.setInvoiceNo(order.getInvoiceNo());
        request.setInvoiceCode(order.getInvoiceCode());
        request.setReason(StringUtils.isBlank(refund.getReason()) ? "退款红冲" : refund.getReason());
        request.setMsgSrc(merchant.getInvoiceMsgSrc());
        request.setMsgId(UUID.randomUUID().toString().replace("-", ""));
        request.setSignKey(merchant.getUmsInvoiceSignKeyEnc());

        JSONObject payload = (JSONObject) JSON.toJSON(request);
        payload.put("payOrderId", order.getId());
        publisher.publish("pii:invoice:reverse", "pii.invoice.reverse", order.getOutTradeNo(), payload.toJSONString());
    }

    private String parseOutRefundNo(String rawBody) {
        JSONObject body = JSON.parseObject(rawBody);
        String outRefundNo = firstText(body.getString("refundOrderId"), body.getString("outRefundNo"));
        if (StringUtils.isBlank(outRefundNo)) {
            throw new ServiceException("退款回调缺少退款单号");
        }
        return outRefundNo;
    }

    private String firstText(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }
}
