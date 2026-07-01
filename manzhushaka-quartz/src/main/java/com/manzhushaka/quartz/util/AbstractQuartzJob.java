package com.manzhushaka.quartz.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.constant.ScheduleConstants;
import com.manzhushaka.common.utils.ExceptionUtil;
import com.manzhushaka.common.utils.JobLog;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.bean.BeanUtils;
import com.manzhushaka.common.utils.spring.SpringUtils;
import com.manzhushaka.quartz.domain.SysJob;
import com.manzhushaka.quartz.domain.SysJobLog;
import com.manzhushaka.quartz.domain.SysJobLogDetail;
import com.manzhushaka.quartz.service.ISysJobLogService;

/**
 * 抽象quartz调用
 *
 * @author manzhushaka
 */
public abstract class AbstractQuartzJob implements Job
{
    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    /**
     * 线程本地变量
     */
    private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context)
    {
        SysJob sysJob = new SysJob();
        BeanUtils.copyBeanProp(sysJob, context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES));
        try
        {
            before(context, sysJob);
            if (sysJob != null)
            {
                doExecute(context, sysJob);
            }
            after(context, sysJob, null);
        }
        catch (Exception e)
        {
            log.error("任务执行异常  - ：", e);
            after(context, sysJob, e);
        }
    }

    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     */
    protected void before(JobExecutionContext context, SysJob sysJob)
    {
        threadLocal.set(new Date());
        JobLog.start();
    }

    /**
     * 执行后
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     */
    protected void after(JobExecutionContext context, SysJob sysJob, Exception e)
    {
        Date startTime = threadLocal.get();
        threadLocal.remove();
        List<JobLog.Line> processLines = JobLog.getLines();

        final SysJobLog sysJobLog = new SysJobLog();
        sysJobLog.setJobName(sysJob.getJobName());
        sysJobLog.setJobGroup(sysJob.getJobGroup());
        sysJobLog.setInvokeTarget(sysJob.getInvokeTarget());
        sysJobLog.setStartTime(startTime);
        sysJobLog.setEndTime(new Date());
        long runMs = sysJobLog.getEndTime().getTime() - sysJobLog.getStartTime().getTime();
        sysJobLog.setJobMessage(sysJobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
        if (e != null)
        {
            sysJobLog.setStatus(Constants.FAIL);
            String errorMsg = StringUtils.substring(ExceptionUtil.getExceptionMessage(e), 0, 2000);
            sysJobLog.setExceptionInfo(errorMsg);
        }
        else
        {
            sysJobLog.setStatus(Constants.SUCCESS);
        }

        try
        {
            // 写入数据库当中
            ISysJobLogService jobLogService = SpringUtils.getBean(ISysJobLogService.class);
            jobLogService.addJobLog(sysJobLog);
            jobLogService.addJobLogDetails(buildJobLogDetails(sysJobLog.getJobLogId(), processLines));
        }
        finally
        {
            JobLog.clear();
        }
    }

    /**
     * 构建任务过程日志明细。
     *
     * @param jobLogId 任务日志ID
     * @param processLines 过程日志行
     * @return 任务过程日志明细列表
     */
    private List<SysJobLogDetail> buildJobLogDetails(Long jobLogId, List<JobLog.Line> processLines)
    {
        List<SysJobLogDetail> details = new ArrayList<>();
        if (jobLogId == null || StringUtils.isEmpty(processLines))
        {
            return details;
        }
        for (JobLog.Line processLine : processLines)
        {
            SysJobLogDetail detail = new SysJobLogDetail();
            detail.setJobLogId(jobLogId);
            detail.setLogLevel(processLine.getLogLevel());
            detail.setLogContent(processLine.getLogContent());
            detail.setSortNo(processLine.getSortNo());
            details.add(detail);
        }
        return details;
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;
}
