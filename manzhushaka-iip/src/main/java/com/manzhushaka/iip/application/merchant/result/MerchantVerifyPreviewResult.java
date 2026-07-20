package com.manzhushaka.iip.application.merchant.result;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 小程序商户核销预检结果。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
public record MerchantVerifyPreviewResult(Long recordId, String couponName, String couponType,
        BigDecimal thresholdAmount, BigDecimal discountAmount, String targetName, String useDesc,
        Date validStartTime, Date validEndTime, String holderDisplayName, boolean identityCheckSuggested)
{
    @Override
    public String toString()
    {
        return "MerchantVerifyPreviewResult[recordId=" + recordId + ", couponName=" + couponName + "]";
    }
}
