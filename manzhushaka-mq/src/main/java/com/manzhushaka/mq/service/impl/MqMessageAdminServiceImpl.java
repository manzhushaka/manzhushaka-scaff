package com.manzhushaka.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    public void retry(Long id) {
        SysMqMessage message = mqMessageMapper.selectById(id);
        if (message == null) {
            throw new BizException(404, "MQ 消息台账不存在");
        }
        if (!allowsRetry(message, LocalDateTime.now())) {
            throw new BizException(400, "当前消息状态不允许手动重试");
        }
        MqEvent<Object> event = rebuildEvent(message);
        claimRetry(message);
        try {
            redisStreamPublisher.publish(message.getStreamKey(), event);
        } catch (RuntimeException exception) {
            markRetryFailed(message, exception.getMessage());
            mqMessageMapper.updateById(message);
            throw exception;
        }
        markRepublished(message, event.getRetryCount());
        mqMessageMapper.updateById(message);
    }

    private void claimRetry(SysMqMessage message) {
        LocalDateTime now = LocalDateTime.now();
        SysMqMessage updating = new SysMqMessage();
        updating.setStatus(MqMessageStatus.PROCESSING.name());
        updating.setProcessingDeadlineAt(now.plusSeconds(mqProperties.getProcessingTimeoutSeconds()));
        updating.setLastError(null);
        updating.setConsumerGroup(null);
        updating.setConsumerName(null);
        updating.setConsumeStartedAt(now);
        updating.setConsumedAt(null);
        updating.setUpdateTime(now);
        int updated = mqMessageMapper.update(updating, new LambdaUpdateWrapper<SysMqMessage>()
            .eq(SysMqMessage::getId, message.getId())
            .eq(SysMqMessage::getStatus, message.getStatus()));
        if (updated != 1) {
            throw new BizException(409, "消息正在重试中，请刷新后重试");
        }
        message.setStatus(updating.getStatus());
        message.setProcessingDeadlineAt(updating.getProcessingDeadlineAt());
        message.setLastError(null);
        message.setConsumerGroup(null);
        message.setConsumerName(null);
        message.setConsumeStartedAt(updating.getConsumeStartedAt());
        message.setConsumedAt(null);
        message.setUpdateTime(now);
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

    private void markRetryFailed(SysMqMessage message, String errorMessage) {
        LocalDateTime now = LocalDateTime.now();
        message.setStatus(MqMessageStatus.FAIL.name());
        message.setProcessingDeadlineAt(null);
        message.setLastError(truncateError(errorMessage));
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

    private String truncateError(String errorMessage) {
        String message = (errorMessage == null || errorMessage.isBlank()) ? "unknown error" : errorMessage;
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
