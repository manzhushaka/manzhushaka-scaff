package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.query.BiDashboardQuery;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;
import com.manzhushaka.biz.pii.application.result.BiDeptAggregateResult;
import com.manzhushaka.biz.pii.application.service.BiCacheService;
import com.manzhushaka.biz.pii.application.service.BiReportService;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.service.ISysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BiReportServiceImpl implements BiReportService {
    private static final List<String> TURNOVER_PAY_STATUSES = List.of("PAID", "REFUNDING", "REFUNDED");

    private final PayOrderRepository payOrderRepository;
    private final ISysDeptService deptService;
    private final BiCacheService biCacheService;

    public BiReportServiceImpl(PayOrderRepository payOrderRepository, ISysDeptService deptService, BiCacheService biCacheService) {
        this.payOrderRepository = payOrderRepository;
        this.deptService = deptService;
        this.biCacheService = biCacheService;
    }

    @Override
    public BiDashboardResult dashboard(BiDashboardQuery query) {
        BiDashboardResult cached = biCacheService.getDashboard(query);
        if (cached != null) {
            return cached;
        }
        Long merchantId = query.merchantId();
        BiDashboardResult result = new BiDashboardResult();
        result.setTotalAmount(payOrderRepository.sumAmountByMerchantAndStatusBetween(merchantId, TURNOVER_PAY_STATUSES,
                query.startTime(), query.endTime()));
        result.setTotalInvoiceAmount(payOrderRepository.sumAmountByInvoiceStatusBetween(merchantId, "ISSUED",
                query.startTime(), query.endTime()));
        result.setTotalOrderCount(payOrderRepository.countByMerchantAndPayTimeBetween(merchantId,
                query.startTime(), query.endTime()));
        result.setAbnormalOrderCount(payOrderRepository.countAbnormalOrders(merchantId, query.startTime(), query.endTime()));
        result.setTrend(payOrderRepository.sumAmountByDay(merchantId, query.startTime(), query.endTime()));
        result.setTaxItemRatio(payOrderRepository.sumAmountByTaxItem(merchantId, query.startTime(), query.endTime()));
        result.setMerchantTop10(payOrderRepository.sumAmountByMerchantTop(merchantId, 10, query.startTime(), query.endTime()));
        result.setAbnormalOrders(payOrderRepository.findAbnormalOrders(merchantId, query.startTime(), query.endTime(), 50));
        biCacheService.putDashboard(query, result);
        return result;
    }

    @Override
    public BiDeptAggregateResult aggregateByDept(String level, Long parentDeptId, BiDashboardQuery query) {
        BiDeptAggregateResult cached = biCacheService.getDeptAggregate(level, parentDeptId, query);
        if (cached != null) {
            return cached;
        }
        SysDept condition = new SysDept();
        condition.setParentId(parentDeptId);
        condition.setDeptType("region");
        condition.setRegionLevel("district".equals(level) ? 3 : 2);
        List<SysDept> depts = deptService.selectDeptList(condition);
        List<BiDeptAggregateResult.BiDeptAggregateItem> items = depts.stream()
                .map(dept -> new BiDeptAggregateResult.BiDeptAggregateItem(
                        dept.getDeptId(),
                        dept.getDeptName(),
                        dept.getRegionCode(),
                        payOrderRepository.sumAmountByDeptId(dept.getDeptId(), query.startTime(), query.endTime()),
                        payOrderRepository.countByDeptId(dept.getDeptId(), query.startTime(), query.endTime()),
                        payOrderRepository.countMerchantsByDeptId(dept.getDeptId())))
                .collect(Collectors.toList());
        BiDeptAggregateResult result = new BiDeptAggregateResult(level, items);
        biCacheService.putDeptAggregate(level, parentDeptId, query, result);
        return result;
    }
}
