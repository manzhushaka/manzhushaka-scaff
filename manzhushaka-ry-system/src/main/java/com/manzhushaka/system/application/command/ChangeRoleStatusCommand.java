package com.manzhushaka.system.application.command;

/**
 * 修改角色状态命令
 *
 * @param roleId 角色ID
 * @param status 状态（0正常 1停用）
 */
public record ChangeRoleStatusCommand(
        Long roleId,
        String status
)
{
}