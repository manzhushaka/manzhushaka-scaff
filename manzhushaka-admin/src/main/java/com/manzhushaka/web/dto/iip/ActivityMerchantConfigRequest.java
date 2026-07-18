package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.NotNull;

/**
 * 活动商户配置请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class ActivityMerchantConfigRequest
{
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }
}
