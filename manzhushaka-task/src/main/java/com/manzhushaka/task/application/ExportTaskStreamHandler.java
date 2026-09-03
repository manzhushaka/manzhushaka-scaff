package com.manzhushaka.task.application;

import org.springframework.stereotype.Component;

import com.manzhushaka.framework.mq.AbstractRedisStreamMessageHandler;
import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamRecord;
import com.manzhushaka.system.service.ISysMqMessageLogService;

/**
 * 异步导出任务 Redis Stream 消费者。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Component
public class ExportTaskStreamHandler extends AbstractRedisStreamMessageHandler
{
    private final ExportTaskManager taskManager;

    public ExportTaskStreamHandler(RedisStreamGateway gateway, ISysMqMessageLogService logService,
            ExportTaskManager taskManager)
    {
        super(gateway, logService);
        this.taskManager = taskManager;
    }

    @Override public String messageType() { return ExportTaskManager.MESSAGE_TYPE; }
    @Override public String streamKey() { return ExportTaskManager.STREAM_KEY; }
    @Override public String consumerGroup() { return "system-export-task-workers"; }
    @Override public String consumerName() { return "system-export-task-worker"; }

    @Override
    protected void doHandle(RedisStreamRecord record)
    {
        taskManager.execute(Long.valueOf(record.getBodyValue("payload")));
    }
}
