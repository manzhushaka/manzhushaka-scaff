package com.manzhushaka.iip.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.manzhushaka.iip.domain.IipActivity;
import com.manzhushaka.iip.domain.IipInvoice;
import com.manzhushaka.iip.domain.IipPointsRule;
import com.manzhushaka.iip.mapper.IipActivityMapper;
import com.manzhushaka.iip.mapper.IipInvoiceMapper;
import com.manzhushaka.iip.mapper.IipMerchantMapper;
import com.manzhushaka.iip.service.IIipPointsRuleService;
import com.manzhushaka.iip.service.IIipPointsService;

/**
 * 发票积分审核服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
@ExtendWith(MockitoExtension.class)
class IipInvoiceServiceImplTest
{
    @Mock
    private IipInvoiceMapper invoiceMapper;

    @Mock
    private IipActivityMapper activityMapper;

    @Mock
    private IipMerchantMapper merchantMapper;

    @Mock
    private IIipPointsService pointsService;

    @Mock
    private IIipPointsRuleService pointsRuleService;

    @InjectMocks
    private IipInvoiceServiceImpl service;

    /**
     * 发票积分按活动比例计算后应用单张上限，并保存计算快照。
     */
    @Test
    void auditShouldApplySingleInvoiceCapAndSaveSnapshot()
    {
        IipInvoice invoice = new IipInvoice();
        invoice.setInvoiceId(6L);
        invoice.setMemberId(7L);
        invoice.setAmount(new BigDecimal("100.00"));
        invoice.setStatus("0");
        when(invoiceMapper.selectIipInvoiceById(6L)).thenReturn(invoice);

        IipActivity activity = new IipActivity();
        activity.setActivityId(4L);
        activity.setPointsRatio(new BigDecimal("2.00"));
        when(activityMapper.selectActiveActivities(any())).thenReturn(List.of(activity));

        IipPointsRule rule = new IipPointsRule();
        rule.setRuleId(9L);
        rule.setActivityId(4L);
        rule.setSingleInvoiceCap(150);
        rule.setMonthlyMemberCap(-1);
        rule.setMerchantScope("all");
        when(pointsRuleService.getRule(4L)).thenReturn(rule);
        when(pointsRuleService.isMerchantEligible(rule, null)).thenReturn(true);
        when(pointsRuleService.reserveMonthlyPoints(eq(rule), eq(7L), eq(150), any())).thenReturn(150);
        when(invoiceMapper.updateAuditStatus(any())).thenReturn(1);

        service.auditInvoice(6L, true, null, "admin");

        ArgumentCaptor<IipInvoice> updateCaptor = ArgumentCaptor.forClass(IipInvoice.class);
        verify(invoiceMapper).updateAuditStatus(updateCaptor.capture());
        IipInvoice update = updateCaptor.getValue();
        assertEquals(150, update.getPoints());
        assertEquals(9L, update.getPointsRuleId());
        assertEquals(new BigDecimal("2.00"), update.getPointsRatioSnapshot());
        assertTrue(update.getPointsRuleSnapshot().contains("单张上限=150"));
        verify(pointsService).awardPoints(eq(7L), eq(150), eq("invoice_audit"), eq("6"), any(),
                eq("发票审核通过"));
    }
}
