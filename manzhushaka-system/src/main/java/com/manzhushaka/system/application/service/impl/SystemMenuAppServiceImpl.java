package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.constant.UserConstants;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.application.command.SaveMenuCommand;
import com.manzhushaka.system.application.query.MenuQuery;
import com.manzhushaka.system.application.result.shared.TreeNodeResult;
import com.manzhushaka.system.application.result.system.MenuResult;
import com.manzhushaka.system.application.result.system.RouterResult;
import com.manzhushaka.system.application.result.system.SystemResultMapper;
import com.manzhushaka.system.application.service.SystemMenuAppService;
import com.manzhushaka.system.infrastructure.persistence.entity.SysMenu;
import com.manzhushaka.system.service.ISysMenuService;

/**
 * 系统菜单应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class SystemMenuAppServiceImpl implements SystemMenuAppService
{
    @Autowired
    private ISysMenuService menuService;

    @Override
    public List<MenuResult> listMenuResults(MenuQuery query, Long userId)
    {
        return SystemResultMapper.toMenuResults(menuService.selectMenuList(toEntity(query), userId));
    }

    @Override
    public MenuResult getMenuResult(Long menuId)
    {
        return SystemResultMapper.toMenuResult(menuService.selectMenuById(menuId));
    }

    @Override
    public List<TreeNodeResult> listMenuTree(MenuQuery query, Long userId)
    {
        List<SysMenu> menus = menuService.selectMenuList(toEntity(query), userId);
        return menuService.buildMenuTreeSelect(menus);
    }

    @Override
    public List<Long> listCheckedMenuIds(Long roleId)
    {
        return menuService.selectMenuListByRoleId(roleId);
    }

    @Override
    public List<RouterResult> listRouterResults(Long userId)
    {
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return SystemResultMapper.toRouterResults(menuService.buildMenus(menus));
    }

    @Override
    @Transactional
    public int createMenu(SaveMenuCommand command, String operatorUsername)
    {
        SysMenu menu = toEntity(command);
        validateMenu(menu, "新增");
        menu.setCreateBy(operatorUsername);
        return menuService.insertMenu(menu);
    }

    @Override
    @Transactional
    public int updateMenu(SaveMenuCommand command, String operatorUsername)
    {
        SysMenu menu = toEntity(command);
        validateMenu(menu, "修改");
        if (menu.getMenuId().equals(menu.getParentId()))
        {
            throw new ServiceException("修改菜单'" + menu.getMenuName()
                    + "'失败，上级菜单不能选择自己");
        }
        menu.setUpdateBy(operatorUsername);
        return menuService.updateMenu(menu);
    }

    @Override
    public void updateMenuSort(String[] menuIds, String[] orderNums)
    {
        menuService.updateMenuSort(menuIds, orderNums);
    }

    @Override
    @Transactional
    public int deleteMenu(Long menuId)
    {
        if (menuService.hasChildByMenuId(menuId))
        {
            throw new ServiceException("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId))
        {
            throw new ServiceException("菜单已分配,不允许删除");
        }
        return menuService.deleteMenuById(menuId);
    }

    private void validateMenu(SysMenu menu, String action)
    {
        if (!menuService.checkMenuNameUnique(menu))
        {
            throw new ServiceException(action + "菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath()))
        {
            throw new ServiceException(action + "菜单'" + menu.getMenuName()
                    + "'失败，地址必须以http(s)://开头");
        }
        if (!menuService.checkRouteConfigUnique(menu))
        {
            throw new ServiceException(action + "菜单'" + menu.getMenuName()
                    + "'失败，路由名称或地址已存在");
        }
    }

    private SysMenu toEntity(MenuQuery query)
    {
        SysMenu menu = new SysMenu();
        if (query != null)
        {
            menu.setMenuName(query.menuName());
            menu.setVisible(query.visible());
            menu.setStatus(query.status());
        }
        return menu;
    }

    private SysMenu toEntity(SaveMenuCommand command)
    {
        SysMenu menu = new SysMenu();
        menu.setMenuId(command.menuId());
        menu.setMenuName(command.menuName());
        menu.setParentId(command.parentId());
        menu.setOrderNum(command.orderNum());
        menu.setPath(command.path());
        menu.setComponent(command.component());
        menu.setQuery(command.query());
        menu.setRouteName(command.routeName());
        menu.setIsFrame(command.isFrame());
        menu.setIsCache(command.isCache());
        menu.setMenuType(command.menuType());
        menu.setVisible(command.visible());
        menu.setStatus(command.status());
        menu.setPerms(command.perms());
        menu.setIcon(command.icon());
        return menu;
    }
}
