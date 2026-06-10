package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.context.LoginUserContext;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysJob;
import com.manzhushaka.db.system.entity.SysJobLog;
import com.manzhushaka.db.system.mapper.SysJobLogMapper;
import com.manzhushaka.db.system.mapper.SysJobMapper;
import com.manzhushaka.framework.job.PlatformJobDefinition;
import com.manzhushaka.framework.job.PlatformJobHandler;
import com.manzhushaka.framework.job.PlatformJobHandlerRegistry;
import com.manzhushaka.framework.job.PlatformJobScheduler;
import com.manzhushaka.system.dto.job.PlatformJobForm;
import com.manzhushaka.system.dto.job.PlatformJobLogQuery;
import com.manzhushaka.system.dto.job.PlatformJobQuery;
import com.manzhushaka.system.service.PlatformJobService;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.job.PlatformJobLogDetailVO;
import com.manzhushaka.system.vo.job.PlatformJobLogVO;
import com.manzhushaka.system.vo.job.PlatformJobVO;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class PlatformJobServiceImpl implements PlatformJobService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
    private final SysJobMapper jobMapper;
    private final SysJobLogMapper jobLogMapper;
    private final PlatformJobScheduler platformJobScheduler;
    private final PlatformJobHandlerRegistry handlerRegistry;

    @Autowired
    public PlatformJobServiceImpl(
        SysJobMapper jobMapper,
        SysJobLogMapper jobLogMapper,
        PlatformJobScheduler platformJobScheduler,
        PlatformJobHandlerRegistry handlerRegistry
    ) {
        this.jobMapper = jobMapper;
        this.jobLogMapper = jobLogMapper;
        this.platformJobScheduler = platformJobScheduler;
        this.handlerRegistry = handlerRegistry;
    }

    public PlatformJobServiceImpl(
        SysJobMapper jobMapper,
        PlatformJobScheduler platformJobScheduler,
        PlatformJobHandlerRegistry handlerRegistry
    ) {
        this(jobMapper, null, platformJobScheduler, handlerRegistry);
    }

    @Override
    public PageResult<PlatformJobVO> page(PlatformJobQuery query) {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<SysJob>()
            .like(StringUtils.hasText(query.getJobName()), SysJob::getJobName, query.getJobName())
            .like(StringUtils.hasText(query.getHandlerName()), SysJob::getHandlerName, query.getHandlerName())
            .eq(query.getStatus() != null, SysJob::getStatus, query.getStatus())
            .eq(StringUtils.hasText(query.getLastRunStatus()), SysJob::getLastRunStatus, query.getLastRunStatus())
            .orderByDesc(SysJob::getId);
        Page<SysJob> page = jobMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toJobVO);
    }

    @Override
    public PlatformJobVO getById(Long id) {
        SysJob job = requireJob(id);
        return toJobVO(job);
    }

    @Override
    public Long create(PlatformJobForm form) {
        validateForm(form);
        SysJob entity = new SysJob();
        fillJobEntity(entity, form);
        entity.setCreateBy(currentOperatorName());
        entity.setUpdateBy(currentOperatorName());
        entity.setNextTriggerTime(form.getStatus() != null && form.getStatus() == 1 ? computeNextTriggerTime(form.getCronExpression()) : null);
        jobMapper.insert(entity);
        syncScheduler(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, PlatformJobForm form) {
        validateForm(form);
        SysJob existing = requireJob(id);
        fillJobEntity(existing, form);
        existing.setId(id);
        existing.setUpdateBy(currentOperatorName());
        existing.setNextTriggerTime(form.getStatus() != null && form.getStatus() == 1 ? computeNextTriggerTime(form.getCronExpression()) : null);
        jobMapper.updateById(existing);
        syncScheduler(existing);
    }

    @Override
    public void delete(Long id) {
        requireJob(id);
        platformJobScheduler.delete(id);
        if (jobLogMapper != null) {
            jobLogMapper.delete(new LambdaQueryWrapper<SysJobLog>().eq(SysJobLog::getJobId, id));
        }
        jobMapper.deleteById(id);
    }

    @Override
    public void pause(Long id) {
        SysJob job = requireJob(id);
        job.setStatus(0);
        job.setNextTriggerTime(null);
        job.setUpdateBy(currentOperatorName());
        jobMapper.updateById(job);
        platformJobScheduler.pause(id);
    }

    @Override
    public void resume(Long id) {
        SysJob job = requireJob(id);
        job.setStatus(1);
        job.setNextTriggerTime(computeNextTriggerTime(job.getCronExpression()));
        job.setUpdateBy(currentOperatorName());
        jobMapper.updateById(job);
        platformJobScheduler.resume(id);
    }

    @Override
    public void trigger(Long id) {
        requireJob(id);
        platformJobScheduler.triggerNow(id);
    }

    @Override
    public List<LabelValueOption> handlerOptions() {
        return handlerRegistry.list().stream()
            .map(handler -> new LabelValueOption(handler.handlerLabel(), handler.handlerName()))
            .toList();
    }

    @Override
    public PageResult<PlatformJobLogVO> pageLogs(Long jobId, PlatformJobLogQuery query) {
        if (jobLogMapper == null) {
            throw new BizException(500, "任务日志能力尚未初始化");
        }
        requireJob(jobId);
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<SysJobLog>()
            .eq(SysJobLog::getJobId, jobId)
            .eq(StringUtils.hasText(query.getRunStatus()), SysJobLog::getRunStatus, query.getRunStatus())
            .eq(StringUtils.hasText(query.getTriggerType()), SysJobLog::getTriggerType, query.getTriggerType())
            .orderByDesc(SysJobLog::getStartTime, SysJobLog::getId);
        Page<SysJobLog> page = jobLogMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toLogVO);
    }

    @Override
    public PlatformJobLogDetailVO getLogDetail(Long id) {
        if (jobLogMapper == null) {
            throw new BizException(500, "任务日志能力尚未初始化");
        }
        SysJobLog log = jobLogMapper.selectById(id);
        if (log == null) {
            throw new BizException(404, "任务执行日志不存在");
        }
        PlatformJobLogDetailVO detail = new PlatformJobLogDetailVO();
        copyLogSummary(log, detail);
        detail.setLogContent(log.getLogContent());
        return detail;
    }

    @Override
    public List<PlatformJobDefinition> listAllDefinitions() {
        return jobMapper.selectList(new LambdaQueryWrapper<SysJob>().orderByAsc(SysJob::getId))
            .stream()
            .map(this::toDefinition)
            .toList();
    }

    private void validateForm(PlatformJobForm form) {
        handlerRegistry.getRequired(form.getHandlerName());
        if (!CronExpression.isValidExpression(form.getCronExpression())) {
            throw new BizException(400, "Cron 表达式不合法");
        }
        if (form.getStatus() == null || (form.getStatus() != 0 && form.getStatus() != 1)) {
            throw new BizException(400, "任务状态不合法");
        }
    }

    private void fillJobEntity(SysJob entity, PlatformJobForm form) {
        entity.setJobName(form.getJobName().trim());
        entity.setHandlerName(form.getHandlerName().trim());
        entity.setCronExpression(form.getCronExpression().trim());
        entity.setStatus(form.getStatus());
        entity.setJobParam(StringUtils.hasText(form.getJobParam()) ? form.getJobParam().trim() : null);
        entity.setRemark(StringUtils.hasText(form.getRemark()) ? form.getRemark().trim() : null);
    }

    private void syncScheduler(SysJob job) {
        platformJobScheduler.scheduleOrUpdate(toDefinition(job));
        if (job.getStatus() != null && job.getStatus() == 1) {
            platformJobScheduler.resume(job.getId());
        } else {
            platformJobScheduler.pause(job.getId());
        }
    }

    private SysJob requireJob(Long id) {
        SysJob job = jobMapper.selectById(id);
        if (job == null) {
            throw new BizException(404, "定时任务不存在");
        }
        return job;
    }

    private PlatformJobVO toJobVO(SysJob job) {
        PlatformJobVO vo = new PlatformJobVO();
        vo.setId(job.getId());
        vo.setJobName(job.getJobName());
        vo.setHandlerName(job.getHandlerName());
        vo.setHandlerLabel(resolveHandlerLabel(job.getHandlerName()));
        vo.setCronExpression(job.getCronExpression());
        vo.setStatus(job.getStatus());
        vo.setJobParam(job.getJobParam());
        vo.setRemark(job.getRemark());
        vo.setLastRunStatus(job.getLastRunStatus());
        vo.setLastTriggerTime(formatDateTime(job.getLastTriggerTime()));
        vo.setNextTriggerTime(formatDateTime(job.getNextTriggerTime()));
        vo.setCreateTime(formatDateTime(job.getCreateTime()));
        return vo;
    }

    private PlatformJobDefinition toDefinition(SysJob job) {
        PlatformJobDefinition definition = new PlatformJobDefinition();
        definition.setJobId(job.getId());
        definition.setHandlerName(job.getHandlerName());
        definition.setCronExpression(job.getCronExpression());
        definition.setStatus(job.getStatus());
        return definition;
    }

    private PlatformJobLogVO toLogVO(SysJobLog log) {
        PlatformJobLogVO vo = new PlatformJobLogVO();
        copyLogSummary(log, vo);
        return vo;
    }

    private void copyLogSummary(SysJobLog log, PlatformJobLogVO vo) {
        vo.setId(log.getId());
        vo.setJobId(log.getJobId());
        vo.setJobNameSnapshot(log.getJobNameSnapshot());
        vo.setHandlerNameSnapshot(log.getHandlerNameSnapshot());
        vo.setTriggerType(log.getTriggerType());
        vo.setRunStatus(log.getRunStatus());
        vo.setExecutorHost(log.getExecutorHost());
        vo.setErrorMsg(log.getErrorMsg());
        vo.setCostMs(log.getCostMs());
        vo.setStartTime(formatDateTime(log.getStartTime()));
        vo.setEndTime(formatDateTime(log.getEndTime()));
        vo.setCreateTime(formatDateTime(log.getCreateTime()));
    }

    private String resolveHandlerLabel(String handlerName) {
        for (PlatformJobHandler handler : handlerRegistry.list()) {
            if (handler.handlerName().equals(handlerName)) {
                return handler.handlerLabel();
            }
        }
        return handlerName;
    }

    private LocalDateTime computeNextTriggerTime(String cronExpression) {
        try {
            CronExpression expression = new CronExpression(cronExpression);
            Date next = expression.getNextValidTimeAfter(new Date());
            return next == null ? null : LocalDateTime.ofInstant(next.toInstant(), SYSTEM_ZONE);
        } catch (Exception exception) {
            return null;
        }
    }

    private String currentOperatorName() {
        LoginUser loginUser = LoginUserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }
}
