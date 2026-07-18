package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.NotNull;

/**
 * 活动券配置请求（新增携带活动ID与券ID，修改仅需主键ID与发行上限）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class ActivityCouponConfigRequest
{
    private Long id;

    private Long activityId;

    private Long couponId;

    @NotNull(message = "发行上限不能为空")
    private Integer issueLimit;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public Long getCouponId()
    {
        return couponId;
    }

    public void setCouponId(Long couponId)
    {
        this.couponId = couponId;
    }

    public Integer getIssueLimit()
    {
        return issueLimit;
    }

    public void setIssueLimit(Integer issueLimit)
    {
        this.issueLimit = issueLimit;
    }
}
