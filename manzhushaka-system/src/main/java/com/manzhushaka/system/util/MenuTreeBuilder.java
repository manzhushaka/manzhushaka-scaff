package com.manzhushaka.system.util;

import com.manzhushaka.system.vo.MenuTreeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MenuTreeBuilder {

    private MenuTreeBuilder() {
    }

    public static List<MenuTreeNode> build(List<MenuTreeNode> flatMenus) {
        Map<Long, MenuTreeNode> nodeMap = new LinkedHashMap<>();
        for (MenuTreeNode menu : flatMenus.stream().sorted(Comparator.comparing(MenuTreeNode::getSort)).toList()) {
            nodeMap.put(menu.getId(), menu);
        }
        List<MenuTreeNode> roots = new ArrayList<>();
        for (MenuTreeNode node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0L) {
                roots.add(node);
                continue;
            }
            MenuTreeNode parent = nodeMap.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        roots.sort(Comparator.comparing(MenuTreeNode::getSort));
        return roots;
    }
}
