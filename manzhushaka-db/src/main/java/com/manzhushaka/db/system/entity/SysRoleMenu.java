package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

public class SysRoleMenu extends BaseEntity {
    private Long roleId;
    private Long menuId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
