package com.manzhushaka.iip.application.result.activity;

/**
 * 当前活动券项结果（小程序端展示）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record CurrentActivityCouponResult(Long couponId, String couponName, Integer pointsCost, String coverImage,
        Integer issueLimit, Integer issuedCount)
{
    @Override
    public String toString()
    {
        return "CurrentActivityCouponResult[couponId=" + couponId + ", couponName=" + couponName + "]";
    }
}
