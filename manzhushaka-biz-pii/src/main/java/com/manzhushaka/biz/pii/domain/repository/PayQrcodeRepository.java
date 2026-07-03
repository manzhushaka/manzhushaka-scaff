package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.PayQrcode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PayQrcodeRepository {
    Long insert(PayQrcode qrcode);
    int updateById(PayQrcode qrcode);
    int deleteById(Long id);
    int updateStatus(Long id, Integer status);
    Optional<PayQrcode> findById(Long id);
    Optional<PayQrcode> findByCode(String qrcodeCode);
    List<PayQrcode> findByMerchantId(Long merchantId);
    List<PayQrcode> findExpiredBefore(LocalDateTime time, int limit);
}
