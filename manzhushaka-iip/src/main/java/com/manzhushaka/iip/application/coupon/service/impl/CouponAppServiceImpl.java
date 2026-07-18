package com.manzhushaka.iip.application.coupon.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.application.coupon.command.SaveCouponCommand;
import com.manzhushaka.iip.application.coupon.query.CouponQuery;
import com.manzhushaka.iip.application.coupon.result.CouponResult;
import com.manzhushaka.iip.application.coupon.service.CouponAppService;
import com.manzhushaka.iip.domain.IipCoupon;
import com.manzhushaka.iip.service.IIipCouponRecordService;
import com.manzhushaka.iip.service.IIipCouponService;

/**
 * 券定义应用服务实现（管理端）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class CouponAppServiceImpl implements CouponAppService
{
    /** 券类型白名单 */
    private static final Set<String> COUPON_TYPES = Set.of("ticket", "virtual", "full_reduction", "discount");

    /** 券品类白名单 */
    private static final Set<String> COUPON_CATEGORIES = Set.of("general", "scenic_ticket", "hotel", "dining",
            "flight_package", "duty_free");

    /** 赞助方类型白名单 */
    private static final Set<String> SPONSOR_TYPES = Set.of("platform", "bank", "merchant");

    /** 赞助方类型：平台 */
    private static final String SPONSOR_TYPE_PLATFORM = "platform";

    /** 券类型：满减 */
    private static final String COUPON_TYPE_FULL_REDUCTION = "full_reduction";

    /** 有效期类型：固定区间 */
    private static final String VALID_TYPE_FIXED = "fixed";

    /** 有效期类型：领取后N天 */
    private static final String VALID_TYPE_DAYS = "days";

    /** 不限数量标识 */
    private static final int UNLIMITED = -1;

    /** 默认每人限兑数量 */
    private static final int DEFAULT_PER_MEMBER_LIMIT = 1;

    @Autowired
    private IIipCouponService couponService;

    @Autowired
    private IIipCouponRecordService couponRecordService;

    /**
     * 查询券列表。
     *
     * @param query 查询条件
     * @return 券列表
     */
    @Override
    public List<CouponResult> listCoupons(CouponQuery query)
    {
        return couponService.selectIipCouponList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 查询券详情。
     *
     * @param couponId 券ID
     * @return 券详情，不存在时返回 null
     */
    @Override
    public CouponResult getCoupon(Long couponId)
    {
        return toResult(couponService.selectIipCouponById(couponId));
    }

    /**
     * 新增券（total_stock 为 -1 时不限库存，remain_stock 同步 -1；否则 remain_stock 初始等于 total_stock）。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    @Override
    @Transactional
    public int createCoupon(SaveCouponCommand command, String operatorUsername)
    {
        validateCouponRules(command);
        IipCoupon coupon = toEntity(command);
        coupon.setRemainStock(coupon.getTotalStock() == UNLIMITED ? UNLIMITED : coupon.getTotalStock());
        coupon.setCreateBy(operatorUsername);
        return couponService.insertIipCoupon(coupon);
    }

    /**
     * 修改券（已产生兑换记录的券禁止修改积分价与有效期相关字段）。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    @Override
    @Transactional
    public int updateCoupon(SaveCouponCommand command, String operatorUsername)
    {
        if (command.couponId() == null)
        {
            throw new ServiceException("券ID不能为空");
        }
        IipCoupon oldCoupon = couponService.selectIipCouponById(command.couponId());
        if (oldCoupon == null)
        {
            throw new ServiceException("券不存在");
        }
        validateCouponRules(command);
        if (couponRecordService.countByCouponAndMember(command.couponId(), null) > 0
                && exchangeRuleChanged(oldCoupon, command))
        {
            throw new ServiceException("已有兑换记录，不允许修改兑换规则");
        }
        IipCoupon coupon = toEntity(command);
        if (coupon.getTotalStock() == UNLIMITED)
        {
            coupon.setRemainStock(UNLIMITED);
        }
        coupon.setUpdateBy(operatorUsername);
        return couponService.updateIipCoupon(coupon);
    }

    /**
     * 批量删除券（已产生兑换记录的券禁止删除）。
     *
     * @param couponIds 券ID数组
     */
    @Override
    @Transactional
    public void deleteCoupons(Long[] couponIds)
    {
        for (Long couponId : couponIds)
        {
            IipCoupon coupon = couponService.selectIipCouponById(couponId);
            if (coupon != null && couponRecordService.countByCouponAndMember(couponId, null) > 0)
            {
                throw new ServiceException("券【" + coupon.getCouponName() + "】已有兑换记录，不允许删除");
            }
        }
        couponService.deleteIipCouponByIds(couponIds);
    }

    /**
     * 校验券定义业务规则：类型与品类白名单、赞助方配套字段、库存与限兑取值、有效期配套字段、满减金额必填。
     *
     * @param command 保存命令
     */
    private void validateCouponRules(SaveCouponCommand command)
    {
        if (!COUPON_TYPES.contains(command.couponType()))
        {
            throw new ServiceException("券类型不合法，仅支持门票/虚拟物品/满减/折扣");
        }
        if (!COUPON_CATEGORIES.contains(command.category()))
        {
            throw new ServiceException("券品类不合法，仅支持通用/景区门票/酒店券/餐饮券/机票+权益包/免税周边");
        }
        if (!SPONSOR_TYPES.contains(command.sponsorType()))
        {
            throw new ServiceException("赞助方类型不合法，仅支持平台/银行/商户");
        }
        if (!SPONSOR_TYPE_PLATFORM.equals(command.sponsorType())
                && (command.sponsorName() == null || command.sponsorName().isBlank()))
        {
            throw new ServiceException("赞助方类型为银行或商户时必须填写赞助方名称");
        }
        if (command.totalStock() == null || command.totalStock() < UNLIMITED)
        {
            throw new ServiceException("总库存不合法，-1 表示不限");
        }
        if (command.perMemberLimit() != null
                && (command.perMemberLimit() < UNLIMITED || command.perMemberLimit() == 0))
        {
            throw new ServiceException("每人限兑数量不合法，-1 表示不限");
        }
        validateValidRule(command);
        if (COUPON_TYPE_FULL_REDUCTION.equals(command.couponType())
                && (command.thresholdAmount() == null || command.discountAmount() == null))
        {
            throw new ServiceException("满减券必须填写满减门槛金额与满减面额");
        }
    }

    /**
     * 校验有效期规则：fixed 需起止时间非空且结束晚于开始；days 需有效天数大于 0。
     *
     * @param command 保存命令
     */
    private void validateValidRule(SaveCouponCommand command)
    {
        if (VALID_TYPE_FIXED.equals(command.validType()))
        {
            if (command.validStartTime() == null || command.validEndTime() == null)
            {
                throw new ServiceException("固定区间有效期必须填写有效期开始与结束时间");
            }
            if (!command.validEndTime().after(command.validStartTime()))
            {
                throw new ServiceException("有效期结束时间必须晚于开始时间");
            }
        }
        else if (VALID_TYPE_DAYS.equals(command.validType()))
        {
            if (command.validDays() == null || command.validDays() <= 0)
            {
                throw new ServiceException("领取后N天有效期必须填写大于0的有效天数");
            }
        }
        else
        {
            throw new ServiceException("有效期类型不合法，仅支持 fixed/days");
        }
    }

    /**
     * 判断兑换规则字段是否变更（积分价与有效期相关字段）。
     *
     * @param oldCoupon 原券
     * @param command 保存命令
     * @return true 表示兑换规则发生变更
     */
    private boolean exchangeRuleChanged(IipCoupon oldCoupon, SaveCouponCommand command)
    {
        return !Objects.equals(oldCoupon.getPointsCost(), command.pointsCost())
                || !Objects.equals(oldCoupon.getValidType(), command.validType())
                || !Objects.equals(oldCoupon.getValidStartTime(), command.validStartTime())
                || !Objects.equals(oldCoupon.getValidEndTime(), command.validEndTime())
                || !Objects.equals(oldCoupon.getValidDays(), command.validDays());
    }

    private IipCoupon toEntity(CouponQuery query)
    {
        IipCoupon coupon = new IipCoupon();
        if (query == null)
        {
            return coupon;
        }
        coupon.setCouponName(query.couponName());
        coupon.setCouponType(query.couponType());
        coupon.setStatus(query.status());
        coupon.setCategory(query.category());
        return coupon;
    }

    private IipCoupon toEntity(SaveCouponCommand command)
    {
        IipCoupon coupon = new IipCoupon();
        coupon.setCouponId(command.couponId());
        coupon.setCouponName(command.couponName());
        coupon.setCouponType(command.couponType());
        coupon.setCoverImage(command.coverImage());
        coupon.setTargetName(command.targetName());
        coupon.setPointsCost(command.pointsCost());
        coupon.setTotalStock(command.totalStock());
        coupon.setRemainStock(command.remainStock());
        coupon.setPerMemberLimit(command.perMemberLimit() == null ? DEFAULT_PER_MEMBER_LIMIT
                : command.perMemberLimit());
        coupon.setExchangeStartTime(command.exchangeStartTime());
        coupon.setExchangeEndTime(command.exchangeEndTime());
        coupon.setValidType(command.validType());
        coupon.setValidStartTime(command.validStartTime());
        coupon.setValidEndTime(command.validEndTime());
        coupon.setValidDays(command.validDays());
        coupon.setThresholdAmount(command.thresholdAmount());
        coupon.setDiscountAmount(command.discountAmount());
        coupon.setMerchantId(command.merchantId());
        coupon.setUseDesc(command.useDesc());
        coupon.setStatus(command.status());
        coupon.setSort(command.sort());
        coupon.setRemark(command.remark());
        coupon.setCategory(command.category());
        coupon.setSponsorType(command.sponsorType());
        coupon.setSponsorName(command.sponsorName());
        return coupon;
    }

    private CouponResult toResult(IipCoupon coupon)
    {
        if (coupon == null)
        {
            return null;
        }
        return new CouponResult(coupon.getCouponId(), coupon.getCouponName(), coupon.getCouponType(),
                coupon.getCoverImage(), coupon.getTargetName(), coupon.getPointsCost(), coupon.getTotalStock(),
                coupon.getRemainStock(), coupon.getPerMemberLimit(), coupon.getExchangeStartTime(),
                coupon.getExchangeEndTime(), coupon.getValidType(), coupon.getValidStartTime(),
                coupon.getValidEndTime(), coupon.getValidDays(), coupon.getThresholdAmount(),
                coupon.getDiscountAmount(), coupon.getMerchantId(), coupon.getUseDesc(), coupon.getStatus(),
                coupon.getSort(), coupon.getCategory(), coupon.getSponsorType(), coupon.getSponsorName(),
                coupon.getCreateBy(), coupon.getCreateTime(), coupon.getUpdateBy(),
                coupon.getUpdateTime(), coupon.getRemark());
    }
}
