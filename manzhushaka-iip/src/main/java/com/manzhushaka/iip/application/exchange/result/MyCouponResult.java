package com.manzhushaka.iip.application.exchange.result;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 我的券条目结果（小程序端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MyCouponResult(
        Long recordId,
        Long couponId,
        String couponName,
        String couponType,
        Integer pointsCost,
        String verifyCode,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date exchangeTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validStartTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validEndTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date verifyTime,
        Long activityId,
        String category,
        String sponsorType,
        String sponsorName)
{
    @Override
    public String toString()
    {
        return "MyCouponResult[recordId=" + recordId + ", couponName=" + couponName
                + ", status=" + status + "]";
    }
}
