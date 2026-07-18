package com.manzhushaka.iip.mapper;

import java.util.List;
import com.manzhushaka.iip.domain.IipActivityMerchant;

/**
 * 活动商户关联 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipActivityMerchantMapper
{
    /**
     * 通过ID查询活动商户关联
     * 
     * @param id 主键ID
     * @return 活动商户关联信息
     */
    public IipActivityMerchant selectIipActivityMerchantById(Long id);

    /**
     * 查询活动商户关联列表
     * 
     * @param iipActivityMerchant 查询条件
     * @return 活动商户关联集合
     */
    public List<IipActivityMerchant> selectIipActivityMerchantList(IipActivityMerchant iipActivityMerchant);

    /**
     * 新增活动商户关联
     * 
     * @param iipActivityMerchant 活动商户关联信息
     * @return 结果
     */
    public int insertIipActivityMerchant(IipActivityMerchant iipActivityMerchant);

    /**
     * 修改活动商户关联
     * 
     * @param iipActivityMerchant 活动商户关联信息
     * @return 结果
     */
    public int updateIipActivityMerchant(IipActivityMerchant iipActivityMerchant);

    /**
     * 通过ID删除活动商户关联
     * 
     * @param id 主键ID
     * @return 结果
     */
    public int deleteIipActivityMerchantById(Long id);

    /**
     * 批量删除活动商户关联
     * 
     * @param ids 需要删除的主键ID
     * @return 结果
     */
    public int deleteIipActivityMerchantByIds(Long[] ids);

    /**
     * 按活动ID查询参与商户关联列表
     *
     * @param activityId 活动ID
     * @return 活动商户关联集合
     */
    public List<IipActivityMerchant> selectByActivityId(Long activityId);

    /**
     * 按活动ID删除全部活动商户关联（删除活动级联用）
     *
     * @param activityId 活动ID
     * @return 结果
     */
    public int deleteByActivityId(Long activityId);

    /**
     * 统计活动已配置商户数量
     *
     * @param activityId 活动ID
     * @return 已配置商户数量
     */
    public int countByActivityId(Long activityId);

    /**
     * 按活动ID查询参与商户关联列表（join iip_merchant 携带商户编号/名称/类别/状态）
     *
     * @param activityId 活动ID
     * @return 活动商户关联集合（含商户展示字段）
     */
    public List<IipActivityMerchant> selectMerchantJoinList(Long activityId);
}
