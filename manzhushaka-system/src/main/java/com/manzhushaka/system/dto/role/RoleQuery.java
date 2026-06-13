package com.manzhushaka.system.dto.role;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 RoleQuery 请求参数。
 */
public class RoleQuery extends PageQuery {
    private String roleCode;
    private String roleName;
    private Integer status;

    /**
     * 返回 roleCode。
     *
     * @return 字段值
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * 设置 roleCode。
     *
     * @param roleCode roleCode 参数
     */
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    /**
     * 返回 roleName。
     *
     * @return 字段值
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * 设置 roleName。
     *
     * @param roleName roleName 参数
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
