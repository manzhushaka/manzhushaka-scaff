package com.manzhushaka.iip.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.mapper.IipActivityCouponMapper;
import com.manzhushaka.iip.mapper.IipActivityMapper;
import com.manzhushaka.iip.mapper.IipCouponMapper;
import com.manzhushaka.iip.mapper.IipCouponRecordMapper;
import com.manzhushaka.iip.service.IIipPointsService;

/**
 * 券实例服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
@ExtendWith(MockitoExtension.class)
class IipCouponRecordServiceImplTest
{
    @Mock
    private IipCouponRecordMapper couponRecordMapper;

    @Mock
    private IipCouponMapper couponMapper;

    @Mock
    private IipActivityMapper activityMapper;

    @Mock
    private IipActivityCouponMapper activityCouponMapper;

    @Mock
    private IIipPointsService pointsService;

    @InjectMocks
    private IipCouponRecordServiceImpl service;

    /**
     * 作废未使用券会恢复库存、活动额度并退回积分。
     */
    @Test
    void voidUnusedCouponShouldRestoreResourcesAndRefundPoints()
    {
        IipCouponRecord record = record();
        when(couponRecordMapper.selectIipCouponRecordById(8L)).thenReturn(record);
        when(couponRecordMapper.voidUnusedAtomic(8L, "admin", "配置错误")).thenReturn(1);

        service.voidUnusedCoupon(8L, "admin", "配置错误");

        verify(couponMapper).restoreStock(2L);
        verify(activityCouponMapper).decrIssued(4L, 2L);
        verify(pointsService).refundConsumedPoints(eq(7L), eq(500), eq("coupon_void_refund"), eq("8"),
                any(), eq("管理员作废券退回积分：测试券"));
    }

    /**
     * 并发或重复作废命中状态守卫时不能重复退积分。
     */
    @Test
    void repeatedVoidShouldNotRefundAgain()
    {
        when(couponRecordMapper.selectIipCouponRecordById(8L)).thenReturn(record());
        when(couponRecordMapper.voidUnusedAtomic(8L, "admin", "重复")).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.voidUnusedCoupon(8L, "admin", "重复"));
        verify(pointsService, never()).refundConsumedPoints(any(), any(), any(), any(), any(), any());
    }

    private IipCouponRecord record()
    {
        IipCouponRecord record = new IipCouponRecord();
        record.setRecordId(8L);
        record.setCouponId(2L);
        record.setCouponName("测试券");
        record.setMemberId(7L);
        record.setPointsCost(500);
        record.setActivityId(4L);
        record.setStatus("0");
        return record;
    }
}
