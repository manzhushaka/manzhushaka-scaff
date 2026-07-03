package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.command.PrecreatePayCommand;
import com.manzhushaka.biz.pii.application.result.PrecreatePayResult;
import com.manzhushaka.biz.pii.application.service.AnonPayService;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeRepository;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeTaxItemRepository;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.PaymentGateway;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.PreCreateResponse;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class AnonPayServiceImpl implements AnonPayService {
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ORDER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PayQrcodeRepository qrcodeRepository;
    private final PayQrcodeTaxItemRepository relationRepository;
    private final TaxItemRepository taxItemRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final PayOrderRepository payOrderRepository;
    private final PaymentGateway paymentGateway;
    private final PiiProperties properties;
    private final RedisCache redisCache;

    public AnonPayServiceImpl(PayQrcodeRepository qrcodeRepository,
                              PayQrcodeTaxItemRepository relationRepository,
                              TaxItemRepository taxItemRepository,
                              MerchantProfileRepository merchantProfileRepository,
                              PayOrderRepository payOrderRepository,
                              PaymentGateway paymentGateway,
                              PiiProperties properties,
                              RedisCache redisCache) {
        this.qrcodeRepository = qrcodeRepository;
        this.relationRepository = relationRepository;
        this.taxItemRepository = taxItemRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.payOrderRepository = payOrderRepository;
        this.paymentGateway = paymentGateway;
        this.properties = properties;
        this.redisCache = redisCache;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrecreatePayResult precreate(PrecreatePayCommand command) {
        validateCommand(command);
        PayQrcode qrcode = qrcodeRepository.findByCode(command.code())
                .orElseThrow(() -> new ServiceException("二维码无效"));
        validateQrcode(qrcode);
        relationRepository.findByQrcodeIdAndTaxItemId(qrcode.getId(), command.taxItemId())
                .orElseThrow(() -> new ServiceException("税目未绑定当前二维码"));
        TaxItem taxItem = taxItemRepository.findById(command.taxItemId())
                .orElseThrow(() -> new ServiceException("税目不存在"));
        if (!Integer.valueOf(1).equals(taxItem.getStatus())) {
            throw new ServiceException("税目已停用");
        }
        MerchantProfile merchant = merchantProfileRepository.findById(qrcode.getMerchantId())
                .orElseThrow(() -> new ServiceException("商户不存在"));
        validateMerchant(merchant);

        LocalDateTime now = LocalDateTime.now();
        String outTradeNo = nextOutTradeNo(now);
        String orderToken = UUID.randomUUID().toString().replace("-", "");
        String appId = properties.getWechat().getAppId();
        String merOrderDate = now.format(ORDER_DATE);

        PayOrder order = buildOrder(command, qrcode, merchant, now, outTradeNo, orderToken, appId, merOrderDate);
        payOrderRepository.insert(order);

        PreCreateResponse payResponse = paymentGateway.preCreate(
                buildPayRequest(command, merchant, taxItem, outTradeNo, appId, merOrderDate));
        redisCache.setCacheObject(orderTokenKey(outTradeNo), orderToken,
                properties.getOrder().getExpireMinutes(), TimeUnit.MINUTES);
        return buildResult(appId, outTradeNo, orderToken, payResponse);
    }

    private void validateCommand(PrecreatePayCommand command) {
        if (command == null || StringUtils.isBlank(command.code())) {
            throw new ServiceException("二维码编码不能为空");
        }
        if (command.taxItemId() == null) {
            throw new ServiceException("税目不能为空");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new ServiceException("支付金额必须大于0");
        }
    }

    private void validateQrcode(PayQrcode qrcode) {
        if (!Integer.valueOf(1).equals(qrcode.getStatus())) {
            throw new ServiceException("二维码已停用");
        }
        if (qrcode.getExpireTime() != null && qrcode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("二维码已过期");
        }
    }

    private void validateMerchant(MerchantProfile merchant) {
        if (!Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new ServiceException("商户已停用");
        }
        if (StringUtils.isBlank(merchant.getUmsMerchantId()) || StringUtils.isBlank(merchant.getUmsTerminalId())) {
            throw new ServiceException("商户支付参数未配置");
        }
    }

    private PayOrder buildOrder(PrecreatePayCommand command, PayQrcode qrcode, MerchantProfile merchant,
                                LocalDateTime now, String outTradeNo, String orderToken, String appId,
                                String merOrderDate) {
        PayOrder order = new PayOrder();
        order.setMerchantId(merchant.getId());
        order.setQrcodeId(qrcode.getId());
        order.setTaxItemId(command.taxItemId());
        order.setOutTradeNo(outTradeNo);
        order.setUmsMerOrderDate(merOrderDate);
        order.setAmount(command.amount());
        order.setBuyerName(command.buyerName());
        order.setBuyerTaxCode(command.buyerTaxCode());
        order.setBuyerEmail(command.buyerEmail());
        order.setBuyerMobile(command.buyerMobile());
        order.setBuyerOpenid(command.openid());
        order.setPayStatus("PENDING");
        order.setPayNotifyStatus("INIT");
        order.setRefundAmount(0L);
        order.setInvoiceStatus("NOT_ISSUED");
        order.setOrderToken(orderToken);
        order.setWechatAppid(appId);
        order.setClientIp(command.clientIp());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        return order;
    }

    private PreCreateRequest buildPayRequest(PrecreatePayCommand command, MerchantProfile merchant, TaxItem taxItem,
                                             String outTradeNo, String appId, String merOrderDate) {
        PreCreateRequest request = new PreCreateRequest();
        request.setAppId(appId);
        request.setAppKey(properties.getWechat().getAppKey());
        request.setMerchantId(merchant.getUmsMerchantId());
        request.setTerminalId(merchant.getUmsTerminalId());
        request.setOutTradeNo(outTradeNo);
        request.setMerOrderDate(merOrderDate);
        request.setTotalAmount(command.amount());
        request.setOpenid(command.openid());
        request.setNotifyUrl(firstText(merchant.getNotifyUrl(), properties.getPay().getNotifyUrl()));
        request.setSignKey(merchant.getUmsPaySignKeyEnc());
        request.setOrderDesc(taxItem.getName());
        request.setInstMid(properties.getPay().getInstMid());
        request.setProd("REAL".equalsIgnoreCase(properties.getMode()));
        return request;
    }

    private PrecreatePayResult buildResult(String appId, String outTradeNo, String orderToken,
                                           PreCreateResponse payResponse) {
        PrecreatePayResult result = new PrecreatePayResult();
        result.setAppId(appId);
        result.setOutTradeNo(outTradeNo);
        result.setOrderToken(orderToken);
        result.setPrepayId(payResponse.getPrepayId());
        result.setTimeStamp(payResponse.getTimestamp());
        result.setNonceStr(payResponse.getNonceStr());
        result.setPackageStr(payResponse.getPackageStr());
        result.setSignType(payResponse.getSignType());
        result.setPaySign(payResponse.getJsApiPaySign());
        return result;
    }

    private String nextOutTradeNo(LocalDateTime now) {
        int suffix = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "PII" + now.format(ORDER_TIME) + suffix;
    }

    private String firstText(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }

    private String orderTokenKey(String outTradeNo) {
        return "pii:order:token:" + outTradeNo;
    }
}
