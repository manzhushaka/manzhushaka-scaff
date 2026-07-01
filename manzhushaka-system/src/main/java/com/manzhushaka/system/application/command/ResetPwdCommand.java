package com.manzhushaka.system.application.command;

/**
 * 重置密码命令
 *
 * @param userId   用户ID
 * @param password 新密码
 */
public record ResetPwdCommand(
        Long userId,
        String password
)
{
}