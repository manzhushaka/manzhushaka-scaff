package com.manzhushaka.iip.application.points.result;

import java.util.Date;

/**
 * 积分流水结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record PointsRecordResult(Long recordId, Long memberId, String changeType, Integer points,
        Integer balanceAfter, String bizType, String bizId, Integer remaining, Date expireTime,
        Date createTime, String remark)
{
    @Override
    public String toString()
    {
        return "PointsRecordResult[recordId=" + recordId + ", memberId=" + memberId
                + ", changeType=" + changeType + ", points=" + points + "]";
    }
}
