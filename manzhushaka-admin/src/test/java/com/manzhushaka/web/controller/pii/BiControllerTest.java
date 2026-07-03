package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.query.BiDashboardQuery;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;
import com.manzhushaka.biz.pii.application.result.BiDeptAggregateResult;
import com.manzhushaka.biz.pii.application.service.BiReportService;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.web.dto.pii.BiDashboardRequest;
import com.manzhushaka.web.dto.pii.BiDeptAggregateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BiControllerTest {
    private final BiReportService biReportService = mock(BiReportService.class);
    private final BiController controller = new BiController(biReportService);

    @Test
    void dataShouldReturnDashboardResult() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
        BiDashboardRequest request = new BiDashboardRequest();
        request.setMerchantId(100L);
        request.setStartTime(start);
        request.setEndTime(end);
        BiDashboardResult dashboard = new BiDashboardResult();
        dashboard.setTotalAmount(12000L);
        when(biReportService.dashboard(new BiDashboardQuery(100L, start, end))).thenReturn(dashboard);

        AjaxResult result = controller.data(request);

        assertThat(result.get("data")).isSameAs(dashboard);
        verify(biReportService).dashboard(new BiDashboardQuery(100L, start, end));
    }

    @Test
    void deptAggregateShouldReturnAggregateResult() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
        BiDeptAggregateRequest request = new BiDeptAggregateRequest();
        request.setLevel("district");
        request.setParentDeptId(46L);
        request.setMerchantId(100L);
        request.setStartTime(start);
        request.setEndTime(end);
        BiDeptAggregateResult aggregate = new BiDeptAggregateResult("district", List.of());
        when(biReportService.aggregateByDept(
                argThat(level -> "district".equals(level)),
                argThat(parentDeptId -> parentDeptId.equals(46L)),
                argThat(query -> query.merchantId().equals(100L)
                        && query.startTime().equals(start)
                        && query.endTime().equals(end))))
                .thenReturn(aggregate);

        AjaxResult result = controller.deptAggregate(request);

        assertThat(result.get("data")).isSameAs(aggregate);
    }
}
