package com.manzhushaka.system.service;

import java.util.List;
import com.manzhushaka.system.domain.SysSlowSqlLog;

/**
 * 慢 SQL 日志 服务层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface ISysSlowSqlLogService
{
    /**
     * 新增慢 SQL 日志。
     *
     * @param slowSqlLog 慢 SQL 日志
     */
    void insertSlowSqlLog(SysSlowSqlLog slowSqlLog);

    /**
     * 查询慢 SQL 日志列表。
     *
     * @param slowSqlLog 慢 SQL 日志查询条件
     * @return 慢 SQL 日志列表
     */
    List<SysSlowSqlLog> selectSlowSqlLogList(SysSlowSqlLog slowSqlLog);

    /**
     * 查询慢 SQL 日志详情。
     *
     * @param slowSqlId 慢 SQL 日志 ID
     * @return 慢 SQL 日志
     */
    SysSlowSqlLog selectSlowSqlLogById(Long slowSqlId);

    /**
     * 批量删除慢 SQL 日志。
     *
     * @param slowSqlIds 慢 SQL 日志 ID 数组
     * @return 影响行数
     */
    int deleteSlowSqlLogByIds(Long[] slowSqlIds);

    /**
     * 清空慢 SQL 日志。
     */
    void cleanSlowSqlLog();
}
