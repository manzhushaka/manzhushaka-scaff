package com.manzhushaka.web.controller.iip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.iip.application.overview.OverviewAppService;

/**
 * 数据概览 信息操作处理
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/iip/overview")
public class IipOverviewController extends BaseController
{
    @Autowired
    private OverviewAppService overviewAppService;

    /**
     * 获取数据概览汇总指标
     */
    @PreAuthorize("@ss.hasPermi('iip:overview:list')")
    @Log(title = "数据概览", businessType = BusinessType.OTHER)
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(overviewAppService.getSummary());
    }

    /**
     * 获取近7日发票、积分、兑换趋势
     */
    @PreAuthorize("@ss.hasPermi('iip:overview:list')")
    @Log(title = "数据概览", businessType = BusinessType.OTHER)
    @GetMapping("/trend")
    public AjaxResult trend()
    {
        return success(overviewAppService.getTrend());
    }
}
