package com.manzhushaka.iip.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 积分月度额度数据层。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
public interface IipPointsMonthlyQuotaMapper
{
    /**
     * 初始化月度额度行，已存在时保持原值。
     *
     * @param ruleId 规则ID
     * @param memberId 用户ID
     * @param quotaMonth 额度月份 yyyy-MM
     * @return 影响行数
     */
    int initialize(@Param("ruleId") Long ruleId, @Param("memberId") Long memberId,
            @Param("quotaMonth") String quotaMonth);

    /**
     * 锁定并读取已发积分。
     *
     * @param ruleId 规则ID
     * @param memberId 用户ID
     * @param quotaMonth 额度月份 yyyy-MM
     * @return 已发积分
     */
    Integer selectAwardedPointsForUpdate(@Param("ruleId") Long ruleId, @Param("memberId") Long memberId,
            @Param("quotaMonth") String quotaMonth);

    /**
     * 累加月度已发积分。
     *
     * @param ruleId 规则ID
     * @param memberId 用户ID
     * @param quotaMonth 额度月份 yyyy-MM
     * @param points 本次积分
     * @return 影响行数
     */
    int increase(@Param("ruleId") Long ruleId, @Param("memberId") Long memberId,
            @Param("quotaMonth") String quotaMonth, @Param("points") Integer points);

    /**
     * 删除指定活动规则对应的月度额度记录。
     *
     * @param activityIds 活动ID集合
     * @return 影响行数
     */
    int deleteByActivityIds(@Param("activityIds") Long[] activityIds);
}
