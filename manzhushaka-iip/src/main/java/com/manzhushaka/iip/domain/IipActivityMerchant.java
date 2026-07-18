package com.manzhushaka.iip.domain;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 活动商户关联对象 iip_activity_merchant
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipActivityMerchant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 活动ID */
    private Long activityId;

    /** 商户ID */
    private Long merchantId;

    /** 状态（0正常 1停用） */
    private String status;

    /** 商户编号（非表字段，join iip_merchant 展示用） */
    private String merchantNo;

    /** 商户名称（非表字段，join iip_merchant 展示用） */
    private String merchantName;

    /** 商户类别（非表字段，join iip_merchant 展示用） */
    private String category;

    /** 商户状态（非表字段，join iip_merchant 展示用） */
    private String merchantStatus;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotNull(message = "活动ID不能为空")
    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    @NotNull(message = "商户ID不能为空")
    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getMerchantNo()
    {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo)
    {
        this.merchantNo = merchantNo;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getMerchantStatus()
    {
        return merchantStatus;
    }

    public void setMerchantStatus(String merchantStatus)
    {
        this.merchantStatus = merchantStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("activityId", getActivityId())
            .append("merchantId", getMerchantId())
            .append("status", getStatus())
            .append("merchantNo", getMerchantNo())
            .append("merchantName", getMerchantName())
            .append("category", getCategory())
            .append("merchantStatus", getMerchantStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
