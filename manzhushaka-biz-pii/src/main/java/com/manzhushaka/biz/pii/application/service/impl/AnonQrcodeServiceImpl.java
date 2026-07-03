package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.result.QrcodeConfigResult;
import com.manzhushaka.biz.pii.application.result.QrcodeConfigTaxItemResult;
import com.manzhushaka.biz.pii.application.service.AnonQrcodeService;
import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeRepository;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeTaxItemRepository;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnonQrcodeServiceImpl implements AnonQrcodeService {

    private final PayQrcodeRepository qrcodeRepository;
    private final PayQrcodeTaxItemRepository relationRepository;
    private final TaxItemRepository taxItemRepository;
    private final PiiProperties properties;

    public AnonQrcodeServiceImpl(PayQrcodeRepository qrcodeRepository,
                                 PayQrcodeTaxItemRepository relationRepository,
                                 TaxItemRepository taxItemRepository,
                                 PiiProperties properties) {
        this.qrcodeRepository = qrcodeRepository;
        this.relationRepository = relationRepository;
        this.taxItemRepository = taxItemRepository;
        this.properties = properties;
    }

    @Override
    public QrcodeConfigResult getConfig(String code) {
        PayQrcode qrcode = qrcodeRepository.findByCode(code)
                .orElseThrow(() -> new ServiceException("二维码无效", 10002));
        if (!Integer.valueOf(1).equals(qrcode.getStatus())) {
            throw new ServiceException("二维码已停用", 10002);
        }
        if (qrcode.getExpireTime() != null && qrcode.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("二维码已过期", 10003);
        }
        List<PayQrcodeTaxItem> relations = relationRepository.findByQrcodeId(qrcode.getId());
        List<Long> taxItemIds = relations.stream().map(PayQrcodeTaxItem::getTaxItemId).collect(Collectors.toList());
        Map<Long, Long> defaultAmountMap = relations.stream().collect(Collectors.toMap(
                PayQrcodeTaxItem::getTaxItemId,
                relation -> relation.getDefaultAmount() == null ? 0L : relation.getDefaultAmount(),
                (first, second) -> first,
                LinkedHashMap::new));
        List<QrcodeConfigTaxItemResult> taxItems = taxItemRepository.findByIds(taxItemIds).stream()
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .map(item -> toTaxItemResult(item, defaultAmountMap.get(item.getId())))
                .collect(Collectors.toList());

        QrcodeConfigResult result = new QrcodeConfigResult();
        result.setQrcodeId(qrcode.getId());
        result.setMerchantId(qrcode.getMerchantId());
        result.setQrcodeCode(qrcode.getQrcodeCode());
        result.setName(qrcode.getName());
        result.setAppId(properties.getWechat().getAppId());
        result.setTaxItems(taxItems);
        return result;
    }

    private QrcodeConfigTaxItemResult toTaxItemResult(TaxItem taxItem, Long defaultAmount) {
        QrcodeConfigTaxItemResult result = new QrcodeConfigTaxItemResult();
        result.setId(taxItem.getId());
        result.setTaxItemCode(taxItem.getTaxItemCode());
        result.setName(taxItem.getName());
        result.setTaxRate(taxItem.getTaxRate());
        result.setDefaultAmount(defaultAmount);
        return result;
    }
}
