package com.manzhushaka.system.vo.dept;

import java.util.ArrayList;
import java.util.List;

/**
 * 承载 DeptTreeVO 响应数据。
 */
public class DeptTreeVO {
    private Long id;
    private Long parentId;
    private String deptName;
    private String ancestorPath;
    private Integer sort;
    private Integer status;
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final List<DeptTreeVO> children = new ArrayList<>();

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

    /**
     * 返回 children。
     *
     * @return 字段值
     */
    public List<DeptTreeVO> getChildren() {
        return children;
    }
}
