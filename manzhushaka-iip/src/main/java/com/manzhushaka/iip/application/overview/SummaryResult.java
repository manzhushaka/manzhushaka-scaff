package com.manzhushaka.iip.application.overview;

/**
 * 数据概览汇总结果。
 *
 * @param memberCount 小程序用户总数
 * @param merchantCount 商户总数
 * @param pendingMerchantCount 待审核商户数
 * @param pendingInvoiceCount 待审核发票数
 * @param approvedInvoiceCount 已通过发票数
 * @param rejectedInvoiceCount 已驳回发票数
 * @param pointsIssued 累计发放积分
 * @param pointsConsumed 累计消耗积分
 * @param couponExchangeCount 券兑换记录总数
 * @param verifiedCouponCount 已核销券数
 * @param activeActivityCount 当前生效活动数
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SummaryResult(Long memberCount, Long merchantCount, Long pendingMerchantCount,
        Long pendingInvoiceCount, Long approvedInvoiceCount, Long rejectedInvoiceCount,
        Long pointsIssued, Long pointsConsumed, Long couponExchangeCount,
        Long verifiedCouponCount, Long activeActivityCount)
{
    @Override
    public String toString()
    {
        return "SummaryResult[memberCount=" + memberCount + ", merchantCount=" + merchantCount
                + ", pendingMerchantCount=" + pendingMerchantCount + ", pendingInvoiceCount=" + pendingInvoiceCount
                + ", approvedInvoiceCount=" + approvedInvoiceCount + ", rejectedInvoiceCount=" + rejectedInvoiceCount
                + ", pointsIssued=" + pointsIssued + ", pointsConsumed=" + pointsConsumed
                + ", couponExchangeCount=" + couponExchangeCount + ", verifiedCouponCount=" + verifiedCouponCount
                + ", activeActivityCount=" + activeActivityCount + "]";
    }
}
