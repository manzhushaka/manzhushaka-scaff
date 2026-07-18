package com.manzhushaka.iip.application.coupon.result;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 兑换成功返回的券实例结果（小程序端，含核销码）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ExchangeResult(
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
        Long activityId)
{
    @Override
    public String toString()
    {
        return "ExchangeResult[recordId=" + recordId + ", couponId=" + couponId
                + ", verifyCode=" + verifyCode + "]";
    }
}
