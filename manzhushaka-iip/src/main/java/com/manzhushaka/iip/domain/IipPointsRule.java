package com.manzhushaka.iip.domain;

import com.manzhushaka.common.core.domain.BaseEntity;
import com.manzhushaka.common.utils.StringUtils;

/**
 * 活动积分规则对象 iip_points_rule。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
public class IipPointsRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 活动ID */
    private Long activityId;

    /** 单张发票积分上限（-1不限） */
    private Integer singleInvoiceCap;

    /** 活动内每人每月积分上限（-1不限） */
    private Integer monthlyMemberCap;

    /** 商户范围（all全部 whitelist活动商户白名单） */
    private String merchantScope;

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public Integer getSingleInvoiceCap()
    {
        return singleInvoiceCap;
    }

    public void setSingleInvoiceCap(Integer singleInvoiceCap)
    {
        this.singleInvoiceCap = singleInvoiceCap;
    }

    public Integer getMonthlyMemberCap()
    {
        return monthlyMemberCap;
    }

    public void setMonthlyMemberCap(Integer monthlyMemberCap)
    {
        this.monthlyMemberCap = monthlyMemberCap;
    }

    public String getMerchantScope()
    {
        return merchantScope;
    }

    public void setMerchantScope(String merchantScope)
    {
        this.merchantScope = merchantScope;
    }

    @Override
    public String toString()
    {
        return StringUtils.format("IipPointsRule[ruleId={}, activityId={}, singleInvoiceCap={}, "
                + "monthlyMemberCap={}, merchantScope={}]", ruleId, activityId, singleInvoiceCap,
                monthlyMemberCap, merchantScope);
    }
}
