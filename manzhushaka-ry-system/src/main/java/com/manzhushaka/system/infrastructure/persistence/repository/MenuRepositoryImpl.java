package com.manzhushaka.system.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.manzhushaka.system.domain.repository.MenuRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysMenu;
import com.manzhushaka.system.infrastructure.persistence.mapper.SysMenuMapper;

/**
 * 菜单仓储实现
 *
 * @author manzhushaka
 */
@Repository
public class MenuRepositoryImpl implements MenuRepository
{
    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> selectMenuList(SysMenu menu)
    {
        return menuMapper.selectMenuList(menu);
    }

    @Override
    public List<String> selectMenuPerms()
    {
        return menuMapper.selectMenuPerms();
    }

    @Override
    public List<SysMenu> selectMenuListByUserId(SysMenu menu)
    {
        return menuMapper.selectMenuListByUserId(menu);
    }

    @Override
    public List<String> selectMenuPermsByRoleId(Long roleId)
    {
        return menuMapper.selectMenuPermsByRoleId(roleId);
    }

    @Override
    public List<String> selectMenuPermsByUserId(Long userId)
    {
        return menuMapper.selectMenuPermsByUserId(userId);
    }

    @Override
    public List<SysMenu> selectMenuTreeAll()
    {
        return menuMapper.selectMenuTreeAll();
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId)
    {
        return menuMapper.selectMenuTreeByUserId(userId);
    }

    @Override
    public List<Long> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly)
    {
        return menuMapper.selectMenuListByRoleId(roleId, menuCheckStrictly);
    }

    @Override
    public SysMenu selectMenuById(Long menuId)
    {
        return menuMapper.selectMenuById(menuId);
    }

    @Override
    public int hasChildByMenuId(Long menuId)
    {
        return menuMapper.hasChildByMenuId(menuId);
    }

    @Override
    public int insertMenu(SysMenu menu)
    {
        return menuMapper.insertMenu(menu);
    }

    @Override
    public int updateMenu(SysMenu menu)
    {
        return menuMapper.updateMenu(menu);
    }

    @Override
    public void updateMenuSort(SysMenu menu)
    {
        menuMapper.updateMenuSort(menu);
    }

    @Override
    public int deleteMenuById(Long menuId)
    {
        return menuMapper.deleteMenuById(menuId);
    }

    @Override
    public SysMenu checkMenuNameUnique(String menuName, Long parentId)
    {
        return menuMapper.checkMenuNameUnique(menuName, parentId);
    }

    @Override
    public List<SysMenu> selectMenusByPathOrRouteName(String path, String routeName)
    {
        return menuMapper.selectMenusByPathOrRouteName(path, routeName);
    }
}