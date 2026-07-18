package com.manzhushaka.iip.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipActivityCoupon;

/**
 * 活动券配置 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipActivityCouponMapper
{
    /**
     * 通过ID查询活动券配置
     * 
     * @param id 主键ID
     * @return 活动券配置信息
     */
    public IipActivityCoupon selectIipActivityCouponById(Long id);

    /**
     * 查询活动券配置列表
     * 
     * @param iipActivityCoupon 查询条件
     * @return 活动券配置集合
     */
    public List<IipActivityCoupon> selectIipActivityCouponList(IipActivityCoupon iipActivityCoupon);

    /**
     * 新增活动券配置
     * 
     * @param iipActivityCoupon 活动券配置信息
     * @return 结果
     */
    public int insertIipActivityCoupon(IipActivityCoupon iipActivityCoupon);

    /**
     * 修改活动券配置
     * 
     * @param iipActivityCoupon 活动券配置信息
     * @return 结果
     */
    public int updateIipActivityCoupon(IipActivityCoupon iipActivityCoupon);

    /**
     * 通过ID删除活动券配置
     * 
     * @param id 主键ID
     * @return 结果
     */
    public int deleteIipActivityCouponById(Long id);

    /**
     * 批量删除活动券配置
     * 
     * @param ids 需要删除的主键ID
     * @return 结果
     */
    public int deleteIipActivityCouponByIds(Long[] ids);

    /**
     * 按活动ID查询活动券配置列表
     * 
     * @param activityId 活动ID
     * @return 活动券配置集合
     */
    public List<IipActivityCoupon> selectByActivityId(Long activityId);

    /**
     * 按券ID查询活动券配置列表（用于兑换归因：查找配置该券的全部活动）
     *
     * @param couponId 券ID
     * @return 活动券配置集合
     */
    public List<IipActivityCoupon> selectByCouponId(Long couponId);

    /**
     * 原子累加已发行数量（issue_limit 为 -1 或未达到上限时加 1）
     *
     * @param id 主键ID
     * @return 影响行数，0 表示已达发行上限
     */
    public int incrIssued(Long id);

    /**
     * 按活动ID删除全部活动券配置（删除活动级联用）
     *
     * @param activityId 活动ID
     * @return 结果
     */
    public int deleteByActivityId(Long activityId);

    /**
     * 统计活动已配置券数量
     *
     * @param activityId 活动ID
     * @return 已配置券数量
     */
    public int countByActivityId(Long activityId);

    /**
     * 汇总活动已配置券的发行上限合计（issue_limit 为 -1 按 0 计）
     *
     * @param activityId 活动ID
     * @param excludeId 需要排除的配置ID（修改场景排除自身），可为null
     * @return 发行上限合计
     */
    public int sumIssueLimitByActivityId(@Param("activityId") Long activityId, @Param("excludeId") Long excludeId);

    /**
     * 按活动ID查询活动券配置列表（join iip_coupon 携带券名称/积分价/封面/库存）
     *
     * @param activityId 活动ID
     * @return 活动券配置集合（含券展示字段）
     */
    public List<IipActivityCoupon> selectCouponJoinList(Long activityId);
}
