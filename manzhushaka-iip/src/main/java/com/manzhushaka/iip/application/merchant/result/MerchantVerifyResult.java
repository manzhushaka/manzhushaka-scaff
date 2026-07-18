package com.manzhushaka.iip.application.merchant.result;

import java.util.Date;

/**
 * 小程序核销成功结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MerchantVerifyResult(Long recordId, String couponName, String couponType, Integer pointsCost,
        String verifyCode, Date verifyTime)
{
    @Override
    public String toString()
    {
        return "MerchantVerifyResult[recordId=" + recordId + ", couponName=" + couponName + "]";
    }
}
