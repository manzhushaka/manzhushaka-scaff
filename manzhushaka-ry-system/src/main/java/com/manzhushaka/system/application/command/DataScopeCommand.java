package com.manzhushaka.system.application.command;

/**
 * 数据权限命令
 *
 * @param roleId   角色ID
 * @param dataScope 数据范围
 * @param deptIds  部门组（数据权限）
 */
public record DataScopeCommand(
        Long roleId,
        String dataScope,
        Long[] deptIds
)
{
}