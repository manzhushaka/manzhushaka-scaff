package com.manzhushaka.mq.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqMessageLedgerServiceImplTest {

    @Test
    void markSuccessShouldClearProcessingDeadlineAt() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        MqMessageLedgerServiceImpl service = new MqMessageLedgerServiceImpl(mqMessageMapper, new ObjectMapper());
        SysMqMessage message = buildProcessingMessage();
        when(mqMessageMapper.selectOne(any())).thenReturn(message);

        service.markSuccess("event-001");

        ArgumentCaptor<SysMqMessage> captor = ArgumentCaptor.forClass(SysMqMessage.class);
        verify(mqMessageMapper).updateById(captor.capture());
        assertEquals(MqMessageStatus.SUCCESS.name(), captor.getValue().getStatus());
        assertNull(captor.getValue().getProcessingDeadlineAt());
    }

    @Test
    void markFailedShouldClearProcessingDeadlineAtAndTruncateLastErrorToOneThousand() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        MqMessageLedgerServiceImpl service = new MqMessageLedgerServiceImpl(mqMessageMapper, new ObjectMapper());
        SysMqMessage message = buildProcessingMessage();
        when(mqMessageMapper.selectOne(any())).thenReturn(message);

        service.markFailed("event-001", "x".repeat(1200));

        ArgumentCaptor<SysMqMessage> captor = ArgumentCaptor.forClass(SysMqMessage.class);
        verify(mqMessageMapper).updateById(captor.capture());
        assertEquals(MqMessageStatus.FAIL.name(), captor.getValue().getStatus());
        assertNull(captor.getValue().getProcessingDeadlineAt());
        assertEquals(1000, captor.getValue().getLastError().length());
    }

    @Test
    void markFailedShouldThrowWhenEventIdDoesNotExist() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        MqMessageLedgerServiceImpl service = new MqMessageLedgerServiceImpl(mqMessageMapper, new ObjectMapper());
        when(mqMessageMapper.selectOne(any())).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.markFailed("missing-event", "oops"));

        assertEquals("MQ 消息台账不存在: missing-event", exception.getMessage());
    }

    @Test
    void recycleTimedOutProcessingMessagesShouldResetStatusToPublished() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        MqMessageLedgerServiceImpl service = new MqMessageLedgerServiceImpl(mqMessageMapper, new ObjectMapper());
        SysMqMessage message = buildProcessingMessage();
        message.setStatus(MqMessageStatus.PROCESSING.name());
        message.setPublishedAt(LocalDateTime.of(2026, 6, 12, 9, 0));
        message.setConsumeStartedAt(LocalDateTime.of(2026, 6, 12, 9, 5));
        message.setConsumerGroup("oplog-group");
        message.setConsumerName("consumer-a");
        message.setLastError("handler failed");
        when(mqMessageMapper.selectList(any())).thenReturn(java.util.List.of(message));

        int recycledCount = service.recycleTimedOutProcessingMessages(LocalDateTime.of(2026, 6, 12, 10, 0));

        ArgumentCaptor<SysMqMessage> captor = ArgumentCaptor.forClass(SysMqMessage.class);
        verify(mqMessageMapper).updateById(captor.capture());
        assertEquals(1, recycledCount);
        assertEquals(MqMessageStatus.PUBLISHED.name(), captor.getValue().getStatus());
        assertNull(captor.getValue().getProcessingDeadlineAt());
        assertNull(captor.getValue().getConsumeStartedAt());
        assertNull(captor.getValue().getConsumerGroup());
        assertNull(captor.getValue().getConsumerName());
        assertTrue(captor.getValue().getLastError().contains("超时"), captor.getValue().getLastError());
    }

    private SysMqMessage buildProcessingMessage() {
        SysMqMessage message = new SysMqMessage();
        message.setId(1L);
        message.setEventId("event-001");
        message.setStatus(MqMessageStatus.PROCESSING.name());
        message.setProcessingDeadlineAt(LocalDateTime.of(2026, 6, 9, 12, 0));
        return message;
    }
}
