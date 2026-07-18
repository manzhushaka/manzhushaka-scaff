package com.manzhushaka.web.dto.miniapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 小程序商户核销请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MerchantVerifyRequest
{
    /** 核销码 */
    @NotBlank(message = "核销码不能为空")
    @Size(max = 32, message = "核销码不能超过32个字符")
    private String verifyCode;

    public String getVerifyCode()
    {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode)
    {
        this.verifyCode = verifyCode;
    }
}
