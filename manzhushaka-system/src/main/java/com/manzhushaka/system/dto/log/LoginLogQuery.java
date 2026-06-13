package com.manzhushaka.system.dto.log;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 LoginLogQuery 请求参数。
 */
public class LoginLogQuery extends PageQuery {
    private String username;
    private String loginStatus;

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
}
