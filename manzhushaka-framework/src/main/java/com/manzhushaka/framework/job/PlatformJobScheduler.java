package com.manzhushaka.framework.job;

/**
 * 定义 PlatformJobScheduler。
 */
public interface PlatformJobScheduler {

    /**
     * 执行 schedule Or Update 逻辑。
     *
     * @param definition definition 参数
     */
    void scheduleOrUpdate(PlatformJobDefinition definition);

    /**
     * 删除数据。
     *
     * @param jobId jobId 标识
     */
    void delete(Long jobId);

    /**
     * 暂停任务。
     *
     * @param jobId jobId 标识
     */
    void pause(Long jobId);

    /**
     * 恢复任务。
     *
     * @param jobId jobId 标识
     */
    void resume(Long jobId);

    /**
     * 执行 trigger Now 操作。
     *
     * @param jobId jobId 标识
     */
    void triggerNow(Long jobId);
}
