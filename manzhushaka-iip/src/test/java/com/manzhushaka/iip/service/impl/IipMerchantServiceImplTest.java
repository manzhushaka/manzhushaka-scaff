package com.manzhushaka.iip.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.domain.IipMerchant;
import com.manzhushaka.iip.mapper.IipCouponMapper;
import com.manzhushaka.iip.mapper.IipCouponRecordMapper;
import com.manzhushaka.iip.mapper.IipMerchantMapper;
import com.manzhushaka.iip.mapper.IipMerchantVerifyMapper;
import com.manzhushaka.iip.service.IIipMerchantService;

/**
 * 商户核销服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-20
 */
@ExtendWith(MockitoExtension.class)
class IipMerchantServiceImplTest
{
    @Mock
    private IipMerchantMapper merchantMapper;

    @Mock
    private IipMerchantVerifyMapper merchantVerifyMapper;

    @Mock
    private IipCouponRecordMapper couponRecordMapper;

    @Mock
    private IipCouponMapper couponMapper;

    @Mock
    private IIipMerchantService selfProxy;

    @InjectMocks
    private IipMerchantServiceImpl service;

    /**
     * 尚未到有效期开始时间的券不能预检通过。
     */
    @Test
    void previewShouldRejectCouponBeforeValidStart()
    {
        when(merchantMapper.selectByMemberId(7L)).thenReturn(normalMerchant());
        IipCouponRecord record = unusedRecord();
        record.setValidStartTime(new Date(System.currentTimeMillis() + 60_000L));
        when(couponRecordMapper.selectByVerifyCode("CODE")).thenReturn(record);

        assertThrows(ServiceException.class, () -> service.previewCoupon(7L, "CODE"));
        verify(merchantVerifyMapper, never()).verifyCouponRecordAtomic(record.getRecordId(), 3L, "operator");
    }

    /**
     * 已过期券预检会先触发过期置位。
     */
    @Test
    void previewShouldMarkExpiredCoupon()
    {
        when(merchantMapper.selectByMemberId(7L)).thenReturn(normalMerchant());
        IipCouponRecord record = unusedRecord();
        record.setValidEndTime(new Date(System.currentTimeMillis() - 60_000L));
        when(couponRecordMapper.selectByVerifyCode("CODE")).thenReturn(record);

        assertThrows(ServiceException.class, () -> service.previewCoupon(7L, "CODE"));
        verify(selfProxy).markCouponRecordExpired(8L);
    }

    private IipMerchant normalMerchant()
    {
        IipMerchant merchant = new IipMerchant();
        merchant.setMerchantId(3L);
        merchant.setStatus("0");
        return merchant;
    }

    private IipCouponRecord unusedRecord()
    {
        IipCouponRecord record = new IipCouponRecord();
        record.setRecordId(8L);
        record.setCouponId(2L);
        record.setStatus("0");
        return record;
    }
}
