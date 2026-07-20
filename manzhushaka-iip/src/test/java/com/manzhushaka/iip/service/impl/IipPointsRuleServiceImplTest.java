package com.manzhushaka.iip.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.manzhushaka.iip.domain.IipActivityMerchant;
import com.manzhushaka.iip.domain.IipPointsRule;
import com.manzhushaka.iip.mapper.IipActivityMerchantMapper;
import com.manzhushaka.iip.mapper.IipPointsMonthlyQuotaMapper;
import com.manzhushaka.iip.mapper.IipPointsRuleMapper;

/**
 * 活动积分规则服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
@ExtendWith(MockitoExtension.class)
class IipPointsRuleServiceImplTest
{
    @Mock
    private IipPointsRuleMapper pointsRuleMapper;

    @Mock
    private IipPointsMonthlyQuotaMapper monthlyQuotaMapper;

    @Mock
    private IipActivityMerchantMapper activityMerchantMapper;

    @InjectMocks
    private IipPointsRuleServiceImpl service;

    /**
     * 月度剩余额度不足时只发放剩余额度。
     */
    @Test
    void reserveMonthlyPointsShouldApplyRemainingCap()
    {
        IipPointsRule rule = rule("all");
        rule.setMonthlyMemberCap(100);
        when(monthlyQuotaMapper.selectAwardedPointsForUpdate(eq(9L), eq(7L), any())).thenReturn(80);

        int actual = service.reserveMonthlyPoints(rule, 7L, 50, new Date());

        assertEquals(20, actual);
        verify(monthlyQuotaMapper).increase(eq(9L), eq(7L), any(), eq(20));
    }

    /**
     * 白名单规则只允许已启用的活动商户。
     */
    @Test
    void whitelistShouldRequireConfiguredMerchant()
    {
        IipPointsRule rule = rule("whitelist");
        when(activityMerchantMapper.selectIipActivityMerchantList(any())).thenReturn(List.of());
        assertFalse(service.isMerchantEligible(rule, 3L));

        when(activityMerchantMapper.selectIipActivityMerchantList(any()))
                .thenReturn(List.of(new IipActivityMerchant()));
        assertTrue(service.isMerchantEligible(rule, 3L));
    }

    private IipPointsRule rule(String merchantScope)
    {
        IipPointsRule rule = new IipPointsRule();
        rule.setRuleId(9L);
        rule.setActivityId(2L);
        rule.setSingleInvoiceCap(-1);
        rule.setMonthlyMemberCap(-1);
        rule.setMerchantScope(merchantScope);
        return rule;
    }
}
