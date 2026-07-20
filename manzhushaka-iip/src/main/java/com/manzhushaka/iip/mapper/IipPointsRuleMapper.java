package com.manzhushaka.iip.mapper;

import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipPointsRule;

/**
 * 活动积分规则数据层。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
public interface IipPointsRuleMapper
{
    /**
     * 按活动查询积分规则。
     *
     * @param activityId 活动ID
     * @return 积分规则，不存在时返回null
     */
    IipPointsRule selectByActivityId(Long activityId);

    /**
     * 新增或更新活动积分规则。
     *
     * @param rule 积分规则
     * @return 影响行数
     */
    int upsert(IipPointsRule rule);

    /**
     * 删除活动积分规则及对应月度额度记录。
     *
     * @param activityIds 活动ID集合
     * @return 删除规则数
     */
    int deleteByActivityIds(@Param("activityIds") Long[] activityIds);
}
