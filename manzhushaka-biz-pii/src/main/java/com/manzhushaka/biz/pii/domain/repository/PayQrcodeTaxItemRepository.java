package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;

import java.util.List;
import java.util.Optional;

public interface PayQrcodeTaxItemRepository {
    Long insert(PayQrcodeTaxItem relation);
    int deleteByQrcodeId(Long qrcodeId);
    int deleteByQrcodeIdAndTaxItemId(Long qrcodeId, Long taxItemId);
    Optional<PayQrcodeTaxItem> findByQrcodeIdAndTaxItemId(Long qrcodeId, Long taxItemId);
    List<PayQrcodeTaxItem> findByQrcodeId(Long qrcodeId);
}
