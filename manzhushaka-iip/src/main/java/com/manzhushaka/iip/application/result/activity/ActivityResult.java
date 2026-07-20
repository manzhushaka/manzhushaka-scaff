package com.manzhushaka.iip.application.result.activity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 活动结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ActivityResult(Long activityId, String activityNo, String activityName, String coverImage,
        String description, Date startTime, Date endTime, BigDecimal pointsRatio, Integer merchantLimit,
        Integer couponQuota, String city, String regionType, String regionName, Integer priority,
        String status, String createBy, Date createTime, String updateBy, Date updateTime,
        String remark, Integer singleInvoiceCap, Integer monthlyMemberCap, String merchantScope)
{
    @Override
    public String toString()
    {
        return "ActivityResult[activityId=" + activityId + ", activityNo=" + activityNo
                + ", activityName=" + activityName + ", status=" + status + "]";
    }
}
