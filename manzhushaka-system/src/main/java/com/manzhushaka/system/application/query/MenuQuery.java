package com.manzhushaka.system.application.query;

/**
 * 菜单查询条件。
 *
 * @param menuName 菜单名称
 * @param visible 显示状态
 * @param status 菜单状态
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MenuQuery(String menuName, String visible, String status)
{
}
