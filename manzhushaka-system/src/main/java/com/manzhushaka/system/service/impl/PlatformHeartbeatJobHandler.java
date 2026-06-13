package com.manzhushaka.system.service.impl;

import com.manzhushaka.framework.job.JobLogger;
import com.manzhushaka.framework.job.PlatformJobExecutionContext;
import com.manzhushaka.framework.job.PlatformJobHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 实现 PlatformHeartbeatJobHandler 业务服务。
 */
@Component
public class PlatformHeartbeatJobHandler implements PlatformJobHandler {

    /**
     * 处理 handler Name 流程。
     *
     * @return 处理结果
     */
    @Override
    public String handlerName() {
        return "platformHeartbeatJob";
    }

    /**
     * 处理 handler Label 流程。
     *
     * @return 处理结果
     */
    @Override
    public String handlerLabel() {
        return "平台心跳任务";
    }

    /**
     * 执行任务处理。
     *
     * @param context 执行上下文
     */
    @Override
    public void execute(PlatformJobExecutionContext context) {
        JobLogger.info("平台心跳任务开始执行，任务名称：{}", context.getJobName());
        JobLogger.info("当前触发方式：{}", context.getTriggerType());
        JobLogger.info("任务参数：{}", context.getJobParam());
        JobLogger.info("当前平台时间：{}", LocalDateTime.now());
        JobLogger.info("平台心跳任务执行完成。");
    }
}
