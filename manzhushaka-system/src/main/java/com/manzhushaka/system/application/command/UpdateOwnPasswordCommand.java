package com.manzhushaka.system.application.command;

/**
 * 修改当前用户密码命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record UpdateOwnPasswordCommand(Long userId, String username,
        String oldPassword, String newPassword)
{
    @Override
    public String toString()
    {
        return "UpdateOwnPasswordCommand[userId=" + userId + ", username=" + username + "]";
    }
}
