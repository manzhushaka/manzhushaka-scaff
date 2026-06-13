package com.manzhushaka.system.vo.log;

import java.time.LocalDateTime;

/**
 * 承载 LoginLogVO 响应数据。
 */
public class LoginLogVO {
    private Long id;
    private String username;
    private String loginStatus;
    private String ip;
    private String userAgent;
    private String message;
    private LocalDateTime createTime;

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 返回 loginStatus。
     *
     * @return 字段值
     */
    public String getLoginStatus() {
        return loginStatus;
    }

    /**
     * 设置 loginStatus。
     *
     * @param loginStatus loginStatus 参数
     */
    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    /**
     * 返回 ip。
     *
     * @return 字段值
     */
    public String getIp() {
        return ip;
    }

    /**
     * 设置 ip。
     *
     * @param ip ip 参数
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 返回 userAgent。
     *
     * @return 字段值
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 设置 userAgent。
     *
     * @param userAgent userAgent 参数
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * 返回 message。
     *
     * @return 字段值
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置 message。
     *
     * @param message message 参数
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
