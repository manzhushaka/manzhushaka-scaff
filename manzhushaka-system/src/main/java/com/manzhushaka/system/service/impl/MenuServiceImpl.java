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

/**
 * 实现 MenuServiceImpl 业务服务。
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;

    /**
     * 创建 MenuServiceImpl 实例。
     *
     * @param menuMapper menuMapper 参数
     */
    public MenuServiceImpl(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @Override
    public List<MenuVO> list(MenuQuery query) {
        return menuMapper.selectList(buildQueryWrapper(query)).stream()
            .map(this::toMenuVO)
            .toList();
    }

    /**
     * 执行 tree 逻辑。
     *
     * @param query 查询条件
     * @return 处理结果
     */
    @Override
    public List<MenuTreeNode> tree(MenuQuery query) {
        List<MenuTreeNode> flatMenus = menuMapper.selectList(buildQueryWrapper(query)).stream()
            .map(menu -> new MenuTreeNode(menu.getId(), menu.getParentId(), menu.getMenuName(), defaultSort(menu.getSort())))
            .toList();
        return MenuTreeBuilder.build(flatMenus);
    }

    /**
     * 执行 routes By User Id 逻辑。
     *
     * @param userId 用户 ID
     * @return 处理结果
     */
    @Override
    public List<MenuRouteVO> routesByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        return buildRouteTree(menus);
    }

    /**
     * 查询下拉选项。
     *
     * @return 查询结果
     */
    @Override
    public List<LabelValueOption> options() {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getStatus, 1)
            .orderByAsc(SysMenu::getSort, SysMenu::getId));
        return menus.stream().map(menu -> new LabelValueOption(menu.getMenuName(), String.valueOf(menu.getId()))).toList();
    }

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @Override
    public MenuVO getById(Long id) {
        return toMenuVO(getMenuOrThrow(id));
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @Override
    @Transactional
    public Long create(MenuForm form) {
        SysMenu entity = new SysMenu();
        applyForm(entity, form);
        menuMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    @Override
    @Transactional
    public void update(Long id, MenuForm form) {
        SysMenu entity = getMenuOrThrow(id);
        applyForm(entity, form);
        menuMapper.updateById(entity);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (menuMapper.deleteById(id) == 0) {
            throw new BizException(404, "菜单不存在");
        }
    }

    /**
     * 构建查询条件。
     *
     * @param query 查询条件
     * @return 处理结果
     */
    private LambdaQueryWrapper<SysMenu> buildQueryWrapper(MenuQuery query) {
        return new LambdaQueryWrapper<SysMenu>()
            .like(StringUtils.hasText(query.getMenuName()), SysMenu::getMenuName, query.getMenuName())
            .eq(StringUtils.hasText(query.getMenuType()), SysMenu::getMenuType, query.getMenuType())
            .eq(query.getStatus() != null, SysMenu::getStatus, query.getStatus())
            .orderByAsc(SysMenu::getSort, SysMenu::getId);
    }

    /**
     * 返回 menuOrThrow。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    private SysMenu getMenuOrThrow(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BizException(404, "菜单不存在");
        }
        return menu;
    }

    /**
     * 更新 apply Form 数据。
     *
     * @param entity 实体对象
     * @param form 表单参数
     */
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

    /**
     * 转换为菜单响应对象。
     *
     * @param menu menu 参数
     * @return 处理结果
     */
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

    /**
     * 构建路由树。
     *
     * @param menus menus 参数
     * @return 处理结果
     */
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

    /**
     * 执行 default Sort 逻辑。
     *
     * @param sort sort 参数
     * @return 处理结果
     */
    private int defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }
}
