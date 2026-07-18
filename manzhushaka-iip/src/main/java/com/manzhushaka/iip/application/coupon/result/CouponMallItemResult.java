package com.manzhushaka.iip.application.coupon.result;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 积分商城券条目结果（小程序端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record CouponMallItemResult(
        Long couponId,
        String couponName,
        String couponType,
        String coverImage,
        String targetName,
        Integer pointsCost,
        Integer remainStock,
        Integer perMemberLimit,
        String validType,
        Integer validDays,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validStartTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validEndTime,
        String useDesc,
        String category,
        String sponsorType,
        String sponsorName)
{
    @Override
    public String toString()
    {
        return "CouponMallItemResult[couponId=" + couponId + ", couponName=" + couponName
                + ", pointsCost=" + pointsCost + ", remainStock=" + remainStock + "]";
    }
}
