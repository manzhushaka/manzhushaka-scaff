package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.coupon.command.SaveCouponCommand;
import com.manzhushaka.iip.application.coupon.query.CouponQuery;
import com.manzhushaka.web.dto.iip.CouponRequest;

/**
 * 券定义 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class CouponAdminConverter
{
    private CouponAdminConverter()
    {
    }

    /**
     * 转换为券列表查询条件。
     *
     * @param request 券请求
     * @return 查询条件
     */
    public static CouponQuery toQuery(CouponRequest request)
    {
        return new CouponQuery(request.getCouponName(), request.getCouponType(), request.getStatus(),
                request.getCategory());
    }

    /**
     * 转换为券保存命令。
     *
     * @param request 券请求
     * @return 保存命令
     */
    public static SaveCouponCommand toCommand(CouponRequest request)
    {
        return new SaveCouponCommand(request.getCouponId(), request.getCouponName(), request.getCouponType(),
                request.getCoverImage(), request.getTargetName(), request.getPointsCost(), request.getTotalStock(),
                request.getRemainStock(), request.getPerMemberLimit(), request.getExchangeStartTime(),
                request.getExchangeEndTime(), request.getValidType(), request.getValidStartTime(),
                request.getValidEndTime(), request.getValidDays(), request.getThresholdAmount(),
                request.getDiscountAmount(), request.getMerchantId(), request.getUseDesc(), request.getStatus(),
                request.getSort(), request.getRemark(), request.getCategory(), request.getSponsorType(),
                request.getSponsorName());
    }
}
