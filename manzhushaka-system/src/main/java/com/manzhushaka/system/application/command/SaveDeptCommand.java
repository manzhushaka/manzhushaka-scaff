package com.manzhushaka.system.application.command;

/**
 * 保存部门命令。
 *
 * @param deptId 部门 ID
 * @param parentId 父部门 ID
 * @param deptName 部门名称
 * @param orderNum 显示顺序
 * @param leader 负责人
 * @param phone 联系电话
 * @param email 邮箱
 * @param status 状态
 * @param deptType 部门类型
 * @param regionCode 区划代码
 * @param regionLevel 区划级别
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveDeptCommand(Long deptId, Long parentId, String deptName,
        Integer orderNum, String leader, String phone, String email, String status,
        String deptType, String regionCode, Integer regionLevel)
{
}
