package com.manzhushaka.iip.application.overview;

/**
 * 数据概览应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface OverviewAppService
{
    /**
     * 查询数据概览汇总指标。
     *
     * @return 汇总结果
     */
    SummaryResult getSummary();

    /**
     * 查询近 7 日发票、积分、兑换趋势。
     *
     * @return 趋势结果
     */
    TrendResult getTrend();
}
