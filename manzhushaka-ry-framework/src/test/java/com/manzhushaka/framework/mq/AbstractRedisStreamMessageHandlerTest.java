package com.manzhushaka.framework.mq;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.domain.SysMqMessageStatusEnum;
import com.manzhushaka.system.service.ISysMqMessageLogService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 模板方法 handler 测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class AbstractRedisStreamMessageHandlerTest
{

    /**
     * 业务处理成功时应更新主表成功、明细成功并 ACK。
     */
    @Test
    void handleShouldMarkSuccessAndAck()
    {
        RedisStreamGateway gateway = mock(RedisStreamGateway.class);
        ISysMqMessageLogService logService = mock(ISysMqMessageLogService.class);
        TestHandler handler = new TestHandler(gateway, logService);
        SysMqMessageLog log = new SysMqMessageLog();
        log.setMessageLogId(100L);
        log.setRetryTimes(0);
        when(logService.createOrGetMessageLog(any(SysMqMessageLog.class))).thenReturn(log);
        SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
        detail.setDetailId(200L);
        when(logService.insertMessageLogDetail(any(SysMqMessageLogDetail.class))).thenReturn(detail);

        handler.handle(new RedisStreamRecord("mq:stream:test", "168-0", body("test", "BIZ-1", "{}", "0")));

        assertThat(handler.handled).isTrue();
        verify(gateway).acknowledge("mq:stream:test", "mq-group-test", "168-0");
        org.mockito.ArgumentCaptor<SysMqMessageLog> logCaptor =
                org.mockito.ArgumentCaptor.forClass(SysMqMessageLog.class);
        verify(logService, org.mockito.Mockito.atLeastOnce()).updateMessageLog(logCaptor.capture());
        assertThat(logCaptor.getAllValues()).anyMatch(value -> SysMqMessageStatusEnum.SUCCESS.getCode().equals(value.getStatus()));
    }

    private static Map<String, String> body(String messageType, String businessKey, String payload, String retryTimes)
    {
        Map<String, String> body = new HashMap<>();
        body.put("messageType", messageType);
        body.put("businessKey", businessKey);
        body.put("payload", payload);
        body.put("retryTimes", retryTimes);
        return body;
    }

    private static class TestHandler extends AbstractRedisStreamMessageHandler
    {
        private boolean handled;
        private boolean alreadyProcessed;
        private RuntimeException failure;

        TestHandler(RedisStreamGateway gateway, ISysMqMessageLogService logService)
        {
            super(gateway, logService);
        }

        @Override
        public String messageType()
        {
            return "test";
        }

        @Override
        public String streamKey()
        {
            return "mq:stream:test";
        }

        @Override
        public String consumerGroup()
        {
            return "mq-group-test";
        }

        @Override
        public String consumerName()
        {
            return "mq-consumer-test";
        }

        @Override
        protected boolean isAlreadyProcessed(RedisStreamRecord record)
        {
            return alreadyProcessed;
        }

        @Override
        protected void doHandle(RedisStreamRecord record)
        {
            handled = true;
            if (failure != null)
            {
                throw failure;
            }
        }
    }
}