package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.query.BiDashboardQuery;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;
import com.manzhushaka.biz.pii.application.result.BiDeptAggregateResult;
import com.manzhushaka.biz.pii.application.service.impl.BiReportServiceImpl;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.service.ISysDeptService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BiReportServiceTest {
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final ISysDeptService deptService = mock(ISysDeptService.class);
    private final BiCacheService biCacheService = mock(BiCacheService.class);
    private final BiReportService service = new BiReportServiceImpl(payOrderRepository, deptService, biCacheService);

    @Test
    void dashboardShouldCollectKpisAndChartsFromRepository() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
        BiDashboardQuery query = new BiDashboardQuery(100L, start, end);
        when(payOrderRepository.sumAmountByMerchantAndStatusBetween(100L, List.of("PAID", "REFUNDING", "REFUNDED"), start, end)).thenReturn(12000L);
        when(payOrderRepository.sumAmountByInvoiceStatusBetween(100L, "ISSUED", start, end)).thenReturn(8800L);
        when(payOrderRepository.countByMerchantAndPayTimeBetween(100L, start, end)).thenReturn(9L);
        when(payOrderRepository.countAbnormalOrders(100L, start, end)).thenReturn(2L);
        when(payOrderRepository.sumAmountByDay(100L, start, end)).thenReturn(List.of(new BiDashboardResult.TrendItem("2026-07-01", 12000L, 3L)));
        when(payOrderRepository.sumAmountByTaxItem(100L, start, end)).thenReturn(List.of(new BiDashboardResult.TaxItemRatioItem(10L, "餐饮服务", 8800L)));
        when(payOrderRepository.sumAmountByMerchantTop(100L, 10, start, end)).thenReturn(List.of(new BiDashboardResult.MerchantRankItem(100L, "测试商户", 12000L, 3L)));
        when(payOrderRepository.findAbnormalOrders(100L, start, end, 50)).thenReturn(List.of(new BiDashboardResult.AbnormalOrderItem(1L, "ORDER001", "FAILED", "PAID", 8800L)));

        BiDashboardResult result = service.dashboard(query);

        assertThat(result.getTotalAmount()).isEqualTo(12000L);
        assertThat(result.getTotalInvoiceAmount()).isEqualTo(8800L);
        assertThat(result.getTotalOrderCount()).isEqualTo(9L);
        assertThat(result.getAbnormalOrderCount()).isEqualTo(2L);
        assertThat(result.getTrend()).hasSize(1);
        assertThat(result.getTaxItemRatio()).hasSize(1);
        assertThat(result.getMerchantTop10()).hasSize(1);
        assertThat(result.getAbnormalOrders()).hasSize(1);
        verify(payOrderRepository).findAbnormalOrders(100L, start, end, 50);
        verify(biCacheService).putDashboard(query, result);
    }

    @Test
    void dashboardShouldReturnCachedResultWhenExists() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
        BiDashboardQuery query = new BiDashboardQuery(100L, start, end);
        BiDashboardResult cached = new BiDashboardResult();
        cached.setTotalAmount(9900L);
        when(biCacheService.getDashboard(query)).thenReturn(cached);

        BiDashboardResult result = service.dashboard(query);

        assertThat(result).isSameAs(cached);
        verify(payOrderRepository, org.mockito.Mockito.never())
                .sumAmountByMerchantAndStatusBetween(org.mockito.Mockito.any(), org.mockito.Mockito.any(), org.mockito.Mockito.any(), org.mockito.Mockito.any());
    }

    @Test
    void aggregateByDeptShouldCollectMapItemsFromChildRegions() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 3, 23, 59, 59);
        BiDashboardQuery query = new BiDashboardQuery(null, start, end);
        SysDept haikou = regionDept(201L, 200L, "海口市", "460100");
        SysDept sanya = regionDept(202L, 200L, "三亚市", "460200");
        when(deptService.selectDeptList(any(SysDept.class))).thenReturn(List.of(haikou, sanya));
        when(payOrderRepository.sumAmountByDeptId(201L, start, end)).thenReturn(12000L);
        when(payOrderRepository.countByDeptId(201L, start, end)).thenReturn(3L);
        when(payOrderRepository.countMerchantsByDeptId(201L)).thenReturn(2L);
        when(payOrderRepository.sumAmountByDeptId(202L, start, end)).thenReturn(8800L);
        when(payOrderRepository.countByDeptId(202L, start, end)).thenReturn(1L);
        when(payOrderRepository.countMerchantsByDeptId(202L)).thenReturn(1L);

        BiDeptAggregateResult result = service.aggregateByDept("city", 200L, query);

        assertThat(result.getLevel()).isEqualTo("city");
        assertThat(result.getItems()).extracting(BiDeptAggregateResult.BiDeptAggregateItem::getDeptName)
                .containsExactly("海口市", "三亚市");
        assertThat(result.getItems().get(0).getAmount()).isEqualTo(12000L);
        assertThat(result.getItems().get(0).getMerchantCount()).isEqualTo(2L);
        verify(biCacheService).putDeptAggregate("city", 200L, query, result);
    }

    private SysDept regionDept(Long deptId, Long parentId, String name, String regionCode) {
        SysDept dept = new SysDept();
        dept.setDeptId(deptId);
        dept.setParentId(parentId);
        dept.setDeptName(name);
        dept.setDeptType("region");
        dept.setRegionCode(regionCode);
        return dept;
    }
}
