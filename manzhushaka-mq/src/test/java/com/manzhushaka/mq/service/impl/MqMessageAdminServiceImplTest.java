package com.manzhushaka.mq.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.mq.core.MqEvent;
import com.manzhushaka.mq.core.RedisStreamPublisher;
import com.manzhushaka.mq.properties.MqProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqMessageAdminServiceImplTest {

    @Test
    void shouldAllowRetryForTimedOutPublishedMessage() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        RedisStreamPublisher redisStreamPublisher = mock(RedisStreamPublisher.class);
        MqMessageAdminServiceImpl service = buildService(mqMessageMapper, redisStreamPublisher);
        SysMqMessage message = buildMessage(MqMessageStatus.PUBLISHED.name());
        message.setPublishedAt(LocalDateTime.now().minusSeconds(301));
        when(mqMessageMapper.selectById(100L)).thenReturn(message);
        when(mqMessageMapper.update(any(), any())).thenReturn(1);
        doNothing().when(redisStreamPublisher).publish(any(), any());
        doAnswer(invocation -> {
            SysMqMessage updated = invocation.getArgument(0);
            message.setStatus(updated.getStatus());
            message.setRetryCount(updated.getRetryCount());
            message.setPublishedAt(updated.getPublishedAt());
            message.setProcessingDeadlineAt(updated.getProcessingDeadlineAt());
            message.setLastError(updated.getLastError());
            message.setConsumerGroup(updated.getConsumerGroup());
            message.setConsumerName(updated.getConsumerName());
            message.setConsumeStartedAt(updated.getConsumeStartedAt());
            message.setConsumedAt(updated.getConsumedAt());
            message.setUpdateTime(updated.getUpdateTime());
            return 1;
        }).when(mqMessageMapper).updateById(any(SysMqMessage.class));

        assertDoesNotThrow(() -> service.retry(100L));

        ArgumentCaptor<MqEvent> eventCaptor = ArgumentCaptor.forClass(MqEvent.class);
        verify(redisStreamPublisher).publish(eq("stream:op-log"), eventCaptor.capture());
        verify(mqMessageMapper).updateById(message);
        assertEquals("evt-100", eventCaptor.getValue().getEventId());
        assertEquals(2, eventCaptor.getValue().getRetryCount());
        assertEquals(MqMessageStatus.PUBLISHED.name(), message.getStatus());
        assertEquals(2, message.getRetryCount());
    }

    @Test
    void shouldRejectRetryForPublishedMessageThatHasNotTimedOut() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        MqMessageAdminServiceImpl service = buildService(mqMessageMapper, mock(RedisStreamPublisher.class));
        SysMqMessage message = buildMessage(MqMessageStatus.PUBLISHED.name());
        message.setPublishedAt(LocalDateTime.now().minusSeconds(299));
        when(mqMessageMapper.selectById(100L)).thenReturn(message);

        BizException exception = assertThrows(BizException.class, () -> service.retry(100L));

        assertEquals(400, exception.getCode());
    }

    @Test
    void shouldRejectRetryWhenConcurrentClaimFails() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        RedisStreamPublisher redisStreamPublisher = mock(RedisStreamPublisher.class);
        MqMessageAdminServiceImpl service = buildService(mqMessageMapper, redisStreamPublisher);
        SysMqMessage message = buildMessage(MqMessageStatus.FAIL.name());
        when(mqMessageMapper.selectById(100L)).thenReturn(message);
        when(mqMessageMapper.update(any(), any())).thenReturn(0);

        BizException exception = assertThrows(BizException.class, () -> service.retry(100L));

        assertEquals(409, exception.getCode());
        verify(redisStreamPublisher, never()).publish(any(), any());
        verify(mqMessageMapper, never()).updateById(any(SysMqMessage.class));
    }

    @Test
    void shouldPersistFailureWhenPublishThrows() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        RedisStreamPublisher redisStreamPublisher = mock(RedisStreamPublisher.class);
        MqMessageAdminServiceImpl service = buildService(mqMessageMapper, redisStreamPublisher);
        SysMqMessage message = buildMessage(MqMessageStatus.FAIL.name());
        when(mqMessageMapper.selectById(100L)).thenReturn(message);
        when(mqMessageMapper.update(any(), any())).thenReturn(1);
        doThrow(new IllegalStateException("redis down")).when(redisStreamPublisher).publish(any(), any());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.retry(100L));

        assertEquals("redis down", exception.getMessage());
        ArgumentCaptor<SysMqMessage> updateCaptor = ArgumentCaptor.forClass(SysMqMessage.class);
        verify(mqMessageMapper).updateById(updateCaptor.capture());
        SysMqMessage failedMessage = updateCaptor.getValue();
        assertEquals(MqMessageStatus.FAIL.name(), failedMessage.getStatus());
        assertEquals(1, failedMessage.getRetryCount());
        assertEquals("redis down", failedMessage.getLastError());
        assertNull(failedMessage.getProcessingDeadlineAt());
    }

    private MqMessageAdminServiceImpl buildService(SysMqMessageMapper mqMessageMapper, RedisStreamPublisher redisStreamPublisher) {
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        return new MqMessageAdminServiceImpl(
            mqMessageMapper,
            redisStreamPublisher,
            new ObjectMapper().findAndRegisterModules(),
            mqProperties
        );
    }

    private SysMqMessage buildMessage(String status) {
        SysMqMessage message = new SysMqMessage();
        message.setId(100L);
        message.setEventId("evt-100");
        message.setStreamKey("stream:op-log");
        message.setEventType("OP_LOG_CREATED");
        message.setBizKey("biz-100");
        message.setTraceId("trace-100");
        message.setSource("system");
        message.setStatus(status);
        message.setRetryCount(1);
        message.setPayloadSnapshot("{\"bizId\":100}");
        return message;
    }
}
