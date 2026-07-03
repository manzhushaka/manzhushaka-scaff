package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.query.BiDashboardQuery;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;
import com.manzhushaka.biz.pii.application.result.BiDeptAggregateResult;

public interface BiCacheService {
    BiDashboardResult getDashboard(BiDashboardQuery query);

    void putDashboard(BiDashboardQuery query, BiDashboardResult result);

    BiDeptAggregateResult getDeptAggregate(String level, Long parentDeptId, BiDashboardQuery query);

    void putDeptAggregate(String level, Long parentDeptId, BiDashboardQuery query, BiDeptAggregateResult result);

    void evictAll();
}
