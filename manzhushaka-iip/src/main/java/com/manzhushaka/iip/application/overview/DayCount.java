package com.manzhushaka.iip.application.overview;

/**
 * 单日统计结果。
 *
 * @param day 日期（yyyy-MM-dd）
 * @param cnt 当日数量
 * @author manzhushaka
 * @date 2026-07-18
 */
public record DayCount(String day, Long cnt)
{
    @Override
    public String toString()
    {
        return "DayCount[day=" + day + ", cnt=" + cnt + "]";
    }
}
