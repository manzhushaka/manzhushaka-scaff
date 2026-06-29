package com.manzhushaka.framework.mq;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Redis Stream 消息发布器。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Component
public class RedisStreamMessagePublisher
{
    private final RedisStreamGateway gateway;

    public RedisStreamMessagePublisher(RedisStreamGateway gateway)
    {
        this.gateway = gateway;
    }

    /**
     * 向指定 Stream 发布消息。
     *
     * @param streamKey   Stream 名称
     * @param messageType 消息类型
     * @param businessKey 业务幂等键
     * @param payload     消息载荷
     * @return 消息 ID
     */
    public String publish(String streamKey, String messageType, String businessKey, String payload)
    {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("messageType", messageType);
        body.put("businessKey", businessKey);
        body.put("payload", payload);
        body.put("retryTimes", "0");
        return gateway.add(streamKey, body);
    }
}