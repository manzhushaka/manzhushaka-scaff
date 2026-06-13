package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

/**
 * 映射 SysDept 数据库实体。
 */
public class SysDept extends BaseEntity {
    private Long parentId;
    private String deptName;
    private String ancestorPath;
    private Integer sort;
    private Integer status;

    /**
     * 返回 parentId。
     *
     * @return 字段值
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置 parentId。
     *
     * @param parentId parentId 标识
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 返回 deptName。
     *
     * @return 字段值
     */
    public String getDeptName() {
        return deptName;
    }

    /**
     * 设置 deptName。
     *
     * @param deptName deptName 参数
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    /**
     * 返回 ancestorPath。
     *
     * @return 字段值
     */
    public String getAncestorPath() {
        return ancestorPath;
    }

    /**
     * 设置 ancestorPath。
     *
     * @param ancestorPath ancestorPath 参数
     */
    public void setAncestorPath(String ancestorPath) {
        this.ancestorPath = ancestorPath;
    }

    /**
     * 返回 sort。
     *
     * @return 字段值
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置 sort。
     *
     * @param sort sort 参数
     */
    public void setSort(Integer sort) {
        this.sort = sort;
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
