package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.points.command.PointsAdjustCommand;
import com.manzhushaka.iip.application.points.query.PointsAccountQuery;
import com.manzhushaka.iip.application.points.query.PointsRecordQuery;
import com.manzhushaka.web.dto.iip.PointsAccountQueryRequest;
import com.manzhushaka.web.dto.iip.PointsAdjustRequest;
import com.manzhushaka.web.dto.iip.PointsRecordQueryRequest;

/**
 * 积分管理 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class PointsAdminConverter
{
    private PointsAdminConverter()
    {
    }

    public static PointsAccountQuery toAccountQuery(PointsAccountQueryRequest request)
    {
        return new PointsAccountQuery(request.getMemberId(), request.getNickname());
    }

    public static PointsRecordQuery toRecordQuery(PointsRecordQueryRequest request)
    {
        return new PointsRecordQuery(request.getMemberId(), request.getChangeType(), request.getBizType(),
                request.getBeginTime(), request.getEndTime());
    }

    public static PointsAdjustCommand toCommand(PointsAdjustRequest request)
    {
        return new PointsAdjustCommand(request.getMemberId(), request.getPoints(), request.getRemark());
    }
}
