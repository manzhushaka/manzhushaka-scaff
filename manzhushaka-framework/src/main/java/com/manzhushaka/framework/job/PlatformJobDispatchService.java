package com.manzhushaka.framework.job;

public interface PlatformJobDispatchService {

    void dispatch(Long jobId, String triggerType, String jobParamOverride);
}
