package com.manzhushaka.iip.application.coupon.service;

import java.util.List;
import com.manzhushaka.iip.application.coupon.result.CouponDetailResult;
import com.manzhushaka.iip.application.coupon.result.CouponMallItemResult;
import com.manzhushaka.iip.application.coupon.result.ExchangeResult;
import com.manzhushaka.iip.application.exchange.result.MyCouponResult;

/**
 * 小程序券应用服务（积分商城、券详情、兑换、我的券）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface MiniappCouponAppService
{
    /**
     * 查询积分商城券列表（上架且在兑换窗口内，按 sort、create_time desc）。
     *
     * @param category 券品类，null 或空表示全部
     * @return 商城券列表
     */
    List<CouponMallItemResult> listMallCoupons(String category);

    /**
     * 查询券详情（含当前用户已兑数量）。
     *
     * @param couponId 券ID
     * @param memberId 当前用户ID
     * @return 券详情
     */
    CouponDetailResult getCouponDetail(Long couponId, Long memberId);

    /**
     * 兑换券（限兑/库存/活动额度/积分扣减整体事务）。
     *
     * @param memberId 当前用户ID
     * @param couponId 券ID
     * @return 兑换成功的券实例（含核销码）
     */
    ExchangeResult exchange(Long memberId, Long couponId);

    /**
     * 查询我的券（查询前先将本人已过期未使用券置为已过期，按兑换时间倒序）。
     *
     * @param memberId 当前用户ID
     * @param status 状态（0未使用 1已使用 2已过期），null 或空表示全部
     * @return 我的券列表
     */
    List<MyCouponResult> listMyCoupons(Long memberId, String status);
}
