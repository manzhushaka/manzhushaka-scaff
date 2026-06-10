package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysLoginLogMapper;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.db.system.mapper.SysOpLogMapper;
import com.manzhushaka.mq.properties.MqProperties;
import com.manzhushaka.system.dto.log.MqMessageQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.MqMessageVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogQueryServiceMqMessageTest {

    private static final int PROCESSING_TIMEOUT_SECONDS = 300;

    @Test
    void shouldMarkProcessingMessageAsTimedOutWhenDeadlineHasPassed() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        LogQueryServiceImpl service = new LogQueryServiceImpl(
            mock(SysLoginLogMapper.class),
            mock(SysOpLogMapper.class),
            mqMessageMapper,
            buildMqProperties()
        );
        SysMqMessage entity = buildMessage(MqMessageStatus.PROCESSING.name());
        entity.setProcessingDeadlineAt(LocalDateTime.now().minusSeconds(30));

        when(mqMessageMapper.selectPage(any(), any())).thenReturn(new Page<SysMqMessage>(1, 20, 1)
            .setRecords(List.of(entity)));

        PageResult<MqMessageVO> result = service.pageMqMessages(new MqMessageQuery());

        assertEquals(1, result.getTotal());
        assertTrue(result.getRecords().get(0).getProcessingTimedOut());
    }

    @Test
    void shouldMarkPublishedMessageAsTimedOutWhenPublishWindowHasExpiredBeforeConsumption() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        LogQueryServiceImpl service = new LogQueryServiceImpl(
            mock(SysLoginLogMapper.class),
            mock(SysOpLogMapper.class),
            mqMessageMapper,
            buildMqProperties()
        );
        SysMqMessage entity = buildMessage(MqMessageStatus.PUBLISHED.name());
        entity.setPublishedAt(LocalDateTime.now().minusSeconds(PROCESSING_TIMEOUT_SECONDS + 1L));
        entity.setProcessingDeadlineAt(null);

        when(mqMessageMapper.selectPage(any(), any())).thenReturn(new Page<SysMqMessage>(1, 20, 1)
            .setRecords(List.of(entity)));

        PageResult<MqMessageVO> result = service.pageMqMessages(new MqMessageQuery());

        assertTrue(result.getRecords().get(0).getProcessingTimedOut());
    }

    @Test
    void shouldNotMarkPublishedMessageAsTimedOutBeforePublishWindowExpires() {
        SysMqMessageMapper mqMessageMapper = mock(SysMqMessageMapper.class);
        LogQueryServiceImpl service = new LogQueryServiceImpl(
            mock(SysLoginLogMapper.class),
            mock(SysOpLogMapper.class),
            mqMessageMapper,
            buildMqProperties()
        );
        SysMqMessage entity = buildMessage(MqMessageStatus.PUBLISHED.name());
        entity.setPublishedAt(LocalDateTime.now().minusSeconds(PROCESSING_TIMEOUT_SECONDS - 1L));
        entity.setProcessingDeadlineAt(null);

        when(mqMessageMapper.selectPage(any(), any())).thenReturn(new Page<SysMqMessage>(1, 20, 1)
            .setRecords(List.of(entity)));

        PageResult<MqMessageVO> result = service.pageMqMessages(new MqMessageQuery());

        assertFalse(result.getRecords().get(0).getProcessingTimedOut());
    }

    private SysMqMessage buildMessage(String status) {
        SysMqMessage entity = new SysMqMessage();
        entity.setId(100L);
        entity.setEventId("evt-100");
        entity.setStreamKey("stream:op-log");
        entity.setEventType("OP_LOG_CREATED");
        entity.setBizKey("biz-100");
        entity.setTraceId("trace-100");
        entity.setSource("system");
        entity.setStatus(status);
        entity.setRetryCount(1);
        entity.setLastError("boom");
        entity.setPayloadSnapshot("{\"id\":100}");
        entity.setPublishedAt(LocalDateTime.now().minusMinutes(5));
        entity.setCreateTime(LocalDateTime.now().minusMinutes(6));
        return entity;
    }

    private MqProperties buildMqProperties() {
        MqProperties mqProperties = new MqProperties();
        mqProperties.setProcessingTimeoutSeconds(PROCESSING_TIMEOUT_SECONDS);
        return mqProperties;
    }
}
