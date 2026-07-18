package com.manzhushaka.iip.application.result.activity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 当前生效活动结果（小程序端：活动全字段 + 参与商户数 + 配置券数 + 券列表）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record CurrentActivityResult(Long activityId, String activityNo, String activityName, String coverImage,
        String description, Date startTime, Date endTime, BigDecimal pointsRatio, Integer merchantLimit,
        Integer couponQuota, String status, String city, String regionType, String regionName, Integer priority,
        String createBy, Date createTime, String updateBy, Date updateTime,
        String remark, Integer merchantCount, Integer couponCount, List<CurrentActivityCouponResult> coupons)
{
    @Override
    public String toString()
    {
        return "CurrentActivityResult[activityId=" + activityId + ", activityNo=" + activityNo
                + ", activityName=" + activityName + ", merchantCount=" + merchantCount
                + ", couponCount=" + couponCount + "]";
    }
}
