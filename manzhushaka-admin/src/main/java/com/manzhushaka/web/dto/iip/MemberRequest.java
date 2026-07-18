package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.Size;
import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 小程序用户查询请求（后台列表/导出）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MemberRequest extends DateRangeRequest
{
    /** 关键字（昵称或手机号模糊匹配） */
    @Size(max = 64, message = "关键字不能超过64个字符")
    private String keyword;

    /** 手机号（精确匹配） */
    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;

    /** 状态（0正常 1停用） */
    private String status;

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
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
