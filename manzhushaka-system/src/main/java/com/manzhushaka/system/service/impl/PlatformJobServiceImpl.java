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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 实现 PlatformJobServiceImpl 业务服务。
 */
@Service
public class PlatformJobServiceImpl implements PlatformJobService {
    /**
     * 执行 of Pattern 逻辑。
     *
     * @param HH:mm:ss" HH:mm:ss" 参数
     * @return 处理结果
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 执行 system Default 逻辑。
     *
     * @return 处理结果
     */
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

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
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

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @Override
    public PlatformJobVO getById(Long id) {
        SysJob job = requireJob(id);
        return toJobVO(job);
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
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

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
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

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireJob(id);
        platformJobScheduler.delete(id);
        if (jobLogMapper != null) {
            jobLogMapper.delete(new LambdaQueryWrapper<SysJobLog>().eq(SysJobLog::getJobId, id));
        }
        jobMapper.deleteById(id);
    }

    /**
     * 暂停任务。
     *
     * @param id 主键 ID
     */
    @Override
    public void pause(Long id) {
        SysJob job = requireJob(id);
        job.setStatus(0);
        job.setNextTriggerTime(null);
        job.setUpdateBy(currentOperatorName());
        jobMapper.updateById(job);
        platformJobScheduler.pause(id);
    }

    /**
     * 恢复任务。
     *
     * @param id 主键 ID
     */
    @Override
    public void resume(Long id) {
        SysJob job = requireJob(id);
        job.setStatus(1);
        job.setNextTriggerTime(computeNextTriggerTime(job.getCronExpression()));
        job.setUpdateBy(currentOperatorName());
        jobMapper.updateById(job);
        platformJobScheduler.resume(id);
    }

    /**
     * 触发任务执行。
     *
     * @param id 主键 ID
     */
    @Override
    public void trigger(Long id) {
        requireJob(id);
        platformJobScheduler.triggerNow(id);
    }

    /**
     * 处理 handler Options 流程。
     *
     * @return 处理结果
     */
    @Override
    public List<LabelValueOption> handlerOptions() {
        return handlerRegistry.list().stream()
            .map(handler -> new LabelValueOption(handler.handlerLabel(), handler.handlerName()))
            .toList();
    }

    /**
     * 查询 page Logs 结果。
     *
     * @param jobId jobId 标识
     * @param query 查询条件
     * @return 查询结果
     */
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

    /**
     * 返回 logDetail。
     *
     * @param id 主键 ID
     * @return 字段值
     */
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

    /**
     * 查询 list All Definitions 结果。
     *
     * @return 查询结果
     */
    @Override
    public List<PlatformJobDefinition> listAllDefinitions() {
        return jobMapper.selectList(new LambdaQueryWrapper<SysJob>().orderByAsc(SysJob::getId))
            .stream()
            .map(this::toDefinition)
            .toList();
    }

    /**
     * 校验表单参数。
     *
     * @param form 表单参数
     */
    private void validateForm(PlatformJobForm form) {
        handlerRegistry.getRequired(form.getHandlerName());
        if (!CronExpression.isValidExpression(form.getCronExpression())) {
            throw new BizException(400, "Cron 表达式不合法");
        }
        if (form.getStatus() == null || (form.getStatus() != 0 && form.getStatus() != 1)) {
            throw new BizException(400, "任务状态不合法");
        }
    }

    /**
     * 更新 fill Job Entity 数据。
     *
     * @param entity 实体对象
     * @param form 表单参数
     */
    private void fillJobEntity(SysJob entity, PlatformJobForm form) {
        entity.setJobName(form.getJobName().trim());
        entity.setHandlerName(form.getHandlerName().trim());
        entity.setCronExpression(form.getCronExpression().trim());
        entity.setStatus(form.getStatus());
        entity.setJobParam(StringUtils.hasText(form.getJobParam()) ? form.getJobParam().trim() : null);
        entity.setRemark(StringUtils.hasText(form.getRemark()) ? form.getRemark().trim() : null);
    }

    /**
     * 更新 sync Scheduler 数据。
     *
     * @param job job 参数
     */
    private void syncScheduler(SysJob job) {
        platformJobScheduler.scheduleOrUpdate(toDefinition(job));
        if (job.getStatus() != null && job.getStatus() == 1) {
            platformJobScheduler.resume(job.getId());
        } else {
            platformJobScheduler.pause(job.getId());
        }
    }

    /**
     * 校验 require Job 条件。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    private SysJob requireJob(Long id) {
        SysJob job = jobMapper.selectById(id);
        if (job == null) {
            throw new BizException(404, "定时任务不存在");
        }
        return job;
    }

    /**
     * 构建 to Job VO 结果。
     *
     * @param job job 参数
     * @return 处理结果
     */
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

    /**
     * 构建 to Definition 结果。
     *
     * @param job job 参数
     * @return 处理结果
     */
    private PlatformJobDefinition toDefinition(SysJob job) {
        PlatformJobDefinition definition = new PlatformJobDefinition();
        definition.setJobId(job.getId());
        definition.setHandlerName(job.getHandlerName());
        definition.setCronExpression(job.getCronExpression());
        definition.setStatus(job.getStatus());
        return definition;
    }

    /**
     * 构建 to Log VO 结果。
     *
     * @param log log 参数
     * @return 处理结果
     */
    private PlatformJobLogVO toLogVO(SysJobLog log) {
        PlatformJobLogVO vo = new PlatformJobLogVO();
        copyLogSummary(log, vo);
        return vo;
    }

    /**
     * 构建 copy Log Summary 结果。
     *
     * @param log log 参数
     * @param vo vo 参数
     */
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

    /**
     * 构建 resolve Handler Label 结果。
     *
     * @param handlerName handlerName 参数
     * @return 处理结果
     */
    private String resolveHandlerLabel(String handlerName) {
        for (PlatformJobHandler handler : handlerRegistry.list()) {
            if (handler.handlerName().equals(handlerName)) {
                return handler.handlerLabel();
            }
        }
        return handlerName;
    }

    /**
     * 执行 compute Next Trigger Time 逻辑。
     *
     * @param cronExpression cronExpression 参数
     * @return 处理结果
     */
    private LocalDateTime computeNextTriggerTime(String cronExpression) {
        try {
            CronExpression expression = new CronExpression(cronExpression);
            Date next = expression.getNextValidTimeAfter(new Date());
            return next == null ? null : LocalDateTime.ofInstant(next.toInstant(), SYSTEM_ZONE);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * 查询 current Operator Name 结果。
     *
     * @return 查询结果
     */
    private String currentOperatorName() {
        LoginUser loginUser = LoginUserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }

    /**
     * 格式化日期时间。
     *
     * @param value 字段值
     * @return 处理结果
     */
    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }
}
