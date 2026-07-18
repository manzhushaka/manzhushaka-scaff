package com.manzhushaka.iip.application.points.result;

import java.util.Date;

/**
 * 积分账户结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record PointsAccountResult(Long accountId, Long memberId, String nickname,
        Integer totalPoints, Integer availablePoints, Integer usedPoints, Integer expiredPoints,
        Date createTime, Date updateTime, String remark)
{
    @Override
    public String toString()
    {
        return "PointsAccountResult[accountId=" + accountId + ", memberId=" + memberId
                + ", availablePoints=" + availablePoints + "]";
    }
}
