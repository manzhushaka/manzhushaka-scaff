package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 修改小程序用户状态请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MemberStatusRequest
{
    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long memberId;

    /** 状态（0正常 1停用） */
    @NotBlank(message = "状态不能为空")
    private String status;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
