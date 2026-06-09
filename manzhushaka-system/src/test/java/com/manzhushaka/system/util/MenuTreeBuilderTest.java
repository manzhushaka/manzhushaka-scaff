package com.manzhushaka.system.util;

import com.manzhushaka.system.vo.MenuTreeNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuTreeBuilderTest {

    @Test
    void shouldBuildNestedMenuTreeInSortOrder() {
        List<MenuTreeNode> flatMenus = List.of(
            new MenuTreeNode(3L, 1L, "用户管理", 2),
            new MenuTreeNode(1L, 0L, "系统管理", 1),
            new MenuTreeNode(2L, 1L, "角色管理", 1)
        );

        List<MenuTreeNode> tree = MenuTreeBuilder.build(flatMenus);

        assertEquals(1, tree.size());
        assertEquals("系统管理", tree.get(0).getMenuName());
        assertEquals(List.of("角色管理", "用户管理"),
            tree.get(0).getChildren().stream().map(MenuTreeNode::getMenuName).toList());
    }
}
