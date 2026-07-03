package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.command.CreateRefundCommand;
import com.manzhushaka.biz.pii.application.result.RefundResult;
import com.manzhushaka.biz.pii.application.service.RefundService;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.RefundRecord;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.domain.repository.RefundRecordRepository;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.PaymentGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.RefundRequest;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RefundServiceImpl implements RefundService {
    private static final DateTimeFormatter REFUND_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PayOrderRepository payOrderRepository;
    private final RefundRecordRepository refundRecordRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final PaymentGateway paymentGateway;
    private final PiiProperties properties;

    public RefundServiceImpl(PayOrderRepository payOrderRepository,
                             RefundRecordRepository refundRecordRepository,
                             MerchantProfileRepository merchantProfileRepository,
                             PaymentGateway paymentGateway,
                             PiiProperties properties) {
        this.payOrderRepository = payOrderRepository;
        this.refundRecordRepository = refundRecordRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.paymentGateway = paymentGateway;
        this.properties = properties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResult create(CreateRefundCommand command) {
        validateCommand(command);
        PayOrder order = payOrderRepository.findById(command.payOrderId())
                .orElseThrow(() -> new ServiceException("订单不存在"));
        validateOrder(command, order);

        MerchantProfile merchant = merchantProfileRepository.findById(command.merchantId())
                .orElseThrow(() -> new ServiceException("商户不存在"));

        LocalDateTime now = LocalDateTime.now();
        RefundRecord record = buildRefundRecord(command, now);
        Long id = refundRecordRepository.insert(record);
        record.setId(id);

        paymentGateway.refund(buildRefundRequest(order, record, merchant));
        return buildResult(record);
    }

    private void validateCommand(CreateRefundCommand command) {
        if (command == null || command.payOrderId() == null || command.merchantId() == null) {
            throw new ServiceException("退款订单不能为空");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new ServiceException("退款金额必须大于0");
        }
    }

    private void validateOrder(CreateRefundCommand command, PayOrder order) {
        if (!"PAID".equals(order.getPayStatus())) {
            throw new ServiceException("订单状态不允许退款");
        }
        if (!command.merchantId().equals(order.getMerchantId())) {
            throw new ServiceException("商户不匹配");
        }
        long refunded = order.getRefundAmount() == null ? 0L : order.getRefundAmount();
        if (refunded + command.amount() > order.getAmount()) {
            throw new ServiceException("退款金额超限");
        }
    }

    private RefundRecord buildRefundRecord(CreateRefundCommand command, LocalDateTime now) {
        RefundRecord record = new RefundRecord();
        record.setMerchantId(command.merchantId());
        record.setPayOrderId(command.payOrderId());
        record.setOutRefundNo(nextOutRefundNo(now));
        record.setAmount(command.amount());
        record.setReason(StringUtils.isBlank(command.reason()) ? "退款" : command.reason());
        record.setStatus("PENDING");
        record.setOperatorId(command.operatorId());
        record.setTriggerInvoiceReverse(1);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        return record;
    }

    private RefundRequest buildRefundRequest(PayOrder order, RefundRecord record, MerchantProfile merchant) {
        RefundRequest request = new RefundRequest();
        request.setAppId(properties.getWechat().getAppId());
        request.setAppKey(properties.getWechat().getAppKey());
        request.setMerchantId(merchant.getUmsMerchantId());
        request.setTerminalId(merchant.getUmsTerminalId());
        request.setOutTradeNo(order.getOutTradeNo());
        request.setRefundOrderId(record.getOutRefundNo());
        request.setRefundAmount(record.getAmount());
        request.setRefundDesc(record.getReason());
        request.setInstMid(properties.getPay().getInstMid());
        request.setProd("REAL".equalsIgnoreCase(properties.getMode()));
        return request;
    }

    private RefundResult buildResult(RefundRecord record) {
        RefundResult result = new RefundResult();
        result.setId(record.getId());
        result.setOutRefundNo(record.getOutRefundNo());
        result.setStatus(record.getStatus());
        return result;
    }

    private String nextOutRefundNo(LocalDateTime now) {
        int suffix = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "PIR" + now.format(REFUND_TIME) + suffix;
    }
}
