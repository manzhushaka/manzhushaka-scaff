package com.manzhushaka.iip.application.exchange.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.iip.application.exchange.query.ExchangeQuery;
import com.manzhushaka.iip.application.exchange.result.ExchangeRecordResult;
import com.manzhushaka.iip.application.exchange.service.ExchangeAppService;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.service.IIipCouponRecordService;

/**
 * 兑换记录应用服务实现（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class ExchangeAppServiceImpl implements ExchangeAppService
{
    @Autowired
    private IIipCouponRecordService couponRecordService;

    /**
     * 查询兑换记录列表。
     *
     * @param query 查询条件
     * @return 兑换记录列表
     */
    @Override
    public List<ExchangeRecordResult> listExchangeRecords(ExchangeQuery query)
    {
        return couponRecordService.selectIipCouponRecordList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 查询兑换记录详情。
     *
     * @param recordId 记录ID
     * @return 兑换记录详情，不存在时返回 null
     */
    @Override
    public ExchangeRecordResult getExchangeRecord(Long recordId)
    {
        return toResult(couponRecordService.selectIipCouponRecordById(recordId));
    }

    /**
     * 作废未使用券并退回兑换积分。
     *
     * @param recordId 记录ID
     * @param operatorUsername 操作人账号
     * @param voidReason 作废原因
     */
    @Override
    @Transactional
    public void voidExchange(Long recordId, String operatorUsername, String voidReason)
    {
        couponRecordService.voidUnusedCoupon(recordId, operatorUsername, voidReason);
    }

    private IipCouponRecord toEntity(ExchangeQuery query)
    {
        IipCouponRecord record = new IipCouponRecord();
        if (query == null)
        {
            return record;
        }
        record.setCouponName(query.couponName());
        record.setMemberId(query.memberId());
        record.setStatus(query.status());
        record.setVerifyCode(query.verifyCode());
        if (query.beginTime() != null)
        {
            record.getParams().put("beginTime", query.beginTime());
        }
        if (query.endTime() != null)
        {
            record.getParams().put("endTime", query.endTime());
        }
        return record;
    }

    private ExchangeRecordResult toResult(IipCouponRecord record)
    {
        if (record == null)
        {
            return null;
        }
        return new ExchangeRecordResult(record.getRecordId(), record.getCouponId(), record.getCouponName(),
                record.getCouponType(), record.getMemberId(), record.getPointsCost(), record.getVerifyCode(),
                record.getStatus(), record.getExchangeTime(), record.getValidStartTime(), record.getValidEndTime(),
                record.getVerifyTime(), record.getVerifyMerchantId(), record.getVerifyBy(), record.getActivityId(),
                record.getVoidTime(), record.getVoidBy(), record.getVoidReason(), record.getRemark());
    }
}
