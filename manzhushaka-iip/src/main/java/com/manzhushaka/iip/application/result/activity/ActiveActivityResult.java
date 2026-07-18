package com.manzhushaka.iip.application.result.activity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 生效活动条目结果（小程序端：多活动并行展示，含地域维度与参与规模）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ActiveActivityResult(Long activityId, String activityName, Date startTime, Date endTime,
        BigDecimal pointsRatio, String city, String regionType, String regionName, Integer priority,
        Integer merchantCount, Integer couponCount)
{
    @Override
    public String toString()
    {
        return "ActiveActivityResult[activityId=" + activityId + ", activityName=" + activityName
                + ", priority=" + priority + "]";
    }
}
