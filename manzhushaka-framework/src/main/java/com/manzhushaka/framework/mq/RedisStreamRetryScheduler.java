package com.manzhushaka.framework.mq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Redis Stream 重试调度器。
 * <p>
 * 定时扫描所有 handler 的 retry stream，到达 nextRetryTime 后重新投递到原始 stream。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RedisStreamRetryScheduler
{
    private static final Logger log = LoggerFactory.getLogger(RedisStreamRetryScheduler.class);

    private final RedisStreamGateway gateway;

    private final RedisStreamMessageHandlerRegistry registry;

    public RedisStreamRetryScheduler(RedisStreamGateway gateway, RedisStreamMessageHandlerRegistry registry)
    {
        this.gateway = gateway;
        this.registry = registry;
    }

    /**
     * 每隔 5 秒扫描一次所有 retry stream。
     */
    @Scheduled(fixedDelay = 5000L)
    public void scanRetryStreams()
    {
        for (RedisStreamMessageHandler handler : registry.listHandlers())
        {
            String retryStreamKey = handler.retryStreamKey();
            List<RedisStreamRecord> records = gateway.range(retryStreamKey, 100);
            if (records.isEmpty())
            {
                continue;
            }

            for (RedisStreamRecord record : records)
            {
                String nextRetryTimeStr = record.getBodyValue("nextRetryTime");
                if (nextRetryTimeStr == null)
                {
                    continue;
                }
                long nextRetryTime;
                try
                {
                    nextRetryTime = Long.parseLong(nextRetryTimeStr);
                }
                catch (NumberFormatException e)
                {
                    continue;
                }
                // 未到重试时间则跳过
                if (nextRetryTime > System.currentTimeMillis())
                {
                    continue;
                }

                try
                {
                    // 构造投递到原始 stream 的 body（复制为可变 Map）
                    Map<String, String> body = new HashMap<>(record.getBody());
                    body.remove("nextRetryTime");

                    gateway.add(handler.streamKey(), body);
                    gateway.delete(retryStreamKey, record.getMessageId());

                    log.info("Requeued retry message: stream={}, messageId={}, retryStream={}",
                            handler.streamKey(), record.getMessageId(), retryStreamKey);
                }
                catch (Exception e)
                {
                    log.error("Failed to requeue retry message: stream={}, messageId={}",
                            handler.streamKey(), record.getMessageId(), e);
                }
            }
        }
    }
}