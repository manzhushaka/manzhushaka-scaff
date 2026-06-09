package com.manzhushaka.system.dto.dept;

public class DeptQuery {
    private String deptName;
    private Integer status;

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
