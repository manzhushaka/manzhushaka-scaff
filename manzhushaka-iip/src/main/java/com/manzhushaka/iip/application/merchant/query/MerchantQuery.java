package com.manzhushaka.iip.application.merchant.query;

/**
 * 商户查询条件（管理端列表/导出）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MerchantQuery(String merchantNo, String merchantName, String category, String status,
        String beginTime, String endTime)
{
}
