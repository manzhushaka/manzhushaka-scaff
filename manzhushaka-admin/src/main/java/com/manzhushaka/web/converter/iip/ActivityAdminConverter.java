package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.command.ActivityMerchantCommand;
import com.manzhushaka.iip.application.command.SaveActivityCommand;
import com.manzhushaka.iip.application.command.SaveActivityCouponCommand;
import com.manzhushaka.iip.application.query.ActivityQuery;
import com.manzhushaka.web.dto.iip.ActivityCouponConfigRequest;
import com.manzhushaka.web.dto.iip.ActivityMerchantConfigRequest;
import com.manzhushaka.web.dto.iip.ActivityRequest;

/**
 * 活动 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class ActivityAdminConverter
{
    private ActivityAdminConverter()
    {
    }

    public static ActivityQuery toQuery(ActivityRequest request)
    {
        return new ActivityQuery(request.getActivityNo(), request.getActivityName(), request.getStatus(),
                getParam(request, "beginTime"), getParam(request, "endTime"));
    }

    public static SaveActivityCommand toCommand(ActivityRequest request)
    {
        return new SaveActivityCommand(request.getActivityId(), request.getActivityName(), request.getCoverImage(),
                request.getDescription(), request.getStartTime(), request.getEndTime(), request.getPointsRatio(),
                request.getMerchantLimit(), request.getCouponQuota(), request.getCity(), request.getRegionType(),
                request.getRegionName(), request.getPriority(), request.getStatus(), request.getRemark(),
                request.getSingleInvoiceCap(), request.getMonthlyMemberCap(), request.getMerchantScope());
    }

    public static ActivityMerchantCommand toMerchantCommand(ActivityMerchantConfigRequest request)
    {
        return new ActivityMerchantCommand(request.getActivityId(), request.getMerchantId());
    }

    public static SaveActivityCouponCommand toCouponCommand(ActivityCouponConfigRequest request)
    {
        return new SaveActivityCouponCommand(request.getId(), request.getActivityId(), request.getCouponId(),
                request.getIssueLimit());
    }

    private static String getParam(ActivityRequest request, String name)
    {
        Object value = request.getParams().get(name);
        return value == null ? null : value.toString();
    }
}
