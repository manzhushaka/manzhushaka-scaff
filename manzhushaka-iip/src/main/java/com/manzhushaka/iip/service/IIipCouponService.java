package com.manzhushaka.iip.service;

import java.util.Date;
import java.util.List;
import com.manzhushaka.iip.domain.IipCoupon;

/**
 * 券定义 服务层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IIipCouponService
{
    /**
     * 通过ID查询券
     * 
     * @param couponId 券ID
     * @return 券信息，不存在时返回null
     */
    public IipCoupon selectIipCouponById(Long couponId);

    /**
     * 查询券列表（管理端，按名称/类型/状态筛选）
     * 
     * @param iipCoupon 查询条件
     * @return 券集合
     */
    public List<IipCoupon> selectIipCouponList(IipCoupon iipCoupon);

    /**
     * 查询积分商城券列表（上架且在兑换窗口内，按 sort、create_time desc）
     * 
     * @param now 当前时间
     * @param category 券品类，null 或空表示全部
     * @return 券集合
     */
    public List<IipCoupon> selectMallCouponList(Date now, String category);

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
     * 批量删除券
     * 
     * @param couponIds 需要删除的券ID
     * @return 结果
     */
    public int deleteIipCouponByIds(Long[] couponIds);
}
