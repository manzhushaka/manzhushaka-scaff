package com.manzhushaka.mq.core;

import java.time.LocalDateTime;

/**
 * 定义 MqEvent。
 */
public class MqEvent<T> {
    private String eventId;
    private String eventType;
    private String bizKey;
    private LocalDateTime occurredAt;
    private String traceId;
    private String source;
    private Integer retryCount;
    private T payload;

    /**
     * 返回 eventId。
     *
     * @return 字段值
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 设置 eventId。
     *
     * @param eventId eventId 标识
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 返回 eventType。
     *
     * @return 字段值
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * 设置 eventType。
     *
     * @param eventType eventType 参数
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * 返回 bizKey。
     *
     * @return 字段值
     */
    public String getBizKey() {
        return bizKey;
    }

    /**
     * 设置 bizKey。
     *
     * @param bizKey bizKey 参数
     */
    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    /**
     * 返回 occurredAt。
     *
     * @return 字段值
     */
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * 设置 occurredAt。
     *
     * @param occurredAt occurredAt 参数
     */
    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    /**
     * 返回 traceId。
     *
     * @return 字段值
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置 traceId。
     *
     * @param traceId traceId 标识
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 返回 source。
     *
     * @return 字段值
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置 source。
     *
     * @param source source 参数
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 返回 retryCount。
     *
     * @return 字段值
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 设置 retryCount。
     *
     * @param retryCount retryCount 参数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 返回 payload。
     *
     * @return 字段值
     */
    public T getPayload() {
        return payload;
    }

    /**
     * 设置 payload。
     *
     * @param payload 请求数据
     */
    public void setPayload(T payload) {
        this.payload = payload;
    }
}
