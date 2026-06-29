package com.manzhushaka.framework.mq;

/**
 * Redis Stream 消息处理器。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface RedisStreamMessageHandler
{
    /**
     * 消息类型标识。
     *
     * @return 消息类型
     */
    String messageType();

    /**
     * 原始 Stream 名称。
     *
     * @return Stream 名称
     */
    String streamKey();

    /**
     * 消费者组名称。
     *
     * @return 消费者组
     */
    String consumerGroup();

    /**
     * 消费者名称。
     *
     * @return 消费者名称
     */
    String consumerName();

    /**
     * 重试 Stream 名称，默认基于 messageType。
     *
     * @return 重试 Stream
     */
    default String retryStreamKey()
    {
        return "mq:retry:" + messageType();
    }

    /**
     * 死信 Stream 名称，默认基于 messageType。
     *
     * @return 死信 Stream
     */
    default String deadLetterStreamKey()
    {
        return "mq:dead:" + messageType();
    }

    /**
     * 最大重试次数。
     *
     * @return 最大次数
     */
    default int maxRetryTimes()
    {
        return 3;
    }

    /**
     * 消费内立即重试次数。
     *
     * @return 立即重试次数
     */
    default int immediateRetryTimes()
    {
        return 0;
    }

    /**
     * 重试间隔（秒）。
     *
     * @return 间隔秒数
     */
    default long retryIntervalSeconds()
    {
        return 60L;
    }

    /**
     * 处理消息。
     *
     * @param record Redis Stream 记录
     */
    void handle(RedisStreamRecord record);
}