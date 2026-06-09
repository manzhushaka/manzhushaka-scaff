package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.menu.MenuForm;
import com.manzhushaka.system.dto.menu.MenuQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.MenuTreeNode;
import com.manzhushaka.system.vo.menu.MenuRouteVO;
import com.manzhushaka.system.vo.menu.MenuVO;

import java.util.List;

public interface MenuService {
    List<MenuVO> list(MenuQuery query);

    List<MenuTreeNode> tree(MenuQuery query);

    List<MenuRouteVO> routesByUserId(Long userId);

    List<LabelValueOption> options();

    MenuVO getById(Long id);

    Long create(MenuForm form);

    void update(Long id, MenuForm form);

    void delete(Long id);
}
