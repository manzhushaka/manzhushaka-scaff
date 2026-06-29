package com.manzhushaka.framework.mq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Stream 记录。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RedisStreamRecord
{
    private final String streamKey;

    private final String messageId;

    private final Map<String, String> body;

    public RedisStreamRecord(String streamKey, String messageId, Map<String, String> body)
    {
        this.streamKey = streamKey;
        this.messageId = messageId;
        this.body = Collections.unmodifiableMap(new HashMap<>(body));
    }

    public String getStreamKey()
    {
        return streamKey;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public Map<String, String> getBody()
    {
        return body;
    }

    public String getBodyValue(String key)
    {
        return body.get(key);
    }
}