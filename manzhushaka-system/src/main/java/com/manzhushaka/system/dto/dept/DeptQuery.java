package com.manzhushaka.system.dto.dept;

/**
 * 承载 DeptQuery 请求参数。
 */
public class DeptQuery {
    private String deptName;
    private Integer status;

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
