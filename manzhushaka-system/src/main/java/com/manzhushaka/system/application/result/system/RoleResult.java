package com.manzhushaka.system.application.result.system;

import java.util.Date;
import java.util.Set;

/**
 * 角色查询结果。
 *
 * @param roleId 角色 ID
 * @param roleName 角色名称
 * @param roleKey 权限字符
 * @param roleSort 显示顺序
 * @param dataScope 数据范围
 * @param menuCheckStrictly 菜单树是否严格关联
 * @param deptCheckStrictly 部门树是否严格关联
 * @param status 状态
 * @param delFlag 删除标志
 * @param flag 当前用户是否拥有角色
 * @param menuIds 菜单 ID 集合
 * @param deptIds 部门 ID 集合
 * @param permissions 权限集合
 * @param createBy 创建者
 * @param createTime 创建时间
 * @param updateBy 更新者
 * @param updateTime 更新时间
 * @param remark 备注
 * @author manzhushaka
 * @date 2026-07-18
 */
public record RoleResult(Long roleId, String roleName, String roleKey, Integer roleSort,
        String dataScope, boolean menuCheckStrictly, boolean deptCheckStrictly,
        String status, String delFlag, boolean flag, Long[] menuIds, Long[] deptIds,
        Set<String> permissions, String createBy, Date createTime, String updateBy,
        Date updateTime, String remark)
{
}
