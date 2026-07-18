package com.manzhushaka.iip.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.core.domain.BaseEntity;

/**
 * 用户平台账号对象 iip_member_account
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public class IipMemberAccount extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 账号ID */
    private Long accountId;

    /** 用户ID */
    private Long memberId;

    /** 平台（wechat/alipay/unionpay） */
    private String platform;

    /** 平台用户标识 */
    private String openid;

    /** 平台联合标识 */
    private String unionid;

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

    @NotBlank(message = "平台不能为空")
    @Size(min = 0, max = 16, message = "平台标识不能超过16个字符")
    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform(String platform)
    {
        this.platform = platform;
    }

    @NotBlank(message = "openid不能为空")
    @Size(min = 0, max = 128, message = "openid不能超过128个字符")
    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }

    public String getUnionid()
    {
        return unionid;
    }

    public void setUnionid(String unionid)
    {
        this.unionid = unionid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("accountId", getAccountId())
            .append("memberId", getMemberId())
            .append("platform", getPlatform())
            .append("openid", getOpenid())
            .append("unionid", getUnionid())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
