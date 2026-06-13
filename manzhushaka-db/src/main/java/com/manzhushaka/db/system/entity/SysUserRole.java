package com.manzhushaka.db.system.entity;

/**
 * 映射 SysUserRole 数据库实体。
 */
public class SysUserRole {
    private Long userId;
    private Long roleId;

    /**
     * 返回 userId。
     *
     * @return 字段值
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置 userId。
     *
     * @param userId 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 返回 roleId。
     *
     * @return 字段值
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * 设置 roleId。
     *
     * @param roleId 角色 ID
     */
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
