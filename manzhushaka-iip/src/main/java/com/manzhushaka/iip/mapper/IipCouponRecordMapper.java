package com.manzhushaka.iip.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipCouponRecord;

/**
 * 券实例（兑换记录） 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipCouponRecordMapper
{
    /**
     * 通过ID查询券实例
     * 
     * @param recordId 记录ID
     * @return 券实例信息
     */
    public IipCouponRecord selectIipCouponRecordById(Long recordId);

    /**
     * 查询券实例列表
     * 
     * @param iipCouponRecord 查询条件
     * @return 券实例集合
     */
    public List<IipCouponRecord> selectIipCouponRecordList(IipCouponRecord iipCouponRecord);

    /**
     * 新增券实例
     * 
     * @param iipCouponRecord 券实例信息
     * @return 结果
     */
    public int insertIipCouponRecord(IipCouponRecord iipCouponRecord);

    /**
     * 修改券实例
     * 
     * @param iipCouponRecord 券实例信息
     * @return 结果
     */
    public int updateIipCouponRecord(IipCouponRecord iipCouponRecord);

    /**
     * 通过ID删除券实例
     * 
     * @param recordId 记录ID
     * @return 结果
     */
    public int deleteIipCouponRecordById(Long recordId);

    /**
     * 批量删除券实例
     * 
     * @param recordIds 需要删除的记录ID
     * @return 结果
     */
    public int deleteIipCouponRecordByIds(Long[] recordIds);

    /**
     * 按核销码查询券实例（商户核销入口）
     * 
     * @param verifyCode 核销码
     * @return 券实例信息，不存在时返回null
     */
    public IipCouponRecord selectByVerifyCode(String verifyCode);

    /**
     * 统计用户对某券的兑换数量（每人限兑校验）；memberId 为 null 时统计该券全表兑换数量
     *
     * @param couponId 券ID
     * @param memberId 用户ID，可为 null
     * @return 兑换数量
     */
    public int countByCouponAndMember(@Param("couponId") Long couponId, @Param("memberId") Long memberId);

    /**
     * 查询用户本人的券实例列表（按兑换时间倒序）
     *
     * @param memberId 用户ID
     * @param status 状态（0未使用 1已使用 2已过期），null 或空表示全部
     * @return 券实例集合
     */
    public List<IipCouponRecord> selectByMember(@Param("memberId") Long memberId, @Param("status") String status);

    /**
     * 将用户本人已过期的未使用券批量置为已过期
     *
     * @param memberId 用户ID
     * @param now 当前时间
     * @return 影响行数
     */
    public int expireByMember(@Param("memberId") Long memberId, @Param("now") Date now);
}
