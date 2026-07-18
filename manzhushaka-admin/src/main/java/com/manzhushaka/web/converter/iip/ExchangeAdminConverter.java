package com.manzhushaka.web.converter.iip;

import com.manzhushaka.iip.application.exchange.query.ExchangeQuery;
import com.manzhushaka.web.dto.iip.ExchangeQueryRequest;

/**
 * 兑换记录 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class ExchangeAdminConverter
{
    private ExchangeAdminConverter()
    {
    }

    /**
     * 转换为兑换记录查询条件。
     *
     * @param request 兑换记录查询请求
     * @return 查询条件
     */
    public static ExchangeQuery toQuery(ExchangeQueryRequest request)
    {
        return new ExchangeQuery(request.getCouponName(), request.getMemberId(), request.getStatus(),
                request.getVerifyCode(), request.getBeginTime(), request.getEndTime());
    }
}
