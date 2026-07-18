package com.manzhushaka.iip.application.overview;

import java.util.List;

/**
 * 数据概览近 7 日趋势结果。
 *
 * @param invoiceTrend 发票上传趋势
 * @param pointsTrend 积分发放趋势
 * @param exchangeTrend 券兑换趋势
 * @author manzhushaka
 * @date 2026-07-18
 */
public record TrendResult(List<DayCount> invoiceTrend, List<DayCount> pointsTrend, List<DayCount> exchangeTrend)
{
    @Override
    public String toString()
    {
        return "TrendResult[invoiceTrend=" + invoiceTrend + ", pointsTrend=" + pointsTrend
                + ", exchangeTrend=" + exchangeTrend + "]";
    }
}
