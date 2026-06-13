package com.manzhushaka.system.dto.role;

import com.manzhushaka.common.enums.DataScopeType;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 承载 RoleForm 请求参数。
 */
public class RoleForm {
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    private DataScopeType dataScope;
    private Integer status;
    private List<Long> menuIds;

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
     * 返回 dataScope。
     *
     * @return 字段值
     */
    public DataScopeType getDataScope() {
        return dataScope;
    }

    /**
     * 设置 dataScope。
     *
     * @param dataScope dataScope 参数
     */
    public void setDataScope(DataScopeType dataScope) {
        this.dataScope = dataScope;
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

    /**
     * 返回 menuIds。
     *
     * @return 字段值
     */
    public List<Long> getMenuIds() {
        return menuIds;
    }

    /**
     * 设置 menuIds。
     *
     * @param menuIds menuIds 标识
     */
    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
