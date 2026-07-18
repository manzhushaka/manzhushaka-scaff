package com.manzhushaka.iip.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 积分流水对象 iip_points_record
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipPointsRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流水ID */
    private Long recordId;

    /** 用户ID */
    private Long memberId;

    /** 变动类型（earn获得 consume消费 expire过期 adjust调整） */
    private String changeType;

    /** 变动数量（正数） */
    private Integer points;

    /** 变动后可用余额 */
    private Integer balanceAfter;

    /** 业务来源（invoice_audit/coupon_exchange/admin_adjust/point_expire） */
    private String bizType;

    /** 业务单据ID（幂等用） */
    private String bizId;

    /** 该批剩余未消耗（仅earn） */
    private Integer remaining;

    /** 批次过期时间（仅earn） */
    private Date expireTime;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    @NotNull(message = "用户ID不能为空")
    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    @NotBlank(message = "变动类型不能为空")
    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    @NotNull(message = "变动数量不能为空")
    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    @NotNull(message = "变动后余额不能为空")
    public Integer getBalanceAfter()
    {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter)
    {
        this.balanceAfter = balanceAfter;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public String getBizId()
    {
        return bizId;
    }

    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public Integer getRemaining()
    {
        return remaining;
    }

    public void setRemaining(Integer remaining)
    {
        this.remaining = remaining;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("memberId", getMemberId())
            .append("changeType", getChangeType())
            .append("points", getPoints())
            .append("balanceAfter", getBalanceAfter())
            .append("bizType", getBizType())
            .append("bizId", getBizId())
            .append("remaining", getRemaining())
            .append("expireTime", getExpireTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
