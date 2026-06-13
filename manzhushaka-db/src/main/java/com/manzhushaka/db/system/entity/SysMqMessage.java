package com.manzhushaka.db.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.manzhushaka.db.meta.BaseEntity;

import java.time.LocalDateTime;

/**
 * 映射 SysMqMessage 数据库实体。
 */
@TableName("sys_mq_message")
public class SysMqMessage extends BaseEntity {
    private String eventId;
    private String streamKey;
    private String eventType;
    private String bizKey;
    private String traceId;
    private String source;
    private String status;
    private String payloadSnapshot;
    private Integer retryCount;
    private String lastError;
    private String consumerGroup;
    private String consumerName;
    private LocalDateTime processingDeadlineAt;
    private LocalDateTime publishedAt;
    private LocalDateTime consumeStartedAt;
    private LocalDateTime consumedAt;

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
     * 返回 streamKey。
     *
     * @return 字段值
     */
    public String getStreamKey() {
        return streamKey;
    }

    /**
     * 设置 streamKey。
     *
     * @param streamKey streamKey 参数
     */
    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
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
     * 返回 status。
     *
     * @return 字段值
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status status 参数
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 返回 payloadSnapshot。
     *
     * @return 字段值
     */
    public String getPayloadSnapshot() {
        return payloadSnapshot;
    }

    /**
     * 设置 payloadSnapshot。
     *
     * @param payloadSnapshot payloadSnapshot 参数
     */
    public void setPayloadSnapshot(String payloadSnapshot) {
        this.payloadSnapshot = payloadSnapshot;
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
     * 返回 lastError。
     *
     * @return 字段值
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * 设置 lastError。
     *
     * @param lastError lastError 参数
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * 返回 consumerGroup。
     *
     * @return 字段值
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * 设置 consumerGroup。
     *
     * @param consumerGroup consumerGroup 参数
     */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /**
     * 返回 consumerName。
     *
     * @return 字段值
     */
    public String getConsumerName() {
        return consumerName;
    }

    /**
     * 设置 consumerName。
     *
     * @param consumerName consumerName 参数
     */
    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    /**
     * 返回 processingDeadlineAt。
     *
     * @return 字段值
     */
    public LocalDateTime getProcessingDeadlineAt() {
        return processingDeadlineAt;
    }

    /**
     * 设置 processingDeadlineAt。
     *
     * @param processingDeadlineAt processingDeadlineAt 参数
     */
    public void setProcessingDeadlineAt(LocalDateTime processingDeadlineAt) {
        this.processingDeadlineAt = processingDeadlineAt;
    }

    /**
     * 返回 publishedAt。
     *
     * @return 字段值
     */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    /**
     * 设置 publishedAt。
     *
     * @param publishedAt publishedAt 参数
     */
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    /**
     * 返回 consumeStartedAt。
     *
     * @return 字段值
     */
    public LocalDateTime getConsumeStartedAt() {
        return consumeStartedAt;
    }

    /**
     * 设置 consumeStartedAt。
     *
     * @param consumeStartedAt consumeStartedAt 参数
     */
    public void setConsumeStartedAt(LocalDateTime consumeStartedAt) {
        this.consumeStartedAt = consumeStartedAt;
    }

    /**
     * 返回 consumedAt。
     *
     * @return 字段值
     */
    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    /**
     * 设置 consumedAt。
     *
     * @param consumedAt consumedAt 参数
     */
    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }
}
