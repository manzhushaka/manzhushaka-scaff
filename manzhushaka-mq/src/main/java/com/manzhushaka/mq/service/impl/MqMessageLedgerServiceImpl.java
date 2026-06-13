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

/**
 * 实现 MqMessageLedgerServiceImpl 业务服务。
 */
@Service
public class MqMessageLedgerServiceImpl implements MqMessageLedgerService {

    private final SysMqMessageMapper mqMessageMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建 MqMessageLedgerServiceImpl 实例。
     *
     * @param mqMessageMapper mqMessageMapper 参数
     * @param mqObjectMapper mqObjectMapper 参数
     */
    public MqMessageLedgerServiceImpl(SysMqMessageMapper mqMessageMapper, ObjectMapper mqObjectMapper) {
        this.mqMessageMapper = mqMessageMapper;
        this.objectMapper = mqObjectMapper;
    }

    /**
     * 创建 create Init Record 数据。
     *
     * @param streamKey streamKey 参数
     * @param event event 参数
     */
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

    /**
     * 判断是否 success。
     *
     * @param eventId eventId 标识
     * @return 字段值
     */
    @Override
    public boolean isSuccess(String eventId) {
        SysMqMessage entity = getByEventId(eventId);
        return entity != null && MqMessageStatus.SUCCESS.name().equals(entity.getStatus());
    }

    /**
     * 更新 mark Published 数据。
     *
     * @param eventId eventId 标识
     */
    @Override
    @Transactional
    public void markPublished(String eventId) {
        updateStatus(eventId, MqMessageStatus.PUBLISHED, entity -> {
            entity.setPublishedAt(LocalDateTime.now());
            entity.setLastError(null);
        });
    }

    /**
     * 更新 mark Processing 数据。
     *
     * @param eventId eventId 标识
     * @param consumerGroup consumerGroup 参数
     * @param consumerName consumerName 参数
     * @param processingTimeoutSeconds processingTimeoutSeconds 参数
     */
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

    /**
     * 更新 mark Success 数据。
     *
     * @param eventId eventId 标识
     */
    @Override
    @Transactional
    public void markSuccess(String eventId) {
        updateStatus(eventId, MqMessageStatus.SUCCESS, entity -> {
            entity.setConsumedAt(LocalDateTime.now());
            entity.setProcessingDeadlineAt(null);
            entity.setLastError(null);
        });
    }

    /**
     * 更新 mark Failed 数据。
     *
     * @param eventId eventId 标识
     * @param errorMessage errorMessage 参数
     */
    @Override
    @Transactional
    public void markFailed(String eventId, String errorMessage) {
        updateStatus(eventId, MqMessageStatus.FAIL, entity -> {
            entity.setProcessingDeadlineAt(null);
            entity.setLastError(truncateError(errorMessage));
        });
    }

    /**
     * 清理 recycle Timed Out Processing Messages 数据。
     *
     * @param now now 参数
     * @return 处理结果
     */
    @Override
    @Transactional
    public int recycleTimedOutProcessingMessages(LocalDateTime now) {
        LocalDateTime referenceTime = now == null ? LocalDateTime.now() : now;
        java.util.List<SysMqMessage> timedOutMessages = mqMessageMapper.selectList(new LambdaQueryWrapper<SysMqMessage>()
            .eq(SysMqMessage::getStatus, MqMessageStatus.PROCESSING.name())
            .isNotNull(SysMqMessage::getProcessingDeadlineAt)
            .le(SysMqMessage::getProcessingDeadlineAt, referenceTime));
        for (SysMqMessage message : timedOutMessages) {
            message.setStatus(MqMessageStatus.PUBLISHED.name());
            message.setProcessingDeadlineAt(null);
            message.setConsumeStartedAt(null);
            message.setConsumerGroup(null);
            message.setConsumerName(null);
            message.setConsumedAt(null);
            message.setLastError(truncateError("消息处理超时，已回退为待消费状态"));
            message.setUpdateTime(referenceTime);
            mqMessageMapper.updateById(message);
        }
        return timedOutMessages.size();
    }

    /**
     * 更新 update Status 数据。
     *
     * @param eventId eventId 标识
     * @param status status 参数
     * @param customizer customizer 参数
     */
    private void updateStatus(String eventId, MqMessageStatus status, java.util.function.Consumer<SysMqMessage> customizer) {
        SysMqMessage entity = getByEventId(eventId);
        if (entity == null) {
            throw new IllegalStateException("MQ 消息台账不存在: " + eventId);
        }
        entity.setStatus(status.name());
        entity.setUpdateTime(LocalDateTime.now());
        customizer.accept(entity);
        mqMessageMapper.updateById(entity);
    }

    /**
     * 返回 byEventId。
     *
     * @param eventId eventId 标识
     * @return 字段值
     */
    private SysMqMessage getByEventId(String eventId) {
        return mqMessageMapper.selectOne(new LambdaQueryWrapper<SysMqMessage>()
            .eq(SysMqMessage::getEventId, eventId)
            .last("limit 1"));
    }

    /**
     * 更新 write Payload Snapshot 数据。
     *
     * @param event event 参数
     * @return 处理结果
     */
    private String writePayloadSnapshot(MqEvent<?> event) {
        try {
            return objectMapper.writeValueAsString(event.getPayload());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("序列化 MQ 消息台账快照失败", exception);
        }
    }

    /**
     * 截断错误信息。
     *
     * @param errorMessage errorMessage 参数
     * @return 处理结果
     */
    private String truncateError(String errorMessage) {
        String message = (errorMessage == null || errorMessage.isBlank()) ? "unknown error" : errorMessage;
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
