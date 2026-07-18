package com.manzhushaka.iip.application.command;

/**
 * 活动券配置保存命令（新增携带活动ID与券ID，修改仅需主键ID与发行上限）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveActivityCouponCommand(Long id, Long activityId, Long couponId, Integer issueLimit)
{
    @Override
    public String toString()
    {
        return "SaveActivityCouponCommand[id=" + id + ", activityId=" + activityId
                + ", couponId=" + couponId + ", issueLimit=" + issueLimit + "]";
    }
}
