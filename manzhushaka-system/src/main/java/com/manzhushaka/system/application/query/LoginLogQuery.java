package com.manzhushaka.system.application.query;

/**
 * 登录日志查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record LoginLogQuery(String ipaddr, String status, String userName,
        String beginTime, String endTime)
{
}
