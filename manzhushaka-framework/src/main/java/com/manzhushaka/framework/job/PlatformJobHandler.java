package com.manzhushaka.framework.job;

/**
 * 定义 PlatformJobHandler。
 */
public interface PlatformJobHandler {

    /**
     * 处理 handler Name 流程。
     *
     * @return 处理结果
     */
    String handlerName();

    /**
     * 处理 handler Label 流程。
     *
     * @return 处理结果
     */
    String handlerLabel();

    /**
     * 执行任务处理。
     *
     * @param context 执行上下文
     */
    default void execute(PlatformJobExecutionContext context) throws Exception {
    }
}
