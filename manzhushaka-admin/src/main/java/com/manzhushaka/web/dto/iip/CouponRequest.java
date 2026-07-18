package com.manzhushaka.web.dto.iip;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 券定义请求（管理端，列表筛选与新增/修改共用）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class CouponRequest
{
    private Long couponId;

    @NotBlank(message = "券名称不能为空")
    @Size(max = 128, message = "券名称不能超过128个字符")
    private String couponName;

    @NotBlank(message = "券类型不能为空")
    private String couponType;

    private String coverImage;

    @Size(max = 128, message = "适用对象不能超过128个字符")
    private String targetName;

    @NotNull(message = "兑换所需积分不能为空")
    private Integer pointsCost;

    @NotNull(message = "总库存不能为空")
    private Integer totalStock;

    private Integer remainStock;

    private Integer perMemberLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date exchangeStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date exchangeEndTime;

    @NotBlank(message = "有效期类型不能为空")
    private String validType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validEndTime;

    private Integer validDays;

    private BigDecimal thresholdAmount;

    private BigDecimal discountAmount;

    private Long merchantId;

    private String useDesc;

    @NotBlank(message = "状态不能为空")
    private String status;

    private Integer sort;

    private String remark;

    @NotBlank(message = "券品类不能为空")
    private String category;

    @NotBlank(message = "赞助方类型不能为空")
    private String sponsorType;

    @Size(max = 128, message = "赞助方名称不能超过128个字符")
    private String sponsorName;

    public Long getCouponId()
    {
        return couponId;
    }

    public void setCouponId(Long couponId)
    {
        this.couponId = couponId;
    }

    public String getCouponName()
    {
        return couponName;
    }

    public void setCouponName(String couponName)
    {
        this.couponName = couponName;
    }

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

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
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
}
