package com.manzhushaka.iip.application.merchant.result;

import java.util.Date;

/**
 * 小程序商户核销记录行。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record VerifyRecordResult(Long recordId, String couponName, String couponType, Long memberId,
        Integer pointsCost, String verifyCode, Date verifyTime, String verifyBy)
{
    @Override
    public String toString()
    {
        return "VerifyRecordResult[recordId=" + recordId + ", couponName=" + couponName
                + ", verifyTime=" + verifyTime + "]";
    }
}
