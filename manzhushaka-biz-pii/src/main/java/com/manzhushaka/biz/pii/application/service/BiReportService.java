package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.query.BiDashboardQuery;
import com.manzhushaka.biz.pii.application.result.BiDashboardResult;
import com.manzhushaka.biz.pii.application.result.BiDeptAggregateResult;

public interface BiReportService {
    BiDashboardResult dashboard(BiDashboardQuery query);

    BiDeptAggregateResult aggregateByDept(String level, Long parentDeptId, BiDashboardQuery query);
}
