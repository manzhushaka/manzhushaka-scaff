package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

/**
 * 映射 SysRoleMenu 数据库实体。
 */
public class SysRoleMenu extends BaseEntity {
    private Long roleId;
    private Long menuId;

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

    /**
     * 返回 menuId。
     *
     * @return 字段值
     */
    public Long getMenuId() {
        return menuId;
    }

    /**
     * 设置 menuId。
     *
     * @param menuId 菜单 ID
     */
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
