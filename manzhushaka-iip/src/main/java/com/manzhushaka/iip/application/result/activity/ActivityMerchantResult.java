package com.manzhushaka.iip.application.result.activity;

import java.util.Date;

/**
 * 活动商户配置结果（join iip_merchant 携带商户编号/名称/类别/状态）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ActivityMerchantResult(Long id, Long activityId, Long merchantId, String merchantNo,
        String merchantName, String category, String merchantStatus, String status, Date createTime, String remark)
{
    @Override
    public String toString()
    {
        return "ActivityMerchantResult[id=" + id + ", activityId=" + activityId
                + ", merchantId=" + merchantId + ", merchantName=" + merchantName + "]";
    }
}
