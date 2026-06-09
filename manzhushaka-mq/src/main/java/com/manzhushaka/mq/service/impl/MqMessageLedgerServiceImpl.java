package com.manzhushaka.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.mq.core.MqEvent;
import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MqMessageLedgerServiceImpl implements MqMessageLedgerService {

    private final SysMqMessageMapper mqMessageMapper;
    private final ObjectMapper objectMapper;

    public MqMessageLedgerServiceImpl(SysMqMessageMapper mqMessageMapper, ObjectMapper mqObjectMapper) {
        this.mqMessageMapper = mqMessageMapper;
        this.objectMapper = mqObjectMapper;
    }

    @Override
    @Transactional
    public void createInitRecord(String streamKey, MqEvent<?> event) {
        SysMqMessage entity = new SysMqMessage();
        LocalDateTime now = LocalDateTime.now();
        entity.setEventId(event.getEventId());
        entity.setStreamKey(streamKey);
        entity.setEventType(event.getEventType());
        entity.setBizKey(event.getBizKey());
        entity.setTraceId(event.getTraceId());
        entity.setSource(event.getSource());
        entity.setStatus(MqMessageStatus.INIT.name());
        entity.setPayloadSnapshot(writePayloadSnapshot(event));
        entity.setRetryCount(event.getRetryCount() == null ? 0 : event.getRetryCount());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        mqMessageMapper.insert(entity);
    }

    @Override
    public boolean isSuccess(String eventId) {
        SysMqMessage entity = getByEventId(eventId);
        return entity != null && MqMessageStatus.SUCCESS.name().equals(entity.getStatus());
    }

    @Override
    @Transactional
    public void markPublished(String eventId) {
        updateStatus(eventId, MqMessageStatus.PUBLISHED, entity -> {
            entity.setPublishedAt(LocalDateTime.now());
            entity.setLastError(null);
        });
    }

    @Override
    @Transactional
    public void markProcessing(String eventId, String consumerGroup, String consumerName, int processingTimeoutSeconds) {
        updateStatus(eventId, MqMessageStatus.PROCESSING, entity -> {
            LocalDateTime now = LocalDateTime.now();
            entity.setConsumerGroup(consumerGroup);
            entity.setConsumerName(consumerName);
            entity.setConsumeStartedAt(now);
            entity.setProcessingDeadlineAt(now.plusSeconds(processingTimeoutSeconds));
            entity.setLastError(null);
        });
    }

    @Override
    @Transactional
    public void markSuccess(String eventId) {
        updateStatus(eventId, MqMessageStatus.SUCCESS, entity -> {
            entity.setConsumedAt(LocalDateTime.now());
            entity.setLastError(null);
        });
    }

    @Override
    @Transactional
    public void markFailed(String eventId, String errorMessage) {
        updateStatus(eventId, MqMessageStatus.FAIL, entity -> entity.setLastError(truncateError(errorMessage)));
    }

    private void updateStatus(String eventId, MqMessageStatus status, java.util.function.Consumer<SysMqMessage> customizer) {
        SysMqMessage entity = getByEventId(eventId);
        if (entity == null) {
            return;
        }
        entity.setStatus(status.name());
        entity.setUpdateTime(LocalDateTime.now());
        customizer.accept(entity);
        mqMessageMapper.updateById(entity);
    }

    private SysMqMessage getByEventId(String eventId) {
        return mqMessageMapper.selectOne(new LambdaQueryWrapper<SysMqMessage>()
            .eq(SysMqMessage::getEventId, eventId)
            .last("limit 1"));
    }

    private String writePayloadSnapshot(MqEvent<?> event) {
        try {
            return objectMapper.writeValueAsString(event.getPayload());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("序列化 MQ 消息台账快照失败", exception);
        }
    }

    private String truncateError(String errorMessage) {
        String message = (errorMessage == null || errorMessage.isBlank()) ? "unknown error" : errorMessage;
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
