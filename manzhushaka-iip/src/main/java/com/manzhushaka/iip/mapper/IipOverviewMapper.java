package com.manzhushaka.iip.mapper;

import java.util.List;
import java.util.Map;

/**
 * 数据概览统计 数据层（只读统计 SQL，直连 iip_* 各表）
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipOverviewMapper
{
    /**
     * 统计小程序用户总数
     * 
     * @return 用户总数
     */
    public Long countMembers();

    /**
     * 统计商户总数
     * 
     * @return 商户总数
     */
    public Long countMerchants();

    /**
     * 统计待审核商户数（status = '2'）
     * 
     * @return 待审核商户数
     */
    public Long countPendingMerchants();

    /**
     * 按状态分组统计发票数量
     * 
     * @return 每行含 status 与 cnt 两个键的统计列表
     */
    public List<Map<String, Object>> countInvoicesByStatus();

    /**
     * 统计待审核发票数（status = '0'）
     * 
     * @return 待审核发票数
     */
    public Long countPendingInvoices();

    /**
     * 统计累计发放积分（iip_points_record 中 change_type = 'earn' 的 points 之和，无记录返回 0）
     * 
     * @return 累计发放积分
     */
    public Long sumPointsIssued();

    /**
     * 统计累计消耗积分（iip_points_record 中 change_type = 'consume' 的 points 之和，无记录返回 0）
     * 
     * @return 累计消耗积分
     */
    public Long sumPointsConsumed();

    /**
     * 统计券兑换记录总数
     * 
     * @return 兑换记录总数
     */
    public Long countCouponExchanges();

    /**
     * 统计已核销券数（status = '1'）
     * 
     * @return 已核销券数
     */
    public Long countVerifiedCoupons();

    /**
     * 统计当前生效活动数（status = '0' 且当前时间在起止时间内）
     * 
     * @return 生效活动数
     */
    public Long countActiveActivities();

    /**
     * 近 7 日发票上传趋势（按自然日分组，含今天）
     * 
     * @return 每行含 day 与 cnt 两个键的趋势列表，按日期升序
     */
    public List<Map<String, Object>> invoiceTrend();

    /**
     * 近 7 日积分发放趋势（earn 流水按自然日求和，含今天）
     * 
     * @return 每行含 day 与 cnt 两个键的趋势列表，按日期升序
     */
    public List<Map<String, Object>> pointsTrend();

    /**
     * 近 7 日券兑换趋势（按自然日分组，含今天）
     * 
     * @return 每行含 day 与 cnt 两个键的趋势列表，按日期升序
     */
    public List<Map<String, Object>> exchangeTrend();
}
