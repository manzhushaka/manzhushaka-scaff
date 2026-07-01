package com.manzhushaka.framework.web.command;

/**
 * 注册命令
 *
 * @param username 用户名
 * @param password 密码
 * @param code     验证码
 * @param uuid     唯一标识
 */
public record RegisterCommand(String username, String password, String code, String uuid)
{
}