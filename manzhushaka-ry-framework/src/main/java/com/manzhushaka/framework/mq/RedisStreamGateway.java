package com.manzhushaka.framework.mq;

import java.util.List;
import java.util.Map;

/**
 * Redis Stream 底层操作接口。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface RedisStreamGateway
{
    /**
     * 向指定 Stream 添加消息。
     *
     * @param streamKey Stream 名称
     * @param body      消息内容（Map）
     * @return 消息 ID
     */
    String add(String streamKey, Map<String, String> body);

    /**
     * 对指定消息做 ACK。
     *
     * @param streamKey     Stream 名称
     * @param consumerGroup 消费者组名称
     * @param messageId     消息 ID
     */
    void acknowledge(String streamKey, String consumerGroup, String messageId);

    /**
     * 若消费者组不存在则创建。
     *
     * @param streamKey     Stream 名称
     * @param consumerGroup 消费者组名称
     */
    void createGroupIfAbsent(String streamKey, String consumerGroup);

    /**
     * 读取指定 Stream 的最近 N 条消息。
     *
     * @param streamKey Stream 名称
     * @param count     读取数量
     * @return 消息记录列表
     */
    List<RedisStreamRecord> range(String streamKey, int count);

    /**
     * 删除指定 Stream 中的某条消息。
     *
     * @param streamKey Stream 名称
     * @param messageId 消息 ID
     */
    void delete(String streamKey, String messageId);
}