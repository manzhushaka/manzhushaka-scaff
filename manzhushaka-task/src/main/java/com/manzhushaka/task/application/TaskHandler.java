package com.manzhushaka.task.application;

/**
 * 业务导入或导出任务处理器。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public interface TaskHandler
{
    /**
     * 返回处理器唯一标识。
     *
     * @return 处理器类型
     */
    String handlerType();

    /**
     * 执行任务。
     *
     * @param context 任务上下文
     * @throws Exception 执行失败
     */
    void execute(TaskContext<?> context) throws Exception;
}
