package com.manzhushaka.quartz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.quartz.domain.SysJobLog;
import com.manzhushaka.quartz.domain.SysJobLogDetail;
import com.manzhushaka.quartz.mapper.SysJobLogDetailMapper;
import com.manzhushaka.quartz.mapper.SysJobLogMapper;
import com.manzhushaka.quartz.service.ISysJobLogService;

/**
 * 定时任务调度日志信息 服务层
 * 
 * @author manzhushaka
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService
{
    @Autowired
    private SysJobLogMapper jobLogMapper;

    @Autowired
    private SysJobLogDetailMapper jobLogDetailMapper;

    /**
     * 获取quartz调度器日志的计划任务
     * 
     * @param jobLog 调度日志信息
     * @return 调度任务日志集合
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog)
    {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    /**
     * 通过调度任务日志ID查询调度信息
     * 
     * @param jobLogId 调度任务日志ID
     * @return 调度任务日志对象信息
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId)
    {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    /**
     * 新增任务日志
     * 
     * @param jobLog 调度日志信息
     */
    @Override
    public SysJobLog addJobLog(SysJobLog jobLog)
    {
        jobLogMapper.insertJobLog(jobLog);
        return jobLog;
    }

    /**
     * 批量新增任务过程日志明细
     *
     * @param details 过程日志明细列表
     */
    @Override
    public void addJobLogDetails(List<SysJobLogDetail> details)
    {
        if (StringUtils.isEmpty(details))
        {
            return;
        }
        jobLogDetailMapper.insertJobLogDetails(details);
    }

    /**
     * 根据调度任务日志ID查询过程日志明细
     *
     * @param jobLogId 调度任务日志ID
     * @return 过程日志明细集合
     */
    @Override
    public List<SysJobLogDetail> selectJobLogDetailListByJobLogId(Long jobLogId)
    {
        return jobLogDetailMapper.selectJobLogDetailListByJobLogId(jobLogId);
    }

    /**
     * 批量删除调度日志信息
     * 
     * @param logIds 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteJobLogByIds(Long[] logIds)
    {
        jobLogDetailMapper.deleteJobLogDetailByJobLogIds(logIds);
        return jobLogMapper.deleteJobLogByIds(logIds);
    }

    /**
     * 删除任务日志
     * 
     * @param jobId 调度日志ID
     */
    @Override
    public int deleteJobLogById(Long jobId)
    {
        jobLogDetailMapper.deleteJobLogDetailByJobLogId(jobId);
        return jobLogMapper.deleteJobLogById(jobId);
    }

    /**
     * 清空任务日志
     */
    @Override
    public void cleanJobLog()
    {
        jobLogDetailMapper.cleanJobLogDetail();
        jobLogMapper.cleanJobLog();
    }
}
