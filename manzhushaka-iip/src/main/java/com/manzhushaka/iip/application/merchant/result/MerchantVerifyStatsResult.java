package com.manzhushaka.iip.application.merchant.result;

/**
 * 小程序商户核销工作台统计。
 *
 * 统计 iip_coupon_record 表中本商户已核销（status='1'）的记录；
 * 今日口径为 verify_time 不早于当日 00:00。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MerchantVerifyStatsResult
{
    /** 今日核销笔数 */
    private Long todayCount;

    /** 今日核销消耗积分合计 */
    private Long todayPoints;

    /** 累计核销笔数 */
    private Long totalCount;

    /** 累计核销消耗积分合计 */
    private Long totalPoints;

    /**
     * 获取今日核销笔数
     *
     * @return 今日核销笔数
     */
    public Long getTodayCount()
    {
        return todayCount;
    }

    /**
     * 设置今日核销笔数
     *
     * @param todayCount 今日核销笔数
     */
    public void setTodayCount(Long todayCount)
    {
        this.todayCount = todayCount;
    }

    /**
     * 获取今日核销消耗积分合计
     *
     * @return 今日核销消耗积分合计
     */
    public Long getTodayPoints()
    {
        return todayPoints;
    }

    /**
     * 设置今日核销消耗积分合计
     *
     * @param todayPoints 今日核销消耗积分合计
     */
    public void setTodayPoints(Long todayPoints)
    {
        this.todayPoints = todayPoints;
    }

    /**
     * 获取累计核销笔数
     *
     * @return 累计核销笔数
     */
    public Long getTotalCount()
    {
        return totalCount;
    }

    /**
     * 设置累计核销笔数
     *
     * @param totalCount 累计核销笔数
     */
    public void setTotalCount(Long totalCount)
    {
        this.totalCount = totalCount;
    }

    /**
     * 获取累计核销消耗积分合计
     *
     * @return 累计核销消耗积分合计
     */
    public Long getTotalPoints()
    {
        return totalPoints;
    }

    /**
     * 设置累计核销消耗积分合计
     *
     * @param totalPoints 累计核销消耗积分合计
     */
    public void setTotalPoints(Long totalPoints)
    {
        this.totalPoints = totalPoints;
    }

    @Override
    public String toString()
    {
        return "MerchantVerifyStatsResult[todayCount=" + todayCount + ", todayPoints=" + todayPoints
                + ", totalCount=" + totalCount + ", totalPoints=" + totalPoints + "]";
    }
}
