package com.manzhushaka.task.application;

/**
 * 任务进度上报回调。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@FunctionalInterface
public interface TaskProgressReporter
{
    /**
     * 上报进度。
     *
     * @param total 总数量
     * @param processed 已处理数量
     * @param success 成功数量
     * @param failure 失败数量
     */
    void report(long total, long processed, long success, long failure);
}
