package com.manzhushaka.system.domain.repository;

import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysMenu;

/**
 * 菜单仓储接口
 *
 * @author manzhushaka
 */
public interface MenuRepository
{
    /**
     * 查询系统菜单列表
     */
    List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 根据用户所有权限
     */
    List<String> selectMenuPerms();

    /**
     * 根据用户查询系统菜单列表
     */
    List<SysMenu> selectMenuListByUserId(SysMenu menu);

    /**
     * 根据角色ID查询权限
     */
    List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限
     */
    List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单
     */
    List<SysMenu> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单树信息
     */
    List<Long> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly);

    /**
     * 根据菜单ID查询信息
     */
    SysMenu selectMenuById(Long menuId);

    /**
     * 是否存在菜单子节点
     */
    int hasChildByMenuId(Long menuId);

    /**
     * 新增菜单信息
     */
    int insertMenu(SysMenu menu);

    /**
     * 修改菜单信息
     */
    int updateMenu(SysMenu menu);

    /**
     * 保存菜单排序
     */
    void updateMenuSort(SysMenu menu);

    /**
     * 删除菜单管理信息
     */
    int deleteMenuById(Long menuId);

    /**
     * 校验菜单名称是否唯一
     */
    SysMenu checkMenuNameUnique(String menuName, Long parentId);

    /**
     * 根据路由路径或名称查询菜单信息
     */
    List<SysMenu> selectMenusByPathOrRouteName(String path, String routeName);
}