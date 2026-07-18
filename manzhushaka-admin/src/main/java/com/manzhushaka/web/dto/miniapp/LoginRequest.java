package com.manzhushaka.web.dto.miniapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 小程序登录请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class LoginRequest
{
    /** 平台（wechat/alipay/unionpay） */
    @NotBlank(message = "平台不能为空")
    private String platform;

    /** 小程序登录code */
    @NotBlank(message = "登录code不能为空")
    private String code;

    /** 昵称（可选） */
    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;

    /** 头像（可选） */
    @Size(max = 255, message = "头像不能超过255个字符")
    private String avatar;

    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform(String platform)
    {
        this.platform = platform;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }
}
