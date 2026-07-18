package com.manzhushaka.iip.domain;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 券定义对象 iip_coupon
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipCoupon extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 券ID */
    private Long couponId;

    /** 券名称 */
    private String couponName;

    /** 券类型（ticket门票 virtual虚拟物品 full_reduction满减 discount折扣） */
    private String couponType;

    /** 封面图片 */
    private String coverImage;

    /** 适用对象（如景区名） */
    private String targetName;

    /** 兑换所需积分 */
    private Integer pointsCost;

    /** 总库存（-1不限） */
    private Integer totalStock;

    /** 剩余库存 */
    private Integer remainStock;

    /** 每人限兑数量（-1不限） */
    private Integer perMemberLimit;

    /** 兑换开始时间 */
    private Date exchangeStartTime;

    /** 兑换结束时间 */
    private Date exchangeEndTime;

    /** 有效期类型（fixed固定区间 days领取后N天） */
    private String validType;

    /** 有效期开始 */
    private Date validStartTime;

    /** 有效期结束 */
    private Date validEndTime;

    /** 领取后有效天数 */
    private Integer validDays;

    /** 满减门槛金额 */
    private BigDecimal thresholdAmount;

    /** 满减面额 */
    private BigDecimal discountAmount;

    /** 指定可用商户ID（null通用） */
    private Long merchantId;

    /** 使用说明 */
    private String useDesc;

    /** 状态（0上架 1下架） */
    private String status;

    /** 显示顺序 */
    private Integer sort;

    /** 券品类（general通用 scenic_ticket景区门票 hotel酒店券 dining餐饮券 flight_package机票+权益包 duty_free免税周边） */
    private String category;

    /** 赞助方类型（platform平台 bank银行 merchant商户） */
    private String sponsorType;

    /** 赞助方名称（赞助方类型为银行/商户时必填） */
    private String sponsorName;

    public Long getCouponId()
    {
        return couponId;
    }

    public void setCouponId(Long couponId)
    {
        this.couponId = couponId;
    }

    @NotBlank(message = "券名称不能为空")
    @Size(min = 0, max = 128, message = "券名称不能超过128个字符")
    public String getCouponName()
    {
        return couponName;
    }

    public void setCouponName(String couponName)
    {
        this.couponName = couponName;
    }

    @NotBlank(message = "券类型不能为空")
    public String getCouponType()
    {
        return couponType;
    }

    public void setCouponType(String couponType)
    {
        this.couponType = couponType;
    }

    public String getCoverImage()
    {
        return coverImage;
    }

    public void setCoverImage(String coverImage)
    {
        this.coverImage = coverImage;
    }

    public String getTargetName()
    {
        return targetName;
    }

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    @NotNull(message = "兑换所需积分不能为空")
    public Integer getPointsCost()
    {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost)
    {
        this.pointsCost = pointsCost;
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

    public Integer getPerMemberLimit()
    {
        return perMemberLimit;
    }

    public void setPerMemberLimit(Integer perMemberLimit)
    {
        this.perMemberLimit = perMemberLimit;
    }

    public Date getExchangeStartTime()
    {
        return exchangeStartTime;
    }

    public void setExchangeStartTime(Date exchangeStartTime)
    {
        this.exchangeStartTime = exchangeStartTime;
    }

    public Date getExchangeEndTime()
    {
        return exchangeEndTime;
    }

    public void setExchangeEndTime(Date exchangeEndTime)
    {
        this.exchangeEndTime = exchangeEndTime;
    }

    public String getValidType()
    {
        return validType;
    }

    public void setValidType(String validType)
    {
        this.validType = validType;
    }

    public Date getValidStartTime()
    {
        return validStartTime;
    }

    public void setValidStartTime(Date validStartTime)
    {
        this.validStartTime = validStartTime;
    }

    public Date getValidEndTime()
    {
        return validEndTime;
    }

    public void setValidEndTime(Date validEndTime)
    {
        this.validEndTime = validEndTime;
    }

    public Integer getValidDays()
    {
        return validDays;
    }

    public void setValidDays(Integer validDays)
    {
        this.validDays = validDays;
    }

    public BigDecimal getThresholdAmount()
    {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount)
    {
        this.thresholdAmount = thresholdAmount;
    }

    public BigDecimal getDiscountAmount()
    {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount)
    {
        this.discountAmount = discountAmount;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getUseDesc()
    {
        return useDesc;
    }

    public void setUseDesc(String useDesc)
    {
        this.useDesc = useDesc;
    }

    @NotBlank(message = "状态不能为空")
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getSponsorType()
    {
        return sponsorType;
    }

    public void setSponsorType(String sponsorType)
    {
        this.sponsorType = sponsorType;
    }

    public String getSponsorName()
    {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName)
    {
        this.sponsorName = sponsorName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("couponId", getCouponId())
            .append("couponName", getCouponName())
            .append("couponType", getCouponType())
            .append("targetName", getTargetName())
            .append("pointsCost", getPointsCost())
            .append("totalStock", getTotalStock())
            .append("remainStock", getRemainStock())
            .append("perMemberLimit", getPerMemberLimit())
            .append("exchangeStartTime", getExchangeStartTime())
            .append("exchangeEndTime", getExchangeEndTime())
            .append("validType", getValidType())
            .append("validStartTime", getValidStartTime())
            .append("validEndTime", getValidEndTime())
            .append("validDays", getValidDays())
            .append("thresholdAmount", getThresholdAmount())
            .append("discountAmount", getDiscountAmount())
            .append("merchantId", getMerchantId())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("category", getCategory())
            .append("sponsorType", getSponsorType())
            .append("sponsorName", getSponsorName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
