package com.manzhushaka.iip.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.manzhushaka.iip.domain.IipCoupon;
import com.manzhushaka.iip.mapper.IipCouponMapper;
import com.manzhushaka.iip.service.IIipCouponService;

/**
 * 券定义 服务层实现
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipCouponServiceImpl implements IIipCouponService
{
    @Autowired
    private IipCouponMapper couponMapper;

    /**
     * 通过ID查询券
     * 
     * @param couponId 券ID
     * @return 券信息，不存在时返回null
     */
    @Override
    public IipCoupon selectIipCouponById(Long couponId)
    {
        return couponMapper.selectIipCouponById(couponId);
    }

    /**
     * 查询券列表（管理端，按名称/类型/状态筛选）
     * 
     * @param iipCoupon 查询条件
     * @return 券集合
     */
    @Override
    public List<IipCoupon> selectIipCouponList(IipCoupon iipCoupon)
    {
        return couponMapper.selectIipCouponList(iipCoupon);
    }

    /**
     * 查询积分商城券列表（上架且在兑换窗口内，按 sort、create_time desc）
     * 
     * @param now 当前时间
     * @param category 券品类，null 或空表示全部
     * @return 券集合
     */
    @Override
    public List<IipCoupon> selectMallCouponList(Date now, String category)
    {
        return couponMapper.selectMallCouponList(now, category);
    }

    /**
     * 新增券
     * 
     * @param iipCoupon 券信息
     * @return 结果
     */
    @Override
    public int insertIipCoupon(IipCoupon iipCoupon)
    {
        return couponMapper.insertIipCoupon(iipCoupon);
    }

    /**
     * 修改券
     * 
     * @param iipCoupon 券信息
     * @return 结果
     */
    @Override
    public int updateIipCoupon(IipCoupon iipCoupon)
    {
        return couponMapper.updateIipCoupon(iipCoupon);
    }

    /**
     * 批量删除券
     * 
     * @param couponIds 需要删除的券ID
     * @return 结果
     */
    @Override
    public int deleteIipCouponByIds(Long[] couponIds)
    {
        return couponMapper.deleteIipCouponByIds(couponIds);
    }
}
