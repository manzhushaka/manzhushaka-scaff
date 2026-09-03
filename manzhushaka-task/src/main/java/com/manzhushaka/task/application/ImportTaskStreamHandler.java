package com.manzhushaka.task.application;

import org.springframework.stereotype.Component;

import com.manzhushaka.framework.mq.AbstractRedisStreamMessageHandler;
import com.manzhushaka.framework.mq.RedisStreamGateway;
import com.manzhushaka.framework.mq.RedisStreamRecord;
import com.manzhushaka.system.service.ISysMqMessageLogService;

/**
 * 异步导入任务 Redis Stream 消费者。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Component
public class ImportTaskStreamHandler extends AbstractRedisStreamMessageHandler
{
    private final ImportTaskManager taskManager;

    public ImportTaskStreamHandler(RedisStreamGateway gateway, ISysMqMessageLogService logService,
            ImportTaskManager taskManager)
    {
        super(gateway, logService);
        this.taskManager = taskManager;
    }

    @Override public String messageType() { return ImportTaskManager.MESSAGE_TYPE; }
    @Override public String streamKey() { return ImportTaskManager.STREAM_KEY; }
    @Override public String consumerGroup() { return "system-import-task-workers"; }
    @Override public String consumerName() { return "system-import-task-worker"; }

    @Override
    protected void doHandle(RedisStreamRecord record)
    {
        taskManager.execute(Long.valueOf(record.getBodyValue("payload")));
    }
}
