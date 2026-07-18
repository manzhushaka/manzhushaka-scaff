package com.manzhushaka.iip.application.result.activity;

import java.util.Date;

/**
 * 活动券配置结果（join iip_coupon 携带券名称/积分价/封面/库存）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ActivityCouponResult(Long id, Long activityId, Long couponId, String couponName, Integer pointsCost,
        String coverImage, Integer totalStock, Integer remainStock, Integer issueLimit, Integer issuedCount,
        Date createTime, String remark)
{
    @Override
    public String toString()
    {
        return "ActivityCouponResult[id=" + id + ", activityId=" + activityId
                + ", couponId=" + couponId + ", couponName=" + couponName + "]";
    }
}
