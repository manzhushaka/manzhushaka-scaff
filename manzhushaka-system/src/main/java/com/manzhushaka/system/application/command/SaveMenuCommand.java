package com.manzhushaka.system.application.command;

/**
 * 保存菜单命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveMenuCommand(Long menuId, String menuName, Long parentId, Integer orderNum,
        String path, String component, String query, String routeName, String isFrame,
        String isCache, String menuType, String visible, String status, String perms, String icon)
{
}
