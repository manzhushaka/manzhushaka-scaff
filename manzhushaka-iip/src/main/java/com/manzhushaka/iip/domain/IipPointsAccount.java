package com.manzhushaka.iip.domain;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 积分账户对象 iip_points_account
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipPointsAccount extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 账户ID */
    private Long accountId;

    /** 用户ID */
    private Long memberId;

    /** 累计获得积分 */
    private Integer totalPoints;

    /** 可用积分 */
    private Integer availablePoints;

    /** 已使用积分 */
    private Integer usedPoints;

    /** 已过期积分 */
    private Integer expiredPoints;

    /** 乐观锁版本号 */
    private Integer version;

    /** 用户昵称（关联 iip_member 查询展示用，非表字段） */
    private String nickname;

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
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

    public Integer getTotalPoints()
    {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints)
    {
        this.totalPoints = totalPoints;
    }

    public Integer getAvailablePoints()
    {
        return availablePoints;
    }

    public void setAvailablePoints(Integer availablePoints)
    {
        this.availablePoints = availablePoints;
    }

    public Integer getUsedPoints()
    {
        return usedPoints;
    }

    public void setUsedPoints(Integer usedPoints)
    {
        this.usedPoints = usedPoints;
    }

    public Integer getExpiredPoints()
    {
        return expiredPoints;
    }

    public void setExpiredPoints(Integer expiredPoints)
    {
        this.expiredPoints = expiredPoints;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("accountId", getAccountId())
            .append("memberId", getMemberId())
            .append("totalPoints", getTotalPoints())
            .append("availablePoints", getAvailablePoints())
            .append("usedPoints", getUsedPoints())
            .append("expiredPoints", getExpiredPoints())
            .append("version", getVersion())
            .append("nickname", getNickname())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
