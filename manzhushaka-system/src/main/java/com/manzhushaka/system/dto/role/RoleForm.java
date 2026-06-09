package com.manzhushaka.system.dto.role;

import com.manzhushaka.common.enums.DataScopeType;
import jakarta.validation.constraints.NotBlank;

public class RoleForm {
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    private DataScopeType dataScope;
    private Integer status;

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
}
