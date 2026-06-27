package com.manzhushaka.system.application.command;

/**
 * 取消授权用户命令
 *
 * @param roleId  角色ID
 * @param userIds 用户ID列表
 */
public record CancelAuthUserCommand(
        Long roleId,
        Long[] userIds
)
{
}