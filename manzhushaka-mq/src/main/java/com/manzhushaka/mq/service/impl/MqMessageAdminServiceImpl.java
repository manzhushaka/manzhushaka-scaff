package com.manzhushaka.mq.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.mq.core.MqEvent;
import com.manzhushaka.mq.core.RedisStreamPublisher;
import com.manzhushaka.mq.properties.MqProperties;
import com.manzhushaka.mq.service.MqMessageAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MqMessageAdminServiceImpl implements MqMessageAdminService {

    private final SysMqMessageMapper mqMessageMapper;
    private final RedisStreamPublisher redisStreamPublisher;
    private final ObjectMapper objectMapper;
    private final MqProperties mqProperties;

    public MqMessageAdminServiceImpl(
        SysMqMessageMapper mqMessageMapper,
        RedisStreamPublisher redisStreamPublisher,
        ObjectMapper mqObjectMapper,
        MqProperties mqProperties
    ) {
        this.mqMessageMapper = mqMessageMapper;
        this.redisStreamPublisher = redisStreamPublisher;
        this.objectMapper = mqObjectMapper;
        this.mqProperties = mqProperties;
    }

    @Override
    @Transactional
    public void retry(Long id) {
        SysMqMessage message = mqMessageMapper.selectById(id);
        if (message == null) {
            throw new BizException(404, "MQ 消息台账不存在");
        }
        if (!allowsRetry(message, LocalDateTime.now())) {
            throw new BizException(400, "当前消息状态不允许手动重试");
        }
        MqEvent<Object> event = rebuildEvent(message);
        redisStreamPublisher.publish(message.getStreamKey(), event);
        markRepublished(message, event.getRetryCount());
        mqMessageMapper.updateById(message);
    }

    private boolean allowsRetry(SysMqMessage message, LocalDateTime now) {
        MqMessageStatus status = resolveStatus(message.getStatus());
        return status.allowsManualRetry(isTimedOut(message, now));
    }

    private MqEvent<Object> rebuildEvent(SysMqMessage message) {
        if (message.getPayloadSnapshot() == null || message.getPayloadSnapshot().isBlank()) {
            throw new BizException(400, "消息快照不存在，无法重试");
        }
        try {
            MqEvent<Object> event = new MqEvent<>();
            event.setEventId(message.getEventId());
            event.setEventType(message.getEventType());
            event.setBizKey(message.getBizKey());
            event.setTraceId(message.getTraceId());
            event.setSource(message.getSource());
            event.setRetryCount((message.getRetryCount() == null ? 0 : message.getRetryCount()) + 1);
            event.setOccurredAt(LocalDateTime.now());
            event.setPayload(objectMapper.readValue(message.getPayloadSnapshot(), Object.class));
            return event;
        } catch (JsonProcessingException exception) {
            throw new BizException(500, "重建 MQ 消息失败");
        }
    }

    private void markRepublished(SysMqMessage message, int retryCount) {
        LocalDateTime now = LocalDateTime.now();
        message.setStatus(MqMessageStatus.PUBLISHED.name());
        message.setRetryCount(retryCount);
        message.setPublishedAt(now);
        message.setProcessingDeadlineAt(null);
        message.setLastError(null);
        message.setConsumerGroup(null);
        message.setConsumerName(null);
        message.setConsumeStartedAt(null);
        message.setConsumedAt(null);
        message.setUpdateTime(now);
    }

    private MqMessageStatus resolveStatus(String status) {
        try {
            return MqMessageStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BizException(400, "消息状态不合法");
        }
    }

    private boolean isProcessingTimedOut(SysMqMessage message, LocalDateTime now) {
        return message.getProcessingDeadlineAt() != null && !message.getProcessingDeadlineAt().isAfter(now);
    }

    private boolean isPublishedTimedOut(SysMqMessage message, LocalDateTime now) {
        if (message.getConsumeStartedAt() != null || message.getPublishedAt() == null) {
            return false;
        }
        return !message.getPublishedAt().plusSeconds(mqProperties.getProcessingTimeoutSeconds()).isAfter(now);
    }

    private boolean isTimedOut(SysMqMessage message, LocalDateTime now) {
        MqMessageStatus status = resolveStatus(message.getStatus());
        return switch (status) {
            case PROCESSING -> isProcessingTimedOut(message, now);
            case PUBLISHED -> isPublishedTimedOut(message, now);
            default -> false;
        };
    }
}
