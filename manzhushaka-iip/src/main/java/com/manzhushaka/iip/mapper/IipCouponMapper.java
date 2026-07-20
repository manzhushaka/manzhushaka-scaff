package com.manzhushaka.iip.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipCoupon;

/**
 * 券定义 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipCouponMapper
{
    /**
     * 通过ID查询券
     * 
     * @param couponId 券ID
     * @return 券信息
     */
    public IipCoupon selectIipCouponById(Long couponId);

    /**
     * 查询券列表
     * 
     * @param iipCoupon 查询条件
     * @return 券集合
     */
    public List<IipCoupon> selectIipCouponList(IipCoupon iipCoupon);

    /**
     * 新增券
     * 
     * @param iipCoupon 券信息
     * @return 结果
     */
    public int insertIipCoupon(IipCoupon iipCoupon);

    /**
     * 修改券
     * 
     * @param iipCoupon 券信息
     * @return 结果
     */
    public int updateIipCoupon(IipCoupon iipCoupon);

    /**
     * 通过ID删除券
     * 
     * @param couponId 券ID
     * @return 结果
     */
    public int deleteIipCouponById(Long couponId);

    /**
     * 批量删除券
     * 
     * @param couponIds 需要删除的券ID
     * @return 结果
     */
    public int deleteIipCouponByIds(Long[] couponIds);

    /**
     * 原子扣减券剩余库存（remain_stock 大于 0 时减 1）
     *
     * @param couponId 券ID
     * @return 影响行数，0 表示库存不足
     */
    public int decrStock(Long couponId);

    /**
     * 作废未使用券后恢复一份库存；不限库存券保持不变。
     *
     * @param couponId 券ID
     * @return 影响行数
     */
    public int restoreStock(Long couponId);

    /**
     * 查询积分商城券列表（上架且兑换窗口为空或当前在窗口内，按 sort、create_time desc）
     *
     * @param now 当前时间
     * @param category 券品类，null 或空表示全部
     * @return 券集合
     */
    public List<IipCoupon> selectMallCouponList(@Param("now") Date now, @Param("category") String category);
}
