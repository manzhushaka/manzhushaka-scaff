package com.manzhushaka.system.application.command;

/**
 * 修改用户命令
 *
 * @param userId      用户ID
 * @param username    用户账号
 * @param nickname    用户昵称
 * @param phonenumber 手机号码
 * @param email       邮箱
 * @param sex         性别
 * @param status      状态（0正常 1停用）
 * @param deptId      部门ID
 * @param roleIds     角色组
 * @param postIds     岗位组
 */
public record UpdateUserCommand(
        Long userId,
        String username,
        String nickname,
        String phonenumber,
        String email,
        String sex,
        String status,
        Long deptId,
        Long[] roleIds,
        Long[] postIds
)
{
}