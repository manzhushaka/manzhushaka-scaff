package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.application.command.SaveMenuCommand;
import com.manzhushaka.system.application.query.MenuQuery;
import com.manzhushaka.system.application.result.shared.TreeNodeResult;
import com.manzhushaka.system.application.result.system.MenuResult;
import com.manzhushaka.system.application.result.system.RouterResult;

/**
 * 系统菜单应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface SystemMenuAppService
{
    /**
     * 查询菜单列表。
     *
     * @param query 查询条件
     * @param userId 用户 ID
     * @return 菜单列表
     */
    List<MenuResult> listMenuResults(MenuQuery query, Long userId);

    /**
     * 查询菜单详情。
     *
     * @param menuId 菜单 ID
     * @return 菜单详情
     */
    MenuResult getMenuResult(Long menuId);

    /**
     * 查询菜单树。
     *
     * @param query 查询条件
     * @param userId 用户 ID
     * @return 菜单树节点
     */
    List<TreeNodeResult> listMenuTree(MenuQuery query, Long userId);

    /**
     * 查询角色已选菜单 ID。
     *
     * @param roleId 角色 ID
     * @return 菜单 ID 列表
     */
    List<Long> listCheckedMenuIds(Long roleId);

    /**
     * 查询用户路由。
     *
     * @param userId 用户 ID
     * @return 路由列表
     */
    List<RouterResult> listRouterResults(Long userId);

    /**
     * 新增菜单。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createMenu(SaveMenuCommand command, String operatorUsername);

    /**
     * 修改菜单。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateMenu(SaveMenuCommand command, String operatorUsername);

    /**
     * 修改菜单排序。
     *
     * @param menuIds 菜单 ID 数组
     * @param orderNums 排序值数组
     */
    void updateMenuSort(String[] menuIds, String[] orderNums);

    /**
     * 删除菜单。
     *
     * @param menuId 菜单 ID
     * @return 影响行数
     */
    int deleteMenu(Long menuId);
}
