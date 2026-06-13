package com.manzhushaka.system.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 承载 MenuTreeNode 响应数据。
 */
public class MenuTreeNode {
    private final Long id;
    private final Long parentId;
    private final String menuName;
    private final Integer sort;
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final List<MenuTreeNode> children = new ArrayList<>();

    /**
     * 创建 MenuTreeNode 实例。
     *
     * @param id 主键 ID
     * @param parentId parentId 标识
     * @param menuName menuName 参数
     * @param sort sort 参数
     */
    public MenuTreeNode(Long id, Long parentId, String menuName, Integer sort) {
        this.id = id;
        this.parentId = parentId;
        this.menuName = menuName;
        this.sort = sort;
    }

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
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
     * 返回 menuName。
     *
     * @return 字段值
     */
    public String getMenuName() {
        return menuName;
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
     * 返回 children。
     *
     * @return 字段值
     */
    public List<MenuTreeNode> getChildren() {
        return children;
    }
}
