package com.manzhushaka.web.controller.monitor;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.service.ISysMqMessageLogService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 消息队列台账控制器测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class SysMqMessageLogControllerTest
{

    /**
     * 详情接口应返回主台账。
     */
    @Test
    void getInfoShouldReturnMessageLog()
    {
        ISysMqMessageLogService service = mock(ISysMqMessageLogService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "mqMessageLogService", service);
        SysMqMessageLog log = new SysMqMessageLog();
        log.setMessageLogId(100L);
        when(service.selectMessageLogById(100L)).thenReturn(log);

        AjaxResult result = controller.getInfo(100L);

        assertThat(result.get("data")).isSameAs(log);
    }

    /**
     * 明细接口应返回指定主台账下的执行明细。
     */
    @Test
    void detailListShouldReturnDetails()
    {
        ISysMqMessageLogService service = mock(ISysMqMessageLogService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "mqMessageLogService", service);
        SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
        detail.setMessageLogId(100L);
        when(service.selectDetailListByMessageLogId(100L)).thenReturn(Collections.singletonList(detail));

        AjaxResult result = controller.detailList(100L);

        assertThat(result.get("data")).isEqualTo(Collections.singletonList(detail));
    }

    /**
     * 删除接口应委托服务删除主表和明细。
     */
    @Test
    void removeShouldDeleteLogs()
    {
        ISysMqMessageLogService service = mock(ISysMqMessageLogService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "mqMessageLogService", service);
        when(service.deleteMessageLogByIds(new Long[] {100L})).thenReturn(1);

        controller.remove(new Long[] {100L});

        verify(service).deleteMessageLogByIds(new Long[] {100L});
    }
}