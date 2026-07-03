package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.result.AnonInvoiceDownloadResult;
import com.manzhushaka.biz.pii.application.result.AnonOrderResult;
import com.manzhushaka.biz.pii.application.service.AnonOrderService;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.InvoiceGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.PickupResponse;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
public class AnonOrderServiceImpl implements AnonOrderService {
    private static final String TOKEN_KEY_PREFIX = "pii:order:token:";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final PayOrderRepository payOrderRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final InvoiceGateway invoiceGateway;
    private final RedisCache redisCache;

    public AnonOrderServiceImpl(PayOrderRepository payOrderRepository,
                                MerchantProfileRepository merchantProfileRepository,
                                InvoiceGateway invoiceGateway,
                                RedisCache redisCache) {
        this.payOrderRepository = payOrderRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.invoiceGateway = invoiceGateway;
        this.redisCache = redisCache;
    }

    @Override
    public AnonOrderResult getOrder(String outTradeNo, String token) {
        PayOrder order = loadOrder(outTradeNo, token);
        return buildOrderResult(order);
    }

    @Override
    public AnonInvoiceDownloadResult downloadInvoice(String outTradeNo, String token) {
        PayOrder order = loadOrder(outTradeNo, token);
        validateInvoiceIssued(order);
        MerchantProfile merchant = merchantProfileRepository.findById(order.getMerchantId())
                .orElseThrow(() -> new ServiceException("商户不存在"));
        PickupResponse response = invoiceGateway.pickup(buildPickupRequest(order, merchant));
        if (response == null || StringUtils.isBlank(response.getPdf())) {
            throw new ServiceException("发票文件不存在");
        }

        AnonInvoiceDownloadResult result = new AnonInvoiceDownloadResult();
        result.setFilename(order.getOutTradeNo() + ".pdf");
        result.setContentType(PDF_CONTENT_TYPE);
        result.setContent(Base64.getDecoder().decode(response.getPdf()));
        return result;
    }

    private PayOrder loadOrder(String outTradeNo, String token) {
        validateToken(outTradeNo, token);
        return payOrderRepository.findByOutTradeNoAndToken(outTradeNo, token)
                .orElseThrow(() -> new ServiceException("订单不存在或令牌无效"));
    }

    private void validateToken(String outTradeNo, String token) {
        if (StringUtils.isBlank(outTradeNo) || StringUtils.isBlank(token)) {
            throw new ServiceException("订单访问令牌无效");
        }
        String cachedToken = redisCache.getCacheObject(TOKEN_KEY_PREFIX + outTradeNo);
        if (!token.equals(cachedToken)) {
            throw new ServiceException("订单访问令牌无效");
        }
    }

    private void validateInvoiceIssued(PayOrder order) {
        if (!"ISSUED".equalsIgnoreCase(order.getInvoiceStatus())
                || StringUtils.isBlank(order.getInvoiceNo())
                || StringUtils.isBlank(order.getInvoiceCode())) {
            throw new ServiceException("发票尚未开具");
        }
    }

    private PickupRequest buildPickupRequest(PayOrder order, MerchantProfile merchant) {
        PickupRequest request = new PickupRequest();
        request.setMerchantId(merchant.getUmsMerchantId());
        request.setTerminalId(merchant.getUmsTerminalId());
        request.setMerOrderId(order.getOutTradeNo());
        request.setMerOrderDate(order.getUmsMerOrderDate());
        request.setInvoiceNo(order.getInvoiceNo());
        request.setInvoiceCode(order.getInvoiceCode());
        request.setMsgSrc(merchant.getInvoiceMsgSrc());
        request.setMsgId(UUID.randomUUID().toString().replace("-", ""));
        request.setSignKey(merchant.getUmsInvoiceSignKeyEnc());
        return request;
    }

    private AnonOrderResult buildOrderResult(PayOrder order) {
        AnonOrderResult result = new AnonOrderResult();
        result.setOutTradeNo(order.getOutTradeNo());
        result.setAmount(order.getAmount());
        result.setBuyerName(order.getBuyerName());
        result.setBuyerTaxCode(order.getBuyerTaxCode());
        result.setPayStatus(order.getPayStatus());
        result.setPayTime(order.getPayTime());
        result.setPayTradeNo(order.getPayTradeNo());
        result.setInvoiceStatus(order.getInvoiceStatus());
        result.setInvoiceNo(order.getInvoiceNo());
        result.setInvoiceCode(order.getInvoiceCode());
        result.setInvoicePdfUrl(order.getInvoicePdfUrl());
        result.setInvoiceIssueTime(order.getInvoiceIssueTime());
        return result;
    }
}
