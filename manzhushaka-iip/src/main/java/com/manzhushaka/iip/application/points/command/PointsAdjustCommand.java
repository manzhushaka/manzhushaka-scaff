package com.manzhushaka.iip.application.points.command;

/**
 * 积分手工调整命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record PointsAdjustCommand(Long memberId, Integer points, String remark)
{
    @Override
    public String toString()
    {
        return "PointsAdjustCommand[memberId=" + memberId + ", points=" + points + "]";
    }
}
