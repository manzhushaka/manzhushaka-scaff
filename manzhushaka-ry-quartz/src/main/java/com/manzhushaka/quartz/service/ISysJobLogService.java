package com.manzhushaka.quartz.service;

import java.util.List;
import com.manzhushaka.quartz.domain.SysJobLog;
import com.manzhushaka.quartz.domain.SysJobLogDetail;

/**
 * 定时任务调度日志信息信息 服务层
 * 
 * @author manzhushaka
 */
public interface ISysJobLogService
{
    /**
     * 获取quartz调度器日志的计划任务
     * 
     * @param jobLog 调度日志信息
     * @return 调度任务日志集合
     */
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 通过调度任务日志ID查询调度信息
     * 
     * @param jobLogId 调度任务日志ID
     * @return 调度任务日志对象信息
     */
    public SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 新增任务日志
     * 
     * @param jobLog 调度日志信息
     */
    public SysJobLog addJobLog(SysJobLog jobLog);

    /**
     * 批量新增任务过程日志明细
     *
     * @param details 过程日志明细列表
     */
    public void addJobLogDetails(List<SysJobLogDetail> details);

    /**
     * 根据调度任务日志ID查询过程日志明细
     *
     * @param jobLogId 调度任务日志ID
     * @return 过程日志明细集合
     */
    public List<SysJobLogDetail> selectJobLogDetailListByJobLogId(Long jobLogId);

    /**
     * 批量删除调度日志信息
     * 
     * @param logIds 需要删除的日志ID
     * @return 结果
     */
    public int deleteJobLogByIds(Long[] logIds);

    /**
     * 删除任务日志
     * 
     * @param jobId 调度日志ID
     * @return 结果
     */
    public int deleteJobLogById(Long jobId);

    /**
     * 清空任务日志
     */
    public void cleanJobLog();
}
