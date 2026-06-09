package com.manzhushaka.system.vo;

import java.util.ArrayList;
import java.util.List;

public class MenuTreeNode {
    private final Long id;
    private final Long parentId;
    private final String menuName;
    private final Integer sort;
    private final List<MenuTreeNode> children = new ArrayList<>();

    public MenuTreeNode(Long id, Long parentId, String menuName, Integer sort) {
        this.id = id;
        this.parentId = parentId;
        this.menuName = menuName;
        this.sort = sort;
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getMenuName() {
        return menuName;
    }

    public Integer getSort() {
        return sort;
    }

    public List<MenuTreeNode> getChildren() {
        return children;
    }
}
