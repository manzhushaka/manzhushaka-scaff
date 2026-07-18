package com.manzhushaka.iip.application.coupon.result;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 券详情结果（小程序端，含当前用户已兑数量与商户展示信息）。
 *
 * merchantName/merchantLogo/merchantDescription/merchantAddress/merchantPhone/
 * merchantBusinessHours/merchantLongitude/merchantLatitude
 * 仅当券绑定商户且商户存在时有值，否则为 null。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record CouponDetailResult(
        Long couponId,
        String couponName,
        String couponType,
        String coverImage,
        String targetName,
        Integer pointsCost,
        Integer remainStock,
        Integer perMemberLimit,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date exchangeStartTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date exchangeEndTime,
        String validType,
        Integer validDays,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validStartTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date validEndTime,
        BigDecimal thresholdAmount,
        BigDecimal discountAmount,
        Long merchantId,
        String useDesc,
        String status,
        String category,
        String sponsorType,
        String sponsorName,
        Integer exchangedCount,
        String merchantName,
        String merchantLogo,
        String merchantDescription,
        String merchantAddress,
        String merchantPhone,
        String merchantBusinessHours,
        BigDecimal merchantLongitude,
        BigDecimal merchantLatitude)
{
    @Override
    public String toString()
    {
        return "CouponDetailResult[couponId=" + couponId + ", couponName=" + couponName
                + ", exchangedCount=" + exchangedCount + ", merchantName=" + merchantName + "]";
    }
}
