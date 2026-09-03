package com.manzhushaka.task.application;

import java.util.List;

/**
 * 分批导入任务模板。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public abstract class AbstractImportTaskHandler<T> implements TaskHandler
{
    private static final int BATCH_SIZE = 500;

    /** 解析输入文件并按固定批次处理。 */
    @Override
    public final void execute(TaskContext<?> context) throws Exception
    {
        List<T> rows = readRows(context);
        long total = rows.size();
        long processed = 0L;
        long success = 0L;
        long failure = 0L;
        context.report(total, processed, success, failure);
        for (int offset = 0; offset < rows.size(); offset += BATCH_SIZE)
        {
            checkCancelled(context);
            List<T> batch = rows.subList(offset, Math.min(offset + BATCH_SIZE, rows.size()));
            ImportBatchResult result = processBatch(context, batch);
            processed += batch.size();
            success += result.successCount();
            failure += result.failureCount();
            context.report(total, processed, success, failure);
        }
    }

    /** 解析任务文件。 */
    protected abstract List<T> readRows(TaskContext<?> context) throws Exception;

    /** 处理一个导入批次。 */
    protected abstract ImportBatchResult processBatch(TaskContext<?> context, List<T> batch);

    /** 检查取消状态。 */
    protected final void checkCancelled(TaskContext<?> context)
    {
        if (context.isCancellationRequested())
        {
            throw new TaskCancelledException();
        }
    }

    /** 导入批次计数。 */
    protected record ImportBatchResult(long successCount, long failureCount)
    {
    }
}
