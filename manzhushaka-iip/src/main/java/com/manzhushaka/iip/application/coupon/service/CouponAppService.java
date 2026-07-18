package com.manzhushaka.iip.application.coupon.service;

import java.util.List;
import com.manzhushaka.iip.application.coupon.command.SaveCouponCommand;
import com.manzhushaka.iip.application.coupon.query.CouponQuery;
import com.manzhushaka.iip.application.coupon.result.CouponResult;

/**
 * 券定义应用服务（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface CouponAppService
{
    /**
     * 查询券列表。
     *
     * @param query 查询条件
     * @return 券列表
     */
    List<CouponResult> listCoupons(CouponQuery query);

    /**
     * 查询券详情。
     *
     * @param couponId 券ID
     * @return 券详情，不存在时返回 null
     */
    CouponResult getCoupon(Long couponId);

    /**
     * 新增券（total_stock 为 -1 时不限库存，remain_stock 同步 -1；否则 remain_stock 初始等于 total_stock）。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createCoupon(SaveCouponCommand command, String operatorUsername);

    /**
     * 修改券（已产生兑换记录的券禁止修改积分价与有效期相关字段）。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateCoupon(SaveCouponCommand command, String operatorUsername);

    /**
     * 批量删除券（已产生兑换记录的券禁止删除）。
     *
     * @param couponIds 券ID数组
     */
    void deleteCoupons(Long[] couponIds);
}
