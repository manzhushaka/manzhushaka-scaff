package com.manzhushaka.iip.application.query;

/**
 * 活动查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ActivityQuery(String activityNo, String activityName, String status,
        String beginTime, String endTime)
{
}
