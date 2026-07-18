package com.manzhushaka.iip.application.command;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 活动保存命令（新增与修改共用，活动编号由服务端生成不可传入）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveActivityCommand(Long activityId, String activityName, String coverImage, String description,
        Date startTime, Date endTime, BigDecimal pointsRatio, Integer merchantLimit, Integer couponQuota,
        String city, String regionType, String regionName, Integer priority, String status, String remark)
{
    @Override
    public String toString()
    {
        return "SaveActivityCommand[activityId=" + activityId + ", activityName=" + activityName
                + ", startTime=" + startTime + ", endTime=" + endTime + ", status=" + status + "]";
    }
}
