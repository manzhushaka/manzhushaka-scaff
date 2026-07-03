package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.PrecreatePayCommand;
import com.manzhushaka.biz.pii.application.service.impl.AnonPayServiceImpl;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
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
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.core.redis.RedisCache;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnonPayServiceTest {

    private final PayQrcodeRepository qrcodeRepository = mock(PayQrcodeRepository.class);
    private final PayQrcodeTaxItemRepository relationRepository = mock(PayQrcodeTaxItemRepository.class);
    private final TaxItemRepository taxItemRepository = mock(TaxItemRepository.class);
    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
    private final RedisCache redisCache = mock(RedisCache.class);
    private final PiiProperties properties = new PiiProperties();
    private final AnonPayService service = new AnonPayServiceImpl(qrcodeRepository, relationRepository,
            taxItemRepository, merchantProfileRepository, payOrderRepository, paymentGateway, properties, redisCache);

    @Test
    void precreateShouldCreatePendingOrderCallGatewayAndReturnPayParams() {
        properties.getWechat().setAppId("wx-app");
        properties.getWechat().setAppKey("wx-key");
        properties.getPay().setNotifyUrl("https://notify.example.com/pii/pay/notify");
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.of(qrcode()));
        when(relationRepository.findByQrcodeIdAndTaxItemId(10L, 20L)).thenReturn(Optional.of(relation()));
        when(taxItemRepository.findById(20L)).thenReturn(Optional.of(taxItem()));
        when(merchantProfileRepository.findById(100L)).thenReturn(Optional.of(merchant()));
        when(payOrderRepository.insert(any(PayOrder.class))).thenReturn(500L);
        when(paymentGateway.preCreate(any(PreCreateRequest.class))).thenReturn(preCreateResponse());

        var result = service.precreate(command());

        assertThat(result.getAppId()).isEqualTo("wx-app");
        assertThat(result.getOutTradeNo()).startsWith("PII");
        assertThat(result.getOrderToken()).isNotBlank();
        assertThat(result.getPrepayId()).isEqualTo("PREPAY001");
        assertThat(result.getTimeStamp()).isEqualTo("1783000000");
        assertThat(result.getPaySign()).isEqualTo("PAY_SIGN");

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderRepository).insert(orderCaptor.capture());
        PayOrder order = orderCaptor.getValue();
        assertThat(order.getMerchantId()).isEqualTo(100L);
        assertThat(order.getQrcodeId()).isEqualTo(10L);
        assertThat(order.getTaxItemId()).isEqualTo(20L);
        assertThat(order.getAmount()).isEqualTo(8800L);
        assertThat(order.getPayStatus()).isEqualTo("PENDING");
        assertThat(order.getInvoiceStatus()).isEqualTo("NOT_ISSUED");
        assertThat(order.getBuyerName()).isEqualTo("张三");
        assertThat(order.getBuyerTaxCode()).isEqualTo("91330000X");
        assertThat(order.getClientIp()).isEqualTo("127.0.0.1");

        ArgumentCaptor<PreCreateRequest> gatewayCaptor = ArgumentCaptor.forClass(PreCreateRequest.class);
        verify(paymentGateway).preCreate(gatewayCaptor.capture());
        PreCreateRequest request = gatewayCaptor.getValue();
        assertThat(request.getAppId()).isEqualTo("wx-app");
        assertThat(request.getAppKey()).isEqualTo("wx-key");
        assertThat(request.getMerchantId()).isEqualTo("MID");
        assertThat(request.getTerminalId()).isEqualTo("TID");
        assertThat(request.getTotalAmount()).isEqualTo(8800L);
        assertThat(request.getNotifyUrl()).isEqualTo("https://notify.example.com/pii/pay/notify");
        assertThat(request.getSignKey()).isEqualTo("PAY_KEY");
        assertThat(request.getOrderDesc()).isEqualTo("餐饮服务");

        verify(redisCache).setCacheObject(eq("pii:order:token:" + result.getOutTradeNo()),
                eq(result.getOrderToken()), eq(30), eq(TimeUnit.MINUTES));
    }

    @Test
    void precreateShouldRejectUnboundTaxItem() {
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.of(qrcode()));
        when(relationRepository.findByQrcodeIdAndTaxItemId(10L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.precreate(command()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("税目未绑定");
    }

    @Test
    void precreateShouldRejectInvalidQrcodeCode() {
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.precreate(command()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("二维码无效");

        verify(paymentGateway, never()).preCreate(any(PreCreateRequest.class));
        verify(payOrderRepository, never()).insert(any(PayOrder.class));
    }

    @Test
    void precreateShouldRejectDisabledQrcode() {
        PayQrcode qrcode = qrcode();
        qrcode.setStatus(0);
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.of(qrcode));

        assertThatThrownBy(() -> service.precreate(command()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("二维码已停用");

        verify(paymentGateway, never()).preCreate(any(PreCreateRequest.class));
    }

    @Test
    void precreateShouldRejectExpiredQrcode() {
        PayQrcode qrcode = qrcode();
        qrcode.setExpireTime(LocalDateTime.now().minusMinutes(1));
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.of(qrcode));

        assertThatThrownBy(() -> service.precreate(command()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("二维码已过期");

        verify(paymentGateway, never()).preCreate(any(PreCreateRequest.class));
    }

    @Test
    void precreateShouldRejectDisabledTaxItem() {
        TaxItem taxItem = taxItem();
        taxItem.setStatus(0);
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.of(qrcode()));
        when(relationRepository.findByQrcodeIdAndTaxItemId(10L, 20L)).thenReturn(Optional.of(relation()));
        when(taxItemRepository.findById(20L)).thenReturn(Optional.of(taxItem));

        assertThatThrownBy(() -> service.precreate(command()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("税目已停用");

        verify(paymentGateway, never()).preCreate(any(PreCreateRequest.class));
    }

    private PrecreatePayCommand command() {
        return new PrecreatePayCommand("QR001", 20L, 8800L, "张三", "91330000X",
                "buyer@example.com", "13800000000", null, "127.0.0.1");
    }

    private PayQrcode qrcode() {
        PayQrcode qrcode = new PayQrcode();
        qrcode.setId(10L);
        qrcode.setMerchantId(100L);
        qrcode.setQrcodeCode("QR001");
        qrcode.setStatus(1);
        return qrcode;
    }

    private PayQrcodeTaxItem relation() {
        PayQrcodeTaxItem relation = new PayQrcodeTaxItem();
        relation.setQrcodeId(10L);
        relation.setTaxItemId(20L);
        relation.setDefaultAmount(9900L);
        return relation;
    }

    private TaxItem taxItem() {
        TaxItem taxItem = new TaxItem();
        taxItem.setId(20L);
        taxItem.setName("餐饮服务");
        taxItem.setTaxItemCode("3070401000000000000");
        taxItem.setTaxRate(new BigDecimal("6.00"));
        taxItem.setStatus(1);
        return taxItem;
    }

    private MerchantProfile merchant() {
        MerchantProfile merchant = new MerchantProfile();
        merchant.setId(100L);
        merchant.setMerchantName("测试商户");
        merchant.setUmsMerchantId("MID");
        merchant.setUmsTerminalId("TID");
        merchant.setUmsPaySignKeyEnc("PAY_KEY");
        merchant.setStatus(1);
        return merchant;
    }

    private PreCreateResponse preCreateResponse() {
        PreCreateResponse response = new PreCreateResponse();
        response.setPrepayId("PREPAY001");
        response.setTimestamp("1783000000");
        response.setNonceStr("NONCE");
        response.setPackageStr("prepay_id=PREPAY001");
        response.setSignType("SHA256");
        response.setJsApiPaySign("PAY_SIGN");
        return response;
    }
}
