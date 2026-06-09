package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysMenu;
import com.manzhushaka.db.system.mapper.SysMenuMapper;
import com.manzhushaka.system.dto.menu.MenuForm;
import com.manzhushaka.system.dto.menu.MenuQuery;
import com.manzhushaka.system.service.MenuService;
import com.manzhushaka.system.util.MenuTreeBuilder;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.MenuTreeNode;
import com.manzhushaka.system.vo.menu.MenuRouteVO;
import com.manzhushaka.system.vo.menu.MenuVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;

    public MenuServiceImpl(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuVO> list(MenuQuery query) {
        return menuMapper.selectList(buildQueryWrapper(query)).stream()
            .map(this::toMenuVO)
            .toList();
    }

    @Override
    public List<MenuTreeNode> tree(MenuQuery query) {
        List<MenuTreeNode> flatMenus = menuMapper.selectList(buildQueryWrapper(query)).stream()
            .map(menu -> new MenuTreeNode(menu.getId(), menu.getParentId(), menu.getMenuName(), defaultSort(menu.getSort())))
            .toList();
        return MenuTreeBuilder.build(flatMenus);
    }

    @Override
    public List<MenuRouteVO> routesByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        return buildRouteTree(menus);
    }

    @Override
    public List<LabelValueOption> options() {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getStatus, 1)
            .orderByAsc(SysMenu::getSort, SysMenu::getId));
        return menus.stream().map(menu -> new LabelValueOption(menu.getMenuName(), String.valueOf(menu.getId()))).toList();
    }

    @Override
    public MenuVO getById(Long id) {
        return toMenuVO(getMenuOrThrow(id));
    }

    @Override
    @Transactional
    public Long create(MenuForm form) {
        SysMenu entity = new SysMenu();
        applyForm(entity, form);
        menuMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(Long id, MenuForm form) {
        SysMenu entity = getMenuOrThrow(id);
        applyForm(entity, form);
        menuMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (menuMapper.deleteById(id) == 0) {
            throw new BizException(404, "菜单不存在");
        }
    }

    private LambdaQueryWrapper<SysMenu> buildQueryWrapper(MenuQuery query) {
        return new LambdaQueryWrapper<SysMenu>()
            .like(StringUtils.hasText(query.getMenuName()), SysMenu::getMenuName, query.getMenuName())
            .eq(StringUtils.hasText(query.getMenuType()), SysMenu::getMenuType, query.getMenuType())
            .eq(query.getStatus() != null, SysMenu::getStatus, query.getStatus())
            .orderByAsc(SysMenu::getSort, SysMenu::getId);
    }

    private SysMenu getMenuOrThrow(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(404, "菜单不存在");
        }
        return menu;
    }

    private void applyForm(SysMenu entity, MenuForm form) {
        entity.setParentId(form.getParentId() == null ? 0L : form.getParentId());
        entity.setMenuType(form.getMenuType());
        entity.setMenuName(form.getMenuName());
        entity.setRoutePath(form.getRoutePath());
        entity.setRouteName(form.getRouteName());
        entity.setComponent(form.getComponent());
        entity.setIcon(form.getIcon());
        entity.setSort(form.getSort() == null ? 0 : form.getSort());
        entity.setVisible(form.getVisible() == null ? 1 : form.getVisible());
        entity.setKeepAlive(form.getKeepAlive() == null ? 0 : form.getKeepAlive());
        entity.setPerms(form.getPerms());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    private MenuVO toMenuVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setMenuType(menu.getMenuType());
        vo.setMenuName(menu.getMenuName());
        vo.setRoutePath(menu.getRoutePath());
        vo.setRouteName(menu.getRouteName());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setVisible(menu.getVisible());
        vo.setKeepAlive(menu.getKeepAlive());
        vo.setPerms(menu.getPerms());
        vo.setStatus(menu.getStatus());
        vo.setCreateTime(menu.getCreateTime());
        return vo;
    }

    private List<MenuRouteVO> buildRouteTree(List<SysMenu> menus) {
        Map<Long, MenuRouteVO> routeMap = new LinkedHashMap<>();
        Map<Long, Long> parentMap = new LinkedHashMap<>();
        for (SysMenu menu : menus.stream()
            .sorted(Comparator.comparing((SysMenu item) -> defaultSort(item.getSort())).thenComparing(SysMenu::getId))
            .toList()) {
            MenuRouteVO route = new MenuRouteVO();
            route.setPath(menu.getRoutePath());
            route.setName(menu.getRouteName());
            route.setComponent(menu.getComponent());
            route.getMeta().setTitle(menu.getMenuName());
            route.getMeta().setIcon(menu.getIcon());
            route.getMeta().setHidden(Integer.valueOf(0).equals(menu.getVisible()));
            route.getMeta().setKeepAlive(Integer.valueOf(1).equals(menu.getKeepAlive()));
            routeMap.put(menu.getId(), route);
            parentMap.put(menu.getId(), menu.getParentId());
        }
        List<MenuRouteVO> roots = new ArrayList<>();
        for (Map.Entry<Long, MenuRouteVO> entry : routeMap.entrySet()) {
            Long parentId = parentMap.get(entry.getKey());
            if (parentId == null || parentId == 0L || !routeMap.containsKey(parentId)) {
                roots.add(entry.getValue());
                continue;
            }
            routeMap.get(parentId).getChildren().add(entry.getValue());
        }
        return roots;
    }

    private int defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }
}
