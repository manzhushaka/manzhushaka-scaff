package com.manzhushaka.iip.service;

import java.util.List;
import com.manzhushaka.iip.domain.IipCouponRecord;

/**
 * 券实例（兑换记录） 服务层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IIipCouponRecordService
{
    /**
     * 通过ID查询券实例
     * 
     * @param recordId 记录ID
     * @return 券实例信息，不存在时返回null
     */
    public IipCouponRecord selectIipCouponRecordById(Long recordId);

    /**
     * 查询券实例列表（管理端，按券名/用户/状态/核销码/兑换时间筛选）
     * 
     * @param iipCouponRecord 查询条件
     * @return 券实例集合
     */
    public List<IipCouponRecord> selectIipCouponRecordList(IipCouponRecord iipCouponRecord);

    /**
     * 查询用户本人的券实例列表（按兑换时间倒序）
     * 
     * @param memberId 用户ID
     * @param status 状态（0未使用 1已使用 2已过期），null 或空表示全部
     * @return 券实例集合
     */
    public List<IipCouponRecord> selectByMember(Long memberId, String status);

    /**
     * 统计兑换数量；memberId 为 null 时统计该券全表兑换数量
     * 
     * @param couponId 券ID
     * @param memberId 用户ID，可为 null
     * @return 兑换数量
     */
    public int countByCouponAndMember(Long couponId, Long memberId);

    /**
     * 将用户本人已过期的未使用券批量置为已过期
     * 
     * @param memberId 用户ID
     * @return 影响行数
     */
    public int expireByMember(Long memberId);

    /**
     * 兑换券（事务：上架与窗口校验、限兑校验、原子扣库存、活动发券额度、生成核销码、插入券实例、扣减积分）
     * 
     * @param memberId 用户ID
     * @param couponId 券ID
     * @return 兑换成功的券实例（含核销码）
     */
    public IipCouponRecord exchangeCoupon(Long memberId, Long couponId);
}
