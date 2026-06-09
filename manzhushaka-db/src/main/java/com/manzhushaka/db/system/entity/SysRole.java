package com.manzhushaka.db.system.entity;

import com.manzhushaka.common.enums.DataScopeType;
import com.manzhushaka.db.meta.BaseEntity;

public class SysRole extends BaseEntity {
    private String roleCode;
    private String roleName;
    private DataScopeType dataScope;
    private Integer status;
    private Integer deleted;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public DataScopeType getDataScope() {
        return dataScope;
    }

    public void setDataScope(DataScopeType dataScope) {
        this.dataScope = dataScope;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
