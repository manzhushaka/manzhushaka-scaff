package com.manzhushaka.system.service;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.domain.SysMqMessageStatusEnum;
import com.manzhushaka.system.mapper.SysMqMessageLogDetailMapper;
import com.manzhushaka.system.mapper.SysMqMessageLogMapper;
import com.manzhushaka.system.service.impl.SysMqMessageLogServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 消息队列台账服务测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class SysMqMessageLogServiceTest
{

    /**
     * 同一 stream 和 messageId 已存在时，应返回已有主表记录。
     */
    @Test
    void createOrGetMessageLogShouldReturnExistingLog()
    {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLog existing = new SysMqMessageLog();
        existing.setMessageLogId(100L);
        when(logMapper.selectMessageLogByStreamAndMessageId("mq:stream:order_paid", "168-0")).thenReturn(existing);

        SysMqMessageLog input = new SysMqMessageLog();
        input.setStreamKey("mq:stream:order_paid");
        input.setMessageId("168-0");

        SysMqMessageLog result = service.createOrGetMessageLog(input);

        assertThat(result.getMessageLogId()).isEqualTo(100L);
    }

    /**
     * 并发插入触发唯一索引冲突时，应重新查询已有记录。
     */
    @Test
    void createOrGetMessageLogShouldRecoverFromDuplicateKey()
    {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLog insertedByOtherThread = new SysMqMessageLog();
        insertedByOtherThread.setMessageLogId(101L);
        when(logMapper.selectMessageLogByStreamAndMessageId("mq:stream:order_paid", "169-0"))
                .thenReturn(null)
                .thenReturn(insertedByOtherThread);
        org.mockito.Mockito.doThrow(new DuplicateKeyException("uk_stream_message"))
                .when(logMapper).insertMessageLog(any(SysMqMessageLog.class));

        SysMqMessageLog input = new SysMqMessageLog();
        input.setStreamKey("mq:stream:order_paid");
        input.setMessageId("169-0");

        SysMqMessageLog result = service.createOrGetMessageLog(input);

        assertThat(result.getMessageLogId()).isEqualTo(101L);
    }

    /**
     * 更新主表状态时应委托 Mapper。
     */
    @Test
    void updateMessageLogShouldDelegateMapper()
    {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLog log = new SysMqMessageLog();
        log.setMessageLogId(100L);
        log.setStatus(SysMqMessageStatusEnum.SUCCESS.getCode());
        log.setSuccessTime(new Date());

        service.updateMessageLog(log);

        verify(logMapper).updateMessageLog(log);
    }

    /**
     * 新增明细后应返回带主键的明细对象。
     */
    @Test
    void insertMessageLogDetailShouldReturnDetail()
    {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
        detail.setMessageLogId(100L);
        org.mockito.Mockito.doAnswer(invocation -> {
            SysMqMessageLogDetail value = invocation.getArgument(0);
            value.setDetailId(200L);
            return 1;
        }).when(detailMapper).insertMessageLogDetail(detail);

        SysMqMessageLogDetail result = service.insertMessageLogDetail(detail);

        assertThat(result.getDetailId()).isEqualTo(200L);
    }
}