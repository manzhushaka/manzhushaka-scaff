package com.manzhushaka.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 承载 LoginRequest 请求参数。
 */
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码标识不能为空")
    private String captchaKey;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /**
     * 返回 username。
     *
     * @return 字段值
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置 username。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 返回 password。
     *
     * @return 字段值
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置 password。
     *
     * @param password password 参数
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 返回 captchaKey。
     *
     * @return 字段值
     */
    public String getCaptchaKey() {
        return captchaKey;
    }

    /**
     * 设置 captchaKey。
     *
     * @param captchaKey captchaKey 参数
     */
    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }

    /**
     * 返回 captchaCode。
     *
     * @return 字段值
     */
    public String getCaptchaCode() {
        return captchaCode;
    }

    /**
     * 设置 captchaCode。
     *
     * @param captchaCode captchaCode 参数
     */
    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
