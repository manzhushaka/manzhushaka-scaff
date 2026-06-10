package com.manzhushaka.system.vo.log;

import java.time.LocalDateTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getProcessingDeadlineAt() {
        return processingDeadlineAt;
    }

    public void setProcessingDeadlineAt(LocalDateTime processingDeadlineAt) {
        this.processingDeadlineAt = processingDeadlineAt;
    }

    public Boolean getProcessingTimedOut() {
        return processingTimedOut;
    }

    public void setProcessingTimedOut(Boolean processingTimedOut) {
        this.processingTimedOut = processingTimedOut;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getConsumeStartedAt() {
        return consumeStartedAt;
    }

    public void setConsumeStartedAt(LocalDateTime consumeStartedAt) {
        this.consumeStartedAt = consumeStartedAt;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getPayloadSnapshot() {
        return payloadSnapshot;
    }

    public void setPayloadSnapshot(String payloadSnapshot) {
        this.payloadSnapshot = payloadSnapshot;
    }
}
