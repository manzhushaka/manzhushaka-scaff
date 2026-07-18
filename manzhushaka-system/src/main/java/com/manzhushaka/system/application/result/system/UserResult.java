package com.manzhushaka.system.application.result.system;

import java.util.Date;
import java.util.List;

/**
 * 用户查询结果。
 *
 * @param userId 用户 ID
 * @param deptId 部门 ID
 * @param userName 登录账号
 * @param nickName 用户昵称
 * @param email 邮箱
 * @param phonenumber 手机号
 * @param sex 性别
 * @param avatar 头像
 * @param status 状态
 * @param delFlag 删除标志
 * @param loginIp 最后登录 IP
 * @param loginDate 最后登录时间
 * @param dept 部门信息
 * @param roles 角色列表
 * @param roleIds 角色 ID 集合
 * @param roleId 查询角色 ID
 * @param createBy 创建者
 * @param createTime 创建时间
 * @param updateBy 更新者
 * @param updateTime 更新时间
 * @param remark 备注
 * @author manzhushaka
 * @date 2026-07-18
 */
public record UserResult(Long userId, Long deptId, String userName, String nickName,
        String email, String phonenumber, String sex, String avatar, String status,
        String delFlag, String loginIp, Date loginDate, DeptResult dept,
        List<RoleResult> roles, Long[] roleIds, Long roleId, String createBy,
        Date createTime, String updateBy, Date updateTime, String remark)
{
    @Override
    public String toString()
    {
        return "UserResult[userId=" + userId + ", deptId=" + deptId + ", userName=" + userName
                + ", nickName=" + nickName + ", status=" + status + "]";
    }
}
