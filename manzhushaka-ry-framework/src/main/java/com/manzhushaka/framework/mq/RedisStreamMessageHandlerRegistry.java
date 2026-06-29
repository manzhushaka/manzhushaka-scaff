package com.manzhushaka.framework.mq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Stream 消息处理器注册表。
 * <p>
 * 收集全部 handler，按 streamKey 和 messageType 建立索引，拒绝重复注册。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RedisStreamMessageHandlerRegistry
{
    private final Map<String, RedisStreamMessageHandler> handlerByStream = new LinkedHashMap<>();

    private final Map<String, RedisStreamMessageHandler> handlerByType = new LinkedHashMap<>();

    /**
     * 构造注册表，自动按 streamKey 和 messageType 索引。
     *
     * @param handlers handler 列表
     */
    public RedisStreamMessageHandlerRegistry(List<RedisStreamMessageHandler> handlers)
    {
        for (RedisStreamMessageHandler handler : handlers)
        {
            String streamKey = handler.streamKey();
            if (handlerByStream.containsKey(streamKey))
            {
                throw new IllegalStateException(
                        "Duplicate streamKey registration: " + streamKey + " for type " + handler.messageType());
            }
            String messageType = handler.messageType();
            if (handlerByType.containsKey(messageType))
            {
                throw new IllegalStateException(
                        "Duplicate messageType registration: " + messageType);
            }
            handlerByStream.put(streamKey, handler);
            handlerByType.put(messageType, handler);
        }
    }

    /**
     * 获取所有注册的 handler。
     *
     * @return handler 列表
     */
    public List<RedisStreamMessageHandler> listHandlers()
    {
        return Collections.unmodifiableList(new ArrayList<>(handlerByStream.values()));
    }

    /**
     * 根据 streamKey 查找 handler。
     *
     * @param streamKey Stream 名称
     * @return handler，不存在返回 null
     */
    public RedisStreamMessageHandler getByStreamKey(String streamKey)
    {
        return handlerByStream.get(streamKey);
    }

    /**
     * 根据 messageType 查找 handler。
     *
     * @param messageType 消息类型
     * @return handler，不存在返回 null
     */
    public RedisStreamMessageHandler getByMessageType(String messageType)
    {
        return handlerByType.get(messageType);
    }
}