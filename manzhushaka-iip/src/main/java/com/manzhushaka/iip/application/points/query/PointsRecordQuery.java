package com.manzhushaka.iip.application.points.query;

/**
 * 积分流水查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record PointsRecordQuery(Long memberId, String changeType, String bizType,
        String beginTime, String endTime)
{
}
