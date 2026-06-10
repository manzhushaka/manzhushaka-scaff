package com.manzhushaka.system.service.impl;

import com.manzhushaka.framework.job.PlatformJobDefinition;
import com.manzhushaka.framework.job.PlatformJobScheduler;
import com.manzhushaka.system.service.PlatformJobService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PlatformJobBootstrap implements ApplicationRunner {
    private final PlatformJobService platformJobService;
    private final PlatformJobScheduler platformJobScheduler;

    public PlatformJobBootstrap(PlatformJobService platformJobService, PlatformJobScheduler platformJobScheduler) {
        this.platformJobService = platformJobService;
        this.platformJobScheduler = platformJobScheduler;
    }

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
