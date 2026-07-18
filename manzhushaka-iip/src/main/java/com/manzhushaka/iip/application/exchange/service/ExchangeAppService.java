package com.manzhushaka.iip.application.exchange.service;

import java.util.List;
import com.manzhushaka.iip.application.exchange.query.ExchangeQuery;
import com.manzhushaka.iip.application.exchange.result.ExchangeRecordResult;

/**
 * 兑换记录应用服务（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface ExchangeAppService
{
    /**
     * 查询兑换记录列表。
     *
     * @param query 查询条件
     * @return 兑换记录列表
     */
    List<ExchangeRecordResult> listExchangeRecords(ExchangeQuery query);

    /**
     * 查询兑换记录详情。
     *
     * @param recordId 记录ID
     * @return 兑换记录详情，不存在时返回 null
     */
    ExchangeRecordResult getExchangeRecord(Long recordId);
}
