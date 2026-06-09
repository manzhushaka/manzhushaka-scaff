package com.manzhushaka.system.dto.user;

import jakarta.validation.constraints.NotBlank;

public class UserForm {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    private Long deptId;
    private Integer status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
