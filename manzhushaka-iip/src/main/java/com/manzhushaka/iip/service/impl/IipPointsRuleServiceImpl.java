package com.manzhushaka.iip.service.impl;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.domain.IipActivityMerchant;
import com.manzhushaka.iip.domain.IipPointsRule;
import com.manzhushaka.iip.mapper.IipActivityMerchantMapper;
import com.manzhushaka.iip.mapper.IipPointsMonthlyQuotaMapper;
import com.manzhushaka.iip.mapper.IipPointsRuleMapper;
import com.manzhushaka.iip.service.IIipPointsRuleService;

/**
 * 活动积分规则服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
@Service
public class IipPointsRuleServiceImpl implements IIipPointsRuleService
{
    private static final int UNLIMITED = -1;
    private static final String SCOPE_ALL = "all";
    private static final String SCOPE_WHITELIST = "whitelist";
    private static final Set<String> MERCHANT_SCOPES = Set.of(SCOPE_ALL, SCOPE_WHITELIST);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private IipPointsRuleMapper pointsRuleMapper;

    @Autowired
    private IipPointsMonthlyQuotaMapper monthlyQuotaMapper;

    @Autowired
    private IipActivityMerchantMapper activityMerchantMapper;

    @Override
    public IipPointsRule getRule(Long activityId)
    {
        IipPointsRule rule = pointsRuleMapper.selectByActivityId(activityId);
        if (rule != null)
        {
            return rule;
        }
        IipPointsRule defaultRule = new IipPointsRule();
        defaultRule.setActivityId(activityId);
        defaultRule.setSingleInvoiceCap(UNLIMITED);
        defaultRule.setMonthlyMemberCap(UNLIMITED);
        defaultRule.setMerchantScope(SCOPE_ALL);
        return defaultRule;
    }

    @Override
    @Transactional
    public int saveRule(IipPointsRule rule)
    {
        validateRule(rule);
        return pointsRuleMapper.upsert(rule);
    }

    @Override
    public boolean isMerchantEligible(IipPointsRule rule, Long merchantId)
    {
        if (!SCOPE_WHITELIST.equals(rule.getMerchantScope()))
        {
            return true;
        }
        if (merchantId == null)
        {
            return false;
        }
        IipActivityMerchant query = new IipActivityMerchant();
        query.setActivityId(rule.getActivityId());
        query.setMerchantId(merchantId);
        query.setStatus("0");
        return !activityMerchantMapper.selectIipActivityMerchantList(query).isEmpty();
    }

    @Override
    @Transactional
    public int reserveMonthlyPoints(IipPointsRule rule, Long memberId, int requestedPoints, Date now)
    {
        Integer monthlyCap = rule.getMonthlyMemberCap();
        if (requestedPoints <= 0 || monthlyCap == null || monthlyCap == UNLIMITED)
        {
            return Math.max(requestedPoints, 0);
        }
        if (rule.getRuleId() == null)
        {
            throw new ServiceException("活动积分规则缺少规则ID");
        }
        String quotaMonth = YearMonth.from(now.toInstant().atZone(ZoneId.systemDefault())).format(MONTH_FORMATTER);
        monthlyQuotaMapper.initialize(rule.getRuleId(), memberId, quotaMonth);
        Integer awarded = monthlyQuotaMapper.selectAwardedPointsForUpdate(rule.getRuleId(), memberId, quotaMonth);
        int remaining = Math.max(monthlyCap - (awarded == null ? 0 : awarded), 0);
        int actualPoints = Math.min(requestedPoints, remaining);
        if (actualPoints > 0)
        {
            monthlyQuotaMapper.increase(rule.getRuleId(), memberId, quotaMonth, actualPoints);
        }
        return actualPoints;
    }

    @Override
    @Transactional
    public void deleteByActivityIds(Long[] activityIds)
    {
        monthlyQuotaMapper.deleteByActivityIds(activityIds);
        pointsRuleMapper.deleteByActivityIds(activityIds);
    }

    /**
     * 校验积分规则完整性。
     *
     * @param rule 积分规则
     */
    private void validateRule(IipPointsRule rule)
    {
        if (rule == null || rule.getActivityId() == null)
        {
            throw new ServiceException("活动积分规则缺少活动ID");
        }
        validateCap(rule.getSingleInvoiceCap(), "单张积分上限");
        validateCap(rule.getMonthlyMemberCap(), "每月积分上限");
        if (!MERCHANT_SCOPES.contains(rule.getMerchantScope()))
        {
            throw new ServiceException("商户积分范围不合法");
        }
    }

    /**
     * 校验上限只能为-1或正整数。
     *
     * @param cap 上限
     * @param fieldName 字段名称
     */
    private void validateCap(Integer cap, String fieldName)
    {
        if (cap == null || cap == 0 || cap < UNLIMITED)
        {
            throw new ServiceException(fieldName + "只能为-1或正整数");
        }
    }
}
