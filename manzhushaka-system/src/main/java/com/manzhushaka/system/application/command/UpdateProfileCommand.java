package com.manzhushaka.system.application.command;

/**
 * 更新个人资料命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record UpdateProfileCommand(Long userId, String username, String nickName,
        String email, String phonenumber, String sex)
{
}
