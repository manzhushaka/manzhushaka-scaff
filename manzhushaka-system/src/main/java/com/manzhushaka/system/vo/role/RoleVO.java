package com.manzhushaka.system.vo.role;

import com.manzhushaka.common.enums.DataScopeType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 承载 RoleVO 响应数据。
 */
public class RoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private DataScopeType dataScope;
    private Integer status;
    private LocalDateTime createTime;
    private List<Long> menuIds;

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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
