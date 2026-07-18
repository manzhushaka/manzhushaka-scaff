package com.manzhushaka.iip.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.application.merchant.result.MerchantVerifyStatsResult;
import com.manzhushaka.iip.domain.IipCouponRecord;

/**
 * 商户核销 数据层。
 *
 * 归属商户域：为解决券实例核销的并发安全问题，在本域直接对 iip_coupon_record 表
 * 手写带状态守护的原子更新 SQL，避免跨域修改券域 Mapper 文件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipMerchantVerifyMapper
{
    /**
     * 原子核销券实例（仅 status='0' 未使用可核销，防并发重复核销）。
     *
     * 执行 update iip_coupon_record set status='1', verify_time=now,
     * verify_merchant_id=?, verify_by=? where record_id=? and status='0'
     *
     * @param recordId 券实例ID
     * @param verifyMerchantId 核销商户ID
     * @param verifyBy 核销操作人
     * @return 影响行数，0 表示券状态已变化（已被核销或已过期）
     */
    public int verifyCouponRecordAtomic(@Param("recordId") Long recordId,
            @Param("verifyMerchantId") Long verifyMerchantId, @Param("verifyBy") String verifyBy);

    /**
     * 原子将未使用券实例置为已过期（仅 status='0' 可置位，防并发覆盖已核销结果）
     *
     * @param recordId 券实例ID
     * @return 影响行数
     */
    public int markCouponRecordExpired(@Param("recordId") Long recordId);

    /**
     * 查询指定商户的核销记录（按核销时间倒序）
     *
     * @param merchantId 商户ID
     * @param days 最近天数（可空，如 1/7/30 表示 verify_time 不早于当前时间往前 days 天；null 为全部）
     * @return 该商户核销的券实例集合
     */
    public List<IipCouponRecord> selectVerifyRecordsByMerchantId(@Param("merchantId") Long merchantId,
            @Param("days") Integer days);

    /**
     * 统计指定商户的核销数据（仅 status='1' 已核销记录，今日按 verify_time 不早于当日 00:00 计算）
     *
     * @param merchantId 商户ID
     * @return 核销统计（今日笔数/今日积分/累计笔数/累计积分）
     */
    public MerchantVerifyStatsResult selectVerifyStatsByMerchantId(@Param("merchantId") Long merchantId);
}
