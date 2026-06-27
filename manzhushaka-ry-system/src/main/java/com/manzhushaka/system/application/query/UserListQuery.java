package com.manzhushaka.system.application.query;

import java.util.Date;

/**
 * 用户列表查询
 *
 * @param pageNum    页码
 * @param pageSize   每页条数
 * @param userName   用户账号
 * @param phonenumber 手机号码
 * @param status     状态（0正常 1停用）
 * @param deptId     部门ID
 * @param beginTime  开始时间
 * @param endTime    结束时间
 */
public record UserListQuery(
        Integer pageNum,
        Integer pageSize,
        String userName,
        String phonenumber,
        String status,
        Long deptId,
        Date beginTime,
        Date endTime
)
{
}