package com.manzhushaka.system.application.query;

/**
 * 慢 SQL 日志查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SlowSqlLogQuery(String mapperId, String sqlText, String dataSourceName,
        Long costTime, String beginTime, String endTime)
{
}
