package com.manzhushaka.system.mapper;

import java.util.List;
import com.manzhushaka.system.domain.SysRequestLog;

/**
 * 请求日志 数据层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface SysRequestLogMapper
{
    /**
     * 新增请求日志。
     *
     * @param requestLog 请求日志
     */
    void insertRequestLog(SysRequestLog requestLog);

    /**
     * 查询请求日志列表。
     *
     * @param requestLog 请求日志查询条件
     * @return 请求日志列表
     */
    List<SysRequestLog> selectRequestLogList(SysRequestLog requestLog);

    /**
     * 查询请求日志详情。
     *
     * @param requestId 请求日志 ID
     * @return 请求日志
     */
    SysRequestLog selectRequestLogById(Long requestId);

    /**
     * 批量删除请求日志。
     *
     * @param requestIds 请求日志 ID 数组
     * @return 影响行数
     */
    int deleteRequestLogByIds(Long[] requestIds);

    /**
     * 清空请求日志。
     */
    void cleanRequestLog();
}
