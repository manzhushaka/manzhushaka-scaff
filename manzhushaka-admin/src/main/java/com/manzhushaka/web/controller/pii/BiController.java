package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.query.BiDashboardQuery;
import com.manzhushaka.biz.pii.application.service.BiReportService;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.web.dto.pii.BiDashboardRequest;
import com.manzhushaka.web.dto.pii.BiDeptAggregateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pii/bi")
public class BiController extends BaseController {
    private final BiReportService biReportService;

    public BiController(BiReportService biReportService) {
        this.biReportService = biReportService;
    }

    @PreAuthorize("@ss.hasPermi('biz:bi:dashboard')")
    @GetMapping("/data")
    public AjaxResult data(BiDashboardRequest request) {
        return success(biReportService.dashboard(toQuery(request)));
    }

    @PreAuthorize("@ss.hasPermi('biz:bi:dashboard')")
    @GetMapping("/dept/aggregate")
    public AjaxResult deptAggregate(BiDeptAggregateRequest request) {
        return success(biReportService.aggregateByDept(request.getLevel(), request.getParentDeptId(), toQuery(request)));
    }

    private BiDashboardQuery toQuery(BiDashboardRequest request) {
        return new BiDashboardQuery(request.getMerchantId(), request.getStartTime(), request.getEndTime());
    }
}
