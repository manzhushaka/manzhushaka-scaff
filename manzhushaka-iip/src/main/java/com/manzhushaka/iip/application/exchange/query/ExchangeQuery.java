package com.manzhushaka.iip.application.exchange.query;

/**
 * 兑换记录查询条件（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ExchangeQuery(String couponName, Long memberId, String status, String verifyCode,
        String beginTime, String endTime)
{
}
