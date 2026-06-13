package com.manzhushaka.system.vo.log;

import java.time.LocalDateTime;

/**
 * 承载 MqMessageVO 响应数据。
 */
public class MqMessageVO {
    private Long id;
    private String eventId;
    private String streamKey;
    private String eventType;
    private String bizKey;
    private String traceId;
    private String source;
    private String status;
    private Integer retryCount;
    private String lastError;
    private LocalDateTime processingDeadlineAt;
    private Boolean processingTimedOut;
    private LocalDateTime publishedAt;
    private LocalDateTime consumeStartedAt;
    private LocalDateTime consumedAt;
    private LocalDateTime createTime;
    private String payloadSnapshot;

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

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
     * 返回 processingTimedOut。
     *
     * @return 字段值
     */
    public Boolean getProcessingTimedOut() {
        return processingTimedOut;
    }

    /**
     * 设置 processingTimedOut。
     *
     * @param processingTimedOut processingTimedOut 参数
     */
    public void setProcessingTimedOut(Boolean processingTimedOut) {
        this.processingTimedOut = processingTimedOut;
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

    /**
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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
}
