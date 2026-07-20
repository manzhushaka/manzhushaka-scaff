package com.manzhushaka.iip.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 券实例对象（兑换记录） iip_coupon_record
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipCouponRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 券ID */
    private Long couponId;

    /** 券名称（冗余） */
    private String couponName;

    /** 券类型（冗余） */
    private String couponType;

    /** 兑换用户ID */
    private Long memberId;

    /** 兑换消耗积分 */
    private Integer pointsCost;

    /** 核销码 */
    private String verifyCode;

    /** 状态（0未使用 1已使用 2已过期 3已作废） */
    private String status;

    /** 兑换时间 */
    private Date exchangeTime;

    /** 有效期开始 */
    private Date validStartTime;

    /** 有效期结束 */
    private Date validEndTime;

    /** 核销时间 */
    private Date verifyTime;

    /** 核销商户ID */
    private Long verifyMerchantId;

    /** 核销操作人 */
    private String verifyBy;

    /** 来源活动ID */
    private Long activityId;

    /** 作废时间 */
    private Date voidTime;

    /** 作废操作人 */
    private String voidBy;

    /** 作废原因 */
    private String voidReason;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
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

    @NotNull(message = "兑换用户ID不能为空")
    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Integer getPointsCost()
    {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost)
    {
        this.pointsCost = pointsCost;
    }

    @NotBlank(message = "核销码不能为空")
    @Size(min = 0, max = 32, message = "核销码不能超过32个字符")
    public String getVerifyCode()
    {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode)
    {
        this.verifyCode = verifyCode;
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

    public Date getExchangeTime()
    {
        return exchangeTime;
    }

    public void setExchangeTime(Date exchangeTime)
    {
        this.exchangeTime = exchangeTime;
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

    public Date getVerifyTime()
    {
        return verifyTime;
    }

    public void setVerifyTime(Date verifyTime)
    {
        this.verifyTime = verifyTime;
    }

    public Long getVerifyMerchantId()
    {
        return verifyMerchantId;
    }

    public void setVerifyMerchantId(Long verifyMerchantId)
    {
        this.verifyMerchantId = verifyMerchantId;
    }

    public String getVerifyBy()
    {
        return verifyBy;
    }

    public void setVerifyBy(String verifyBy)
    {
        this.verifyBy = verifyBy;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public Date getVoidTime()
    {
        return voidTime;
    }

    public void setVoidTime(Date voidTime)
    {
        this.voidTime = voidTime;
    }

    public String getVoidBy()
    {
        return voidBy;
    }

    public void setVoidBy(String voidBy)
    {
        this.voidBy = voidBy;
    }

    public String getVoidReason()
    {
        return voidReason;
    }

    public void setVoidReason(String voidReason)
    {
        this.voidReason = voidReason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("couponId", getCouponId())
            .append("couponName", getCouponName())
            .append("couponType", getCouponType())
            .append("memberId", getMemberId())
            .append("pointsCost", getPointsCost())
            .append("verifyCode", getVerifyCode())
            .append("status", getStatus())
            .append("exchangeTime", getExchangeTime())
            .append("validStartTime", getValidStartTime())
            .append("validEndTime", getValidEndTime())
            .append("verifyTime", getVerifyTime())
            .append("verifyMerchantId", getVerifyMerchantId())
            .append("verifyBy", getVerifyBy())
            .append("activityId", getActivityId())
            .append("voidTime", getVoidTime())
            .append("voidBy", getVoidBy())
            .append("voidReason", getVoidReason())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
