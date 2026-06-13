package com.manzhushaka.mq.core;

/**
 * 定义 MqStreams。
 */
public final class MqStreams {
    public static final String OP_LOG = "manzhushaka:stream:oplog";
    public static final String NOTIFY = "manzhushaka:stream:notify";
    public static final String DEAD = "manzhushaka:stream:dead";

    /**
     * 创建 MqStreams 实例。
     */
    private MqStreams() {
    }
}
