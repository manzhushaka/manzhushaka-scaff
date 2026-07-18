package com.manzhushaka.web.controller.monitor;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.system.application.result.system.MqMessageLogDetailResult;
import com.manzhushaka.system.application.result.system.MqMessageLogResult;
import com.manzhushaka.system.application.service.SystemAuditAppService;

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
        SystemAuditAppService service = mock(SystemAuditAppService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "auditAppService", service);
        MqMessageLogResult log = new MqMessageLogResult(100L, "TEST", "stream", "1-0",
                "group", "business", null, "1", 1, 3, null, null, null, null,
                null, null, null, null, null);
        when(service.getMqMessageLog(100L)).thenReturn(log);

        AjaxResult result = controller.getInfo(100L);

        assertThat(result.get("data")).isSameAs(log);
    }

    /**
     * 明细接口应返回指定主台账下的执行明细。
     */
    @Test
    void detailListShouldReturnDetails()
    {
        SystemAuditAppService service = mock(SystemAuditAppService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "auditAppService", service);
        MqMessageLogDetailResult detail = new MqMessageLogDetailResult(
                1L, 100L, 1, "consumer", "1", null, null, 10L, null);
        when(service.listMqMessageLogDetails(100L)).thenReturn(Collections.singletonList(detail));

        AjaxResult result = controller.detailList(100L);

        assertThat(result.get("data")).isEqualTo(Collections.singletonList(detail));
    }

    /**
     * 删除接口应委托服务删除主表和明细。
     */
    @Test
    void removeShouldDeleteLogs()
    {
        SystemAuditAppService service = mock(SystemAuditAppService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "auditAppService", service);
        when(service.deleteMqMessageLogs(new Long[] {100L})).thenReturn(1);

        controller.remove(new Long[] {100L});

        verify(service).deleteMqMessageLogs(new Long[] {100L});
    }
}
