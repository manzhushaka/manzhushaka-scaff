package com.manzhushaka.iip.service;

import java.util.Date;
import java.util.List;
import com.manzhushaka.iip.domain.IipActivity;
import com.manzhushaka.iip.domain.IipActivityCoupon;
import com.manzhushaka.iip.domain.IipActivityMerchant;

/**
 * 活动 服务层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IIipActivityService
{
    /**
     * 通过ID查询活动
     * 
     * @param activityId 活动ID
     * @return 活动信息，不存在时返回null
     */
    public IipActivity selectIipActivityById(Long activityId);

    /**
     * 查询活动列表
     * 
     * @param iipActivity 查询条件
     * @return 活动集合
     */
    public List<IipActivity> selectIipActivityList(IipActivity iipActivity);

    /**
     * 新增活动（服务端生成活动编号 A+yyyyMM+4位序号，校验起止时间与积分比例）
     * 
     * @param iipActivity 活动信息
     * @return 结果
     * @throws com.manzhushaka.common.exception.ServiceException 校验失败或编号生成失败时抛出
     */
    public int insertIipActivity(IipActivity iipActivity);

    /**
     * 修改活动（校验活动存在、起止时间与积分比例，活动编号不可变更）
     * 
     * @param iipActivity 活动信息
     * @return 结果
     * @throws com.manzhushaka.common.exception.ServiceException 活动不存在或校验失败时抛出
     */
    public int updateIipActivity(IipActivity iipActivity);

    /**
     * 批量删除活动（启用中且在时间窗内的活动禁止删除；级联删除活动商户与活动券配置）
     * 
     * @param activityIds 需要删除的活动ID
     * @throws com.manzhushaka.common.exception.ServiceException 活动进行中时抛出
     */
    public void deleteIipActivityByIds(Long[] activityIds);

    /**
     * 查询当前生效活动（启用且在时间窗内，取优先级最高的一个）
     * 
     * @param now 当前时间
     * @return 当前活动信息，不存在时返回null
     */
    public IipActivity selectCurrentActivity(Date now);

    /**
     * 查询全部生效活动（启用且在时间窗内，按优先级倒序、开始时间倒序）
     * 
     * @param now 当前时间
     * @return 生效活动集合，无生效活动时返回空集合
     */
    public List<IipActivity> selectActiveActivities(Date now);

    /**
     * 按活动ID查询参与商户关联列表（join iip_merchant 携带商户编号/名称/类别/状态）
     * 
     * @param activityId 活动ID
     * @return 活动商户关联集合（含商户展示字段）
     */
    public List<IipActivityMerchant> selectMerchantJoinList(Long activityId);

    /**
     * 统计活动已配置商户数量
     * 
     * @param activityId 活动ID
     * @return 已配置商户数量
     */
    public int countMerchantByActivityId(Long activityId);

    /**
     * 新增活动商户配置（校验活动存在、商户存在且正常、参与商户数上限、唯一键冲突）
     * 
     * @param activityMerchant 活动商户关联信息
     * @return 结果
     * @throws com.manzhushaka.common.exception.ServiceException 校验失败或商户已配置时抛出
     */
    public int insertActivityMerchant(IipActivityMerchant activityMerchant);

    /**
     * 通过ID删除活动商户配置
     * 
     * @param id 主键ID
     * @return 结果
     */
    public int deleteActivityMerchantById(Long id);

    /**
     * 通过ID查询活动券配置
     * 
     * @param id 主键ID
     * @return 活动券配置信息，不存在时返回null
     */
    public IipActivityCoupon selectActivityCouponById(Long id);

    /**
     * 按活动ID查询活动券配置列表（join iip_coupon 携带券名称/积分价/封面/库存）
     * 
     * @param activityId 活动ID
     * @return 活动券配置集合（含券展示字段）
     */
    public List<IipActivityCoupon> selectCouponJoinList(Long activityId);

    /**
     * 统计活动已配置券数量
     * 
     * @param activityId 活动ID
     * @return 已配置券数量
     */
    public int countCouponByActivityId(Long activityId);

    /**
     * 新增活动券配置（校验活动存在、券存在、活动发券额度、券总库存、唯一键冲突）
     * 
     * @param activityCoupon 活动券配置信息
     * @return 结果
     * @throws com.manzhushaka.common.exception.ServiceException 校验失败或券已配置时抛出
     */
    public int insertActivityCoupon(IipActivityCoupon activityCoupon);

    /**
     * 修改活动券配置发行上限（同新增校验，额度合计排除自身）
     * 
     * @param activityCoupon 活动券配置信息（仅需主键ID与发行上限）
     * @return 结果
     * @throws com.manzhushaka.common.exception.ServiceException 配置不存在或校验失败时抛出
     */
    public int updateActivityCouponIssueLimit(IipActivityCoupon activityCoupon);

    /**
     * 通过ID删除活动券配置
     * 
     * @param id 主键ID
     * @return 结果
     */
    public int deleteActivityCouponById(Long id);
}
