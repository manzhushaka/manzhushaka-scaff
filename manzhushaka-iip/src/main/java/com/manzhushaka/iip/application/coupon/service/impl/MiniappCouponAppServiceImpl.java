package com.manzhushaka.iip.application.coupon.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.application.coupon.result.CouponDetailResult;
import com.manzhushaka.iip.application.coupon.result.CouponMallItemResult;
import com.manzhushaka.iip.application.coupon.result.ExchangeResult;
import com.manzhushaka.iip.application.coupon.service.MiniappCouponAppService;
import com.manzhushaka.iip.application.exchange.result.MyCouponResult;
import com.manzhushaka.iip.domain.IipCoupon;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.domain.IipMerchant;
import com.manzhushaka.iip.service.IIipCouponRecordService;
import com.manzhushaka.iip.service.IIipCouponService;
import com.manzhushaka.iip.service.IIipMerchantService;

/**
 * 小程序券应用服务实现（积分商城、券详情、兑换、我的券）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class MiniappCouponAppServiceImpl implements MiniappCouponAppService
{
    @Autowired
    private IIipCouponService couponService;

    @Autowired
    private IIipCouponRecordService couponRecordService;

    @Autowired
    private IIipMerchantService merchantService;

    /**
     * 查询积分商城券列表（上架且在兑换窗口内，可按品类筛选，按 sort、create_time desc）。
     *
     * @param category 券品类，null 或空表示全部
     * @return 商城券列表
     */
    @Override
    public List<CouponMallItemResult> listMallCoupons(String category)
    {
        return couponService.selectMallCouponList(new Date(), category).stream()
                .map(this::toMallItemResult)
                .toList();
    }

    /**
     * 查询券详情（含当前用户已兑数量；券绑定商户且商户存在时附带商户展示信息，否则商户字段为 null）。
     *
     * @param couponId 券ID
     * @param memberId 当前用户ID
     * @return 券详情
     */
    @Override
    public CouponDetailResult getCouponDetail(Long couponId, Long memberId)
    {
        IipCoupon coupon = couponService.selectIipCouponById(couponId);
        if (coupon == null)
        {
            throw new ServiceException("券不存在");
        }
        int exchangedCount = couponRecordService.countByCouponAndMember(couponId, memberId);
        // 券未绑定商户或商户已删除时不补商户信息，不影响详情主流程
        IipMerchant merchant = coupon.getMerchantId() == null ? null
                : merchantService.selectIipMerchantById(coupon.getMerchantId());
        return new CouponDetailResult(coupon.getCouponId(), coupon.getCouponName(), coupon.getCouponType(),
                coupon.getCoverImage(), coupon.getTargetName(), coupon.getPointsCost(), coupon.getRemainStock(),
                coupon.getPerMemberLimit(), coupon.getExchangeStartTime(), coupon.getExchangeEndTime(),
                coupon.getValidType(), coupon.getValidDays(), coupon.getValidStartTime(), coupon.getValidEndTime(),
                coupon.getThresholdAmount(), coupon.getDiscountAmount(), coupon.getMerchantId(), coupon.getUseDesc(),
                coupon.getStatus(), coupon.getCategory(), coupon.getSponsorType(), coupon.getSponsorName(),
                exchangedCount,
                merchant == null ? null : merchant.getMerchantName(),
                merchant == null ? null : merchant.getLogo(),
                merchant == null ? null : merchant.getDescription(),
                merchant == null ? null : merchant.getAddress(),
                merchant == null ? null : merchant.getContactPhone(),
                merchant == null ? null : merchant.getBusinessHours(),
                merchant == null ? null : merchant.getLongitude(),
                merchant == null ? null : merchant.getLatitude());
    }

    /**
     * 兑换券（限兑/库存/活动额度/积分扣减整体事务）。
     *
     * @param memberId 当前用户ID
     * @param couponId 券ID
     * @return 兑换成功的券实例（含核销码）
     */
    @Override
    public ExchangeResult exchange(Long memberId, Long couponId)
    {
        IipCouponRecord record = couponRecordService.exchangeCoupon(memberId, couponId);
        return new ExchangeResult(record.getRecordId(), record.getCouponId(), record.getCouponName(),
                record.getCouponType(), record.getPointsCost(), record.getVerifyCode(), record.getStatus(),
                record.getExchangeTime(), record.getValidStartTime(), record.getValidEndTime(),
                record.getActivityId());
    }

    /**
     * 查询我的券（查询前先将本人已过期未使用券置为已过期，按兑换时间倒序；
     * 券品类与赞助方取自券定义，券已删除时对应字段为 null）。
     *
     * @param memberId 当前用户ID
     * @param status 状态（0未使用 1已使用 2已过期），null 或空表示全部
     * @return 我的券列表
     */
    @Override
    public List<MyCouponResult> listMyCoupons(Long memberId, String status)
    {
        couponRecordService.expireByMember(memberId);
        List<IipCouponRecord> records = couponRecordService.selectByMember(memberId, status);
        Map<Long, IipCoupon> couponMap = new HashMap<>(records.size() * 2);
        for (IipCouponRecord record : records)
        {
            couponMap.computeIfAbsent(record.getCouponId(), couponService::selectIipCouponById);
        }
        return records.stream()
                .map(record -> toMyCouponResult(record, couponMap.get(record.getCouponId())))
                .toList();
    }

    private CouponMallItemResult toMallItemResult(IipCoupon coupon)
    {
        return new CouponMallItemResult(coupon.getCouponId(), coupon.getCouponName(), coupon.getCouponType(),
                coupon.getCoverImage(), coupon.getTargetName(), coupon.getPointsCost(), coupon.getRemainStock(),
                coupon.getPerMemberLimit(), coupon.getValidType(), coupon.getValidDays(), coupon.getValidStartTime(),
                coupon.getValidEndTime(), coupon.getUseDesc(), coupon.getCategory(), coupon.getSponsorType(),
                coupon.getSponsorName());
    }

    private MyCouponResult toMyCouponResult(IipCouponRecord record, IipCoupon coupon)
    {
        return new MyCouponResult(record.getRecordId(), record.getCouponId(), record.getCouponName(),
                record.getCouponType(), record.getPointsCost(), record.getVerifyCode(), record.getStatus(),
                record.getExchangeTime(), record.getValidStartTime(), record.getValidEndTime(),
                record.getVerifyTime(), record.getActivityId(),
                coupon == null ? null : coupon.getCategory(),
                coupon == null ? null : coupon.getSponsorType(),
                coupon == null ? null : coupon.getSponsorName());
    }
}
