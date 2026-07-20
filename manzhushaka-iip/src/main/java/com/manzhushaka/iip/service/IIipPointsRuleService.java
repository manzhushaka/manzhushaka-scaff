package com.manzhushaka.iip.service;

import java.util.Date;
import com.manzhushaka.iip.domain.IipPointsRule;

/**
 * 活动积分规则服务层。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
public interface IIipPointsRuleService
{
    /**
     * 查询活动积分规则，未配置时返回兼容默认规则。
     *
     * @param activityId 活动ID
     * @return 积分规则
     */
    IipPointsRule getRule(Long activityId);

    /**
     * 保存活动积分规则。
     *
     * @param rule 积分规则
     * @return 影响行数
     */
    int saveRule(IipPointsRule rule);

    /**
     * 判断商户是否符合活动积分范围。
     *
     * @param rule 积分规则
     * @param merchantId 商户ID
     * @return true符合
     */
    boolean isMerchantEligible(IipPointsRule rule, Long merchantId);

    /**
     * 按活动规则预留本月积分额度；事务回滚时额度同步回滚。
     *
     * @param rule 积分规则
     * @param memberId 用户ID
     * @param requestedPoints 单张封顶后的积分
     * @param now 当前时间
     * @return 月度封顶后的实际积分
     */
    int reserveMonthlyPoints(IipPointsRule rule, Long memberId, int requestedPoints, Date now);

    /**
     * 删除活动规则和额度数据。
     *
     * @param activityIds 活动ID集合
     */
    void deleteByActivityIds(Long[] activityIds);
}
