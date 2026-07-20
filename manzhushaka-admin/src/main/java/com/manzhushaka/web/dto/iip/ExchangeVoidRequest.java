package com.manzhushaka.web.dto.iip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理员作废未使用券请求。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
public class ExchangeVoidRequest
{
    /** 作废原因 */
    @NotBlank(message = "作废原因不能为空")
    @Size(max = 255, message = "作废原因不能超过255个字符")
    private String voidReason;

    public String getVoidReason()
    {
        return voidReason;
    }

    public void setVoidReason(String voidReason)
    {
        this.voidReason = voidReason;
    }
}
