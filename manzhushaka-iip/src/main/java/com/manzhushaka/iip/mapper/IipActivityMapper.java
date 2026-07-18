package com.manzhushaka.iip.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipActivity;

/**
 * 活动 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipActivityMapper
{
    /**
     * 通过ID查询活动
     * 
     * @param activityId 活动ID
     * @return 活动信息
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
     * 新增活动
     * 
     * @param iipActivity 活动信息
     * @return 结果
     */
    public int insertIipActivity(IipActivity iipActivity);

    /**
     * 修改活动
     * 
     * @param iipActivity 活动信息
     * @return 结果
     */
    public int updateIipActivity(IipActivity iipActivity);

    /**
     * 通过ID删除活动
     * 
     * @param activityId 活动ID
     * @return 结果
     */
    public int deleteIipActivityById(Long activityId);

    /**
     * 批量删除活动
     * 
     * @param activityIds 需要删除的活动ID
     * @return 结果
     */
    public int deleteIipActivityByIds(Long[] activityIds);

    /**
     * 查询当前生效活动（启用且在时间窗内，取优先级最高的一个）
     *
     * @param now 当前时间
     * @return 当前活动信息，不存在时返回null
     */
    public IipActivity selectCurrentActivity(@Param("now") Date now);

    /**
     * 查询全部生效活动（启用且在时间窗内，按优先级倒序、开始时间倒序）
     *
     * @param now 当前时间
     * @return 生效活动集合，无生效活动时返回空集合
     */
    public List<IipActivity> selectActiveActivities(@Param("now") Date now);

    /**
     * 查询指定编号前缀下最大的活动编号（用于活动编号序号生成）
     *
     * @param prefix 编号前缀（A+yyyyMM）
     * @return 最大活动编号，不存在时返回null
     */
    public String selectMaxActivityNo(@Param("prefix") String prefix);

    /**
     * 统计指定活动编号的出现次数（活动编号查重）
     *
     * @param activityNo 活动编号
     * @return 出现次数
     */
    public int countByActivityNo(String activityNo);
}
