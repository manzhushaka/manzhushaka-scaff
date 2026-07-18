package com.manzhushaka.system.application.query;

/**
 * 角色列表查询
 *
 * @param pageNum   页码
 * @param pageSize  每页条数
 * @param roleName  角色名称
 * @param roleKey   角色权限
 * @param status    状态（0正常 1停用）
 * @param beginTime 开始时间
 * @param endTime   结束时间
 * @author manzhushaka
 * @date 2026-07-18
 */
public record RoleListQuery(
        Integer pageNum,
        Integer pageSize,
        String roleName,
        String roleKey,
        String status,
        String beginTime,
        String endTime
)
{
}
