package com.manzhushaka.framework.job;

public interface PlatformJobScheduler {

    void scheduleOrUpdate(PlatformJobDefinition definition);

    void delete(Long jobId);

    void pause(Long jobId);

    void resume(Long jobId);

    void triggerNow(Long jobId);
}
