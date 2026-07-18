package com.manzhushaka.iip.application.member.command;

/**
 * 修改用户状态命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ChangeMemberStatusCommand(Long memberId, String status)
{
}
