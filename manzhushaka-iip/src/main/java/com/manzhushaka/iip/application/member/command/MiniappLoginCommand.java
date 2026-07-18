package com.manzhushaka.iip.application.member.command;

/**
 * 小程序登录命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MiniappLoginCommand(String platform, String code, String nickname, String avatar)
{
}
