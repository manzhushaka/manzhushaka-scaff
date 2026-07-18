package com.manzhushaka.iip.domain;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 活动券配置对象 iip_activity_coupon
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipActivityCoupon extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 活动ID */
    private Long activityId;

    /** 券ID */
    private Long couponId;

    /** 发行上限（-1不限） */
    private Integer issueLimit;

    /** 已发行数量 */
    private Integer issuedCount;

    /** 券名称（非表字段，join iip_coupon 展示用） */
    private String couponName;

    /** 兑换所需积分（非表字段，join iip_coupon 展示用） */
    private Integer pointsCost;

    /** 封面图片（非表字段，join iip_coupon 展示用） */
    private String coverImage;

    /** 总库存（非表字段，join iip_coupon 展示用） */
    private Integer totalStock;

    /** 剩余库存（非表字段，join iip_coupon 展示用） */
    private Integer remainStock;

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

    @NotNull(message = "券ID不能为空")
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

    public Integer getIssuedCount()
    {
        return issuedCount;
    }

    public void setIssuedCount(Integer issuedCount)
    {
        this.issuedCount = issuedCount;
    }

    public String getCouponName()
    {
        return couponName;
    }

    public void setCouponName(String couponName)
    {
        this.couponName = couponName;
    }

    public Integer getPointsCost()
    {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost)
    {
        this.pointsCost = pointsCost;
    }

    public String getCoverImage()
    {
        return coverImage;
    }

    public void setCoverImage(String coverImage)
    {
        this.coverImage = coverImage;
    }

    public Integer getTotalStock()
    {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock)
    {
        this.totalStock = totalStock;
    }

    public Integer getRemainStock()
    {
        return remainStock;
    }

    public void setRemainStock(Integer remainStock)
    {
        this.remainStock = remainStock;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("activityId", getActivityId())
            .append("couponId", getCouponId())
            .append("issueLimit", getIssueLimit())
            .append("issuedCount", getIssuedCount())
            .append("couponName", getCouponName())
            .append("pointsCost", getPointsCost())
            .append("coverImage", getCoverImage())
            .append("totalStock", getTotalStock())
            .append("remainStock", getRemainStock())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
