package com.manzhushaka.framework.job;

/**
 * 定义 PlatformJobDispatchService。
 */
public interface PlatformJobDispatchService {

    /**
     * 分发任务。
     *
     * @param jobId jobId 标识
     * @param triggerType triggerType 参数
     * @param jobParamOverride jobParamOverride 参数
     */
    void dispatch(Long jobId, String triggerType, String jobParamOverride);
}
