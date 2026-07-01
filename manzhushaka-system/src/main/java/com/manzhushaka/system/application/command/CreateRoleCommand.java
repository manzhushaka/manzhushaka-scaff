package com.manzhushaka.system.application.command;

/**
 * 创建角色命令
 *
 * @param roleId   角色ID
 * @param roleName 角色名称
 * @param roleKey  角色权限
 * @param roleSort 角色排序
 * @param dataScope 数据范围
 * @param status   状态（0正常 1停用）
 * @param menuIds  菜单组
 * @param deptIds  部门组（数据权限）
 * @param remark   备注
 */
public record CreateRoleCommand(
        Long roleId,
        String roleName,
        String roleKey,
        Integer roleSort,
        String dataScope,
        String status,
        Long[] menuIds,
        Long[] deptIds,
        String remark
)
{
}