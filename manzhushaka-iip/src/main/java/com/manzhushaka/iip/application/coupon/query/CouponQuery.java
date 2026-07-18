package com.manzhushaka.iip.application.coupon.query;

/**
 * 券定义查询条件（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record CouponQuery(String couponName, String couponType, String status, String category)
{
}
