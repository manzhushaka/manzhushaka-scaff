package com.manzhushaka.iip.application.command;

/**
 * 活动商户配置命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ActivityMerchantCommand(Long activityId, Long merchantId)
{
    @Override
    public String toString()
    {
        return "ActivityMerchantCommand[activityId=" + activityId + ", merchantId=" + merchantId + "]";
    }
}
