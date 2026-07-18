package com.manzhushaka.system.application.query;

/**
 * 部门查询条件。
 *
 * @param deptName 部门名称
 * @param status 状态
 * @param deptType 部门类型
 * @param regionCode 区划代码
 * @param regionLevel 区划级别
 * @author manzhushaka
 * @date 2026-07-18
 */
public record DeptQuery(String deptName, String status, String deptType,
        String regionCode, Integer regionLevel)
{
}
