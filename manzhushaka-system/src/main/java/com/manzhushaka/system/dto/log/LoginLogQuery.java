package com.manzhushaka.system.dto.log;

import com.manzhushaka.system.dto.PageQuery;

public class LoginLogQuery extends PageQuery {
    private String username;
    private String loginStatus;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }
}
