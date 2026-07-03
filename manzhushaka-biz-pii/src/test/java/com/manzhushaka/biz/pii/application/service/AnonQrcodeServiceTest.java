package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.service.impl.AnonQrcodeServiceImpl;
import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeRepository;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeTaxItemRepository;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnonQrcodeServiceTest {

    private final PayQrcodeRepository qrcodeRepository = mock(PayQrcodeRepository.class);
    private final PayQrcodeTaxItemRepository relationRepository = mock(PayQrcodeTaxItemRepository.class);
    private final TaxItemRepository taxItemRepository = mock(TaxItemRepository.class);
    private final PiiProperties properties = new PiiProperties();
    private final AnonQrcodeService service = new AnonQrcodeServiceImpl(
            qrcodeRepository, relationRepository, taxItemRepository, properties);

    @Test
    void getConfigShouldThrowWhenQrcodeNotFound() {
        when(qrcodeRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConfig("NOPE"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("二维码无效");
    }

    @Test
    void getConfigShouldThrowWhenQrcodeDisabledOrExpired() {
        PayQrcode disabled = qrcode(1);
        disabled.setStatus(0);
        when(qrcodeRepository.findByCode("DISABLED")).thenReturn(Optional.of(disabled));
        PayQrcode expired = qrcode(1);
        expired.setExpireTime(LocalDateTime.now().minusMinutes(1));
        when(qrcodeRepository.findByCode("EXPIRED")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.getConfig("DISABLED"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("二维码已停用");
        assertThatThrownBy(() -> service.getConfig("EXPIRED"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("二维码已过期");
    }

    @Test
    void getConfigShouldReturnBoundTaxItemsAndAppId() {
        properties.getWechat().setAppId("wx-app");
        PayQrcode qrcode = qrcode(1);
        PayQrcodeTaxItem relation = new PayQrcodeTaxItem();
        relation.setTaxItemId(20L);
        relation.setDefaultAmount(9900L);
        TaxItem taxItem = new TaxItem();
        taxItem.setId(20L);
        taxItem.setName("餐饮服务");
        taxItem.setTaxItemCode("3070401000000000000");
        taxItem.setTaxRate(new BigDecimal("6.00"));
        taxItem.setStatus(1);
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.of(qrcode));
        when(relationRepository.findByQrcodeId(10L)).thenReturn(List.of(relation));
        when(taxItemRepository.findByIds(List.of(20L))).thenReturn(List.of(taxItem));

        var result = service.getConfig("QR001");

        assertThat(result.getAppId()).isEqualTo("wx-app");
        assertThat(result.getQrcodeCode()).isEqualTo("QR001");
        assertThat(result.getTaxItems()).hasSize(1);
        assertThat(result.getTaxItems().get(0).getDefaultAmount()).isEqualTo(9900L);
        assertThat(result.getTaxItems().get(0).getName()).isEqualTo("餐饮服务");
    }

    private PayQrcode qrcode(Integer status) {
        PayQrcode qrcode = new PayQrcode();
        qrcode.setId(10L);
        qrcode.setMerchantId(100L);
        qrcode.setQrcodeCode("QR001");
        qrcode.setName("门店收款码");
        qrcode.setStatus(status);
        return qrcode;
    }
}
