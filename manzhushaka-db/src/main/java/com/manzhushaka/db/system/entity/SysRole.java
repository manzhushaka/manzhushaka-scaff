package com.manzhushaka.db.system.entity;

import com.manzhushaka.common.enums.DataScopeType;
import com.manzhushaka.db.meta.BaseEntity;

/**
 * 映射 SysRole 数据库实体。
 */
public class SysRole extends BaseEntity {
    private String roleCode;
    private String roleName;
    private DataScopeType dataScope;
    private Integer status;
    private Integer deleted;

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
     * 返回 deleted。
     *
     * @return 字段值
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 设置 deleted。
     *
     * @param deleted deleted 参数
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
