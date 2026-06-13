package com.manzhushaka.system.service.impl;

import com.manzhushaka.framework.job.PlatformJobDefinition;
import com.manzhushaka.framework.job.PlatformJobScheduler;
import com.manzhushaka.system.service.PlatformJobService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 实现 PlatformJobBootstrap 业务服务。
 */
@Component
public class PlatformJobBootstrap implements ApplicationRunner {
    private final PlatformJobService platformJobService;
    private final PlatformJobScheduler platformJobScheduler;

    /**
     * 创建 PlatformJobBootstrap 实例。
     *
     * @param platformJobService platformJobService 参数
     * @param platformJobScheduler platformJobScheduler 参数
     */
    public PlatformJobBootstrap(PlatformJobService platformJobService, PlatformJobScheduler platformJobScheduler) {
        this.platformJobService = platformJobService;
        this.platformJobScheduler = platformJobScheduler;
    }

    /**
     * 执行 run 操作。
     *
     * @param args args 参数
     */
    @Override
    public void run(ApplicationArguments args) {
        for (PlatformJobDefinition definition : platformJobService.listAllDefinitions()) {
            platformJobScheduler.scheduleOrUpdate(definition);
            if (definition.getJobId() == null) {
                continue;
            }
            if (definition.getStatus() != null && definition.getStatus() == 1) {
                platformJobScheduler.resume(definition.getJobId());
            } else {
                platformJobScheduler.pause(definition.getJobId());
            }
        }
    }
}
