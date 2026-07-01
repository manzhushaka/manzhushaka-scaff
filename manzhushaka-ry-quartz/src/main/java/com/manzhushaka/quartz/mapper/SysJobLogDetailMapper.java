package com.manzhushaka.quartz.mapper;

import java.util.List;

import com.manzhushaka.quartz.domain.SysJobLogDetail;

/**
 * 定时任务调度过程日志明细 数据层。
 *
 * @author manzhushaka
 * @date 2026-06-30
 */
public interface SysJobLogDetailMapper
{
    /**
     * 批量新增定时任务调度过程日志明细。
     *
     * @param details 过程日志明细列表
     * @return 影响行数
     */
    int insertJobLogDetails(List<SysJobLogDetail> details);

    /**
     * 根据任务日志ID查询过程日志明细列表。
     *
     * @param jobLogId 任务日志ID
     * @return 过程日志明细列表
     */
    List<SysJobLogDetail> selectJobLogDetailListByJobLogId(Long jobLogId);

    /**
     * 根据任务日志ID批量删除过程日志明细。
     *
     * @param jobLogIds 任务日志ID数组
     * @return 影响行数
     */
    int deleteJobLogDetailByJobLogIds(Long[] jobLogIds);

    /**
     * 根据任务日志ID删除过程日志明细。
     *
     * @param jobLogId 任务日志ID
     * @return 影响行数
     */
    int deleteJobLogDetailByJobLogId(Long jobLogId);

    /**
     * 清空定时任务调度过程日志明细。
     */
    void cleanJobLogDetail();
}
