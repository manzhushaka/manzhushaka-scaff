package com.manzhushaka.system.application.command;

/**
 * 修改用户状态命令
 *
 * @param userId 用户ID
 * @param status 状态（0正常 1停用）
 */
public record ChangeUserStatusCommand(
        Long userId,
        String status
)
{
}