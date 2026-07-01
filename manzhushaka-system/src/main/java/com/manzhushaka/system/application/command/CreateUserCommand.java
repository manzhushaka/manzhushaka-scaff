package com.manzhushaka.system.application.command;

/**
 * 创建用户命令
 *
 * @param userId   用户ID
 * @param username 用户账号
 * @param password 密码
 * @param nickname 用户昵称
 * @param phonenumber 手机号码
 * @param email   邮箱
 * @param sex     性别
 * @param avatar  头像
 * @param status  状态（0正常 1停用）
 * @param deptId  部门ID
 * @param roleIds 角色组
 */
public record CreateUserCommand(
        Long userId,
        String username,
        String password,
        String nickname,
        String phonenumber,
        String email,
        String sex,
        String avatar,
        String status,
        Long deptId,
        Long[] roleIds
)
{
}
