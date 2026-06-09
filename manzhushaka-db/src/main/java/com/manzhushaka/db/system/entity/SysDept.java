package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

public class SysDept extends BaseEntity {
    private Long parentId;
    private String deptName;
    private String ancestorPath;
    private Integer sort;
    private Integer status;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getAncestorPath() {
        return ancestorPath;
    }

    public void setAncestorPath(String ancestorPath) {
        this.ancestorPath = ancestorPath;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
