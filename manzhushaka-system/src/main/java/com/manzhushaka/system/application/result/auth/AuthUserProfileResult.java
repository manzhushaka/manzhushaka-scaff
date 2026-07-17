package com.manzhushaka.system.application.result.auth;

import java.util.Date;
import java.util.Set;

/**
 * 用户认证信息查询结果
 * <p>
 * 供 {@code framework} 和 {@code admin} 通过应用层读取认证所需信息，
 * 不再直接操作持久化实体 {@code SysUser}。
 * </p>
 *
 * @param userId       用户ID
 * @param deptId       部门ID
 * @param deptName     部门名称
 * @param username     用户名
 * @param nickName     用户昵称
 * @param avatar       用户头像
 * @param password     加密后的密码
 * @param status       账号状态（0正常 1停用）
 * @param delFlag      删除标志（0存在 2删除）
 * @param admin        是否为管理员
 * @param roleIds      角色ID集合
 * @param roleKeys     角色键集合
 * @param permissions  权限集合
 * @param pwdUpdateDate 密码最后更新时间
 *
 * @author manzhushaka
 */
public record AuthUserProfileResult(
        Long userId,
        Long deptId,
        String deptName,
        String username,
        String nickName,
        String avatar,
        String password,
        String status,
        String delFlag,
        boolean admin,
        Set<Long> roleIds,
        Set<String> roleKeys,
        Set<String> permissions,
        Date pwdUpdateDate) {
}
