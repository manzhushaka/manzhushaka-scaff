package com.manzhushaka.system.application.result.system;

import java.util.Date;
import java.util.List;

/**
 * 部门查询结果。
 *
 * @param deptId 部门 ID
 * @param parentId 父部门 ID
 * @param ancestors 祖级列表
 * @param deptName 部门名称
 * @param orderNum 显示顺序
 * @param leader 负责人
 * @param phone 联系电话
 * @param email 邮箱
 * @param status 状态
 * @param delFlag 删除标志
 * @param deptType 部门类型
 * @param regionCode 区划代码
 * @param regionLevel 区划级别
 * @param parentName 父部门名称
 * @param createBy 创建者
 * @param createTime 创建时间
 * @param updateBy 更新者
 * @param updateTime 更新时间
 * @param remark 备注
 * @param children 子部门
 * @author manzhushaka
 * @date 2026-07-18
 */
public record DeptResult(Long deptId, Long parentId, String ancestors, String deptName,
        Integer orderNum, String leader, String phone, String email, String status,
        String delFlag, String deptType, String regionCode, Integer regionLevel,
        String parentName, String createBy, Date createTime, String updateBy,
        Date updateTime, String remark, List<DeptResult> children)
{
}
