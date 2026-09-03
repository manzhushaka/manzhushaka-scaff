package com.manzhushaka.task.application;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;

import com.manzhushaka.task.infrastructure.persistence.entity.AbstractTaskEntity;

/**
 * 异步任务执行上下文，不持有 HTTP 请求对象。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public final class TaskContext<T extends AbstractTaskEntity>
{
    private final T task;
    private final Path filePath;
    private final TaskProgressReporter progressReporter;
    private final BooleanSupplier cancellationRequested;

    public TaskContext(T task, Path filePath, TaskProgressReporter progressReporter,
            BooleanSupplier cancellationRequested)
    {
        this.task = task;
        this.filePath = filePath;
        this.progressReporter = progressReporter;
        this.cancellationRequested = cancellationRequested;
    }

    /** 获取任务记录。 */
    public T task() { return task; }

    /** 获取任务文件路径。 */
    public Path filePath() { return filePath; }

    /** 上报任务进度。 */
    public void report(long total, long processed, long success, long failure)
    {
        progressReporter.report(total, processed, success, failure);
    }

    /** 判断任务是否已请求取消。 */
    public boolean isCancellationRequested()
    {
        return cancellationRequested.getAsBoolean();
    }
}
