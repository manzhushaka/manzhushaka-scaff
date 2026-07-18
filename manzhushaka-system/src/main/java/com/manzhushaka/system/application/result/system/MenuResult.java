package com.manzhushaka.system.application.result.system;

import java.util.Date;
import java.util.List;

/**
 * 菜单查询结果。
 *
 * @param menuId 菜单 ID
 * @param menuName 菜单名称
 * @param parentName 父菜单名称
 * @param parentId 父菜单 ID
 * @param orderNum 显示顺序
 * @param path 路由地址
 * @param component 组件路径
 * @param query 路由参数
 * @param routeName 路由名称
 * @param isFrame 是否外链
 * @param isCache 是否缓存
 * @param menuType 菜单类型
 * @param visible 显示状态
 * @param status 菜单状态
 * @param perms 权限标识
 * @param icon 菜单图标
 * @param createBy 创建者
 * @param createTime 创建时间
 * @param updateBy 更新者
 * @param updateTime 更新时间
 * @param remark 备注
 * @param children 子菜单
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MenuResult(Long menuId, String menuName, String parentName, Long parentId,
        Integer orderNum, String path, String component, String query, String routeName,
        String isFrame, String isCache, String menuType, String visible, String status,
        String perms, String icon, String createBy, Date createTime, String updateBy,
        Date updateTime, String remark, List<MenuResult> children)
{
}
