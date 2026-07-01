package com.manzhushaka.framework.mq;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

/**
 * Redis Stream 消息监听注册器。
 * <p>
 * 应用启动时遍历所有 handler，创建消费者组并注册 StreamMessageListener。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RedisStreamMessageListenerRegistrar implements SmartLifecycle
{
    private static final Logger log = LoggerFactory.getLogger(RedisStreamMessageListenerRegistrar.class);

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    private final RedisStreamMessageHandlerRegistry registry;

    private final RedisStreamGateway gateway;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final Map<String, Subscription> subscriptions = new LinkedHashMap<>();

    public RedisStreamMessageListenerRegistrar(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
            RedisStreamMessageHandlerRegistry registry,
            RedisStreamGateway gateway)
    {
        this.container = container;
        this.registry = registry;
        this.gateway = gateway;
    }

    @Override
    public void start()
    {
        if (running.compareAndSet(false, true))
        {
            for (RedisStreamMessageHandler handler : registry.listHandlers())
            {
                gateway.createGroupIfAbsent(handler.streamKey(), handler.consumerGroup());

                Subscription subscription = container.receive(
                        Consumer.from(handler.consumerGroup(), handler.consumerName()),
                        StreamOffset.create(handler.streamKey(), ReadOffset.lastConsumed()),
                        record -> {
                            RedisStreamRecord streamRecord = new RedisStreamRecord(
                                    record.getStream(),
                                    record.getId().getValue(),
                                    record.getValue());
                            handler.handle(streamRecord);
                        });

                subscriptions.put(handler.streamKey(), subscription);
                log.info("Registered Redis Stream consumer: stream={}, group={}, consumer={}",
                        handler.streamKey(), handler.consumerGroup(), handler.consumerName());
            }
            container.start();
            log.info("Redis Stream listener container started with {} handlers", registry.listHandlers().size());
        }
    }

    @Override
    public void stop()
    {
        if (running.compareAndSet(true, false))
        {
            for (Map.Entry<String, Subscription> entry : subscriptions.entrySet())
            {
                entry.getValue().cancel();
                log.info("Cancelled subscription for stream: {}", entry.getKey());
            }
            subscriptions.clear();
            container.stop();
            log.info("Redis Stream listener container stopped");
        }
    }

    @Override
    public boolean isRunning()
    {
        return running.get();
    }
}