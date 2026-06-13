package com.manzhushaka.system.dto.user;

import jakarta.validation.constraints.NotBlank;

/**
 * 承载 UserForm 请求参数。
 */
public class UserForm {
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    private Long deptId;
    private Integer status;

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
     * 返回 nickname。
     *
     * @return 字段值
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置 nickname。
     *
     * @param nickname nickname 参数
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 返回 deptId。
     *
     * @return 字段值
     */
    public Long getDeptId() {
        return deptId;
    }

    /**
     * 设置 deptId。
     *
     * @param deptId 部门 ID
     */
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    /**
     * 返回 status。
     *
     * @return 字段值
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status status 参数
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
