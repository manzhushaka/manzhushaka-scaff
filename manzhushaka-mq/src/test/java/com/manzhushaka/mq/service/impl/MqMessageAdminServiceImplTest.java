package com.manzhushaka.mq.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.mq.core.RedisStreamPublisher;
import com.manzhushaka.mq.properties.MqProperties;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqMessageAdminServiceImplTest {

    @Test
    void shouldAllowRetryForTimedOutPublishedMessage() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        RedisStreamPublisher redisStreamPublisher = mock(RedisStreamPublisher.class);
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        MqMessageAdminServiceImpl service = new MqMessageAdminServiceImpl(
            mqMessageMapper,
            redisStreamPublisher,
            new ObjectMapper().findAndRegisterModules(),
            mqProperties
        );
        SysMqMessage message = buildMessage(MqMessageStatus.PUBLISHED.name());
        message.setPublishedAt(LocalDateTime.now().minusSeconds(301));
        when(mqMessageMapper.selectById(100L)).thenReturn(message);
        doNothing().when(redisStreamPublisher).publish(any(), any());

        assertDoesNotThrow(() -> service.retry(100L));

        verify(redisStreamPublisher).publish(any(), any());
        verify(mqMessageMapper).updateById(message);
        assertEquals(MqMessageStatus.PUBLISHED.name(), message.getStatus());
        assertEquals(2, message.getRetryCount());
    }

    @Test
    void shouldRejectRetryForPublishedMessageThatHasNotTimedOut() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(300);
        MqMessageAdminServiceImpl service = new MqMessageAdminServiceImpl(
            mqMessageMapper,
            mock(RedisStreamPublisher.class),
            new ObjectMapper().findAndRegisterModules(),
            mqProperties
        );
        SysMqMessage message = buildMessage(MqMessageStatus.PUBLISHED.name());
        message.setPublishedAt(LocalDateTime.now().minusSeconds(299));
        when(mqMessageMapper.selectById(100L)).thenReturn(message);

        BizException exception = assertThrows(BizException.class, () -> service.retry(100L));

        assertEquals(400, exception.getCode());
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
