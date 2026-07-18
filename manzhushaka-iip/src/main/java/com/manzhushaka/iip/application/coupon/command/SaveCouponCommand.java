package com.manzhushaka.iip.application.coupon.command;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 券定义保存命令（新增/修改共用）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveCouponCommand(Long couponId, String couponName, String couponType, String coverImage,
        String targetName, Integer pointsCost, Integer totalStock, Integer remainStock, Integer perMemberLimit,
        Date exchangeStartTime, Date exchangeEndTime, String validType, Date validStartTime, Date validEndTime,
        Integer validDays, BigDecimal thresholdAmount, BigDecimal discountAmount, Long merchantId, String useDesc,
        String status, Integer sort, String remark, String category, String sponsorType, String sponsorName)
{
    @Override
    public String toString()
    {
        return "SaveCouponCommand[couponId=" + couponId + ", couponName=" + couponName
                + ", couponType=" + couponType + ", pointsCost=" + pointsCost + "]";
    }
}
