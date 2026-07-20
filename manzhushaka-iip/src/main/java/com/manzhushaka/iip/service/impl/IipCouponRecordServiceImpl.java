package com.manzhushaka.iip.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.iip.domain.IipActivity;
import com.manzhushaka.iip.domain.IipActivityCoupon;
import com.manzhushaka.iip.domain.IipCoupon;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.mapper.IipActivityCouponMapper;
import com.manzhushaka.iip.mapper.IipActivityMapper;
import com.manzhushaka.iip.mapper.IipCouponMapper;
import com.manzhushaka.iip.mapper.IipCouponRecordMapper;
import com.manzhushaka.iip.service.IIipCouponRecordService;
import com.manzhushaka.iip.service.IIipPointsService;

/**
 * 券实例（兑换记录） 服务层实现
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipCouponRecordServiceImpl implements IIipCouponRecordService
{
    /** 券状态：上架 */
    private static final String COUPON_STATUS_ON_SHELF = "0";

    /** 券实例状态：未使用 */
    private static final String RECORD_STATUS_UNUSED = "0";

    /** 有效期类型：领取后N天 */
    private static final String VALID_TYPE_DAYS = "days";

    /** 不限数量标识 */
    private static final int UNLIMITED = -1;

    /** 核销码长度（大写字母+数字，UUID 截取） */
    private static final int VERIFY_CODE_LENGTH = 16;

    /** 积分消费业务来源：券兑换 */
    private static final String BIZ_TYPE_COUPON_EXCHANGE = "coupon_exchange";

    /** 积分退款业务来源：管理员作废券 */
    private static final String BIZ_TYPE_COUPON_VOID_REFUND = "coupon_void_refund";

    /** 作废退回积分有效期天数 */
    private static final int REFUND_POINTS_VALID_DAYS = 365;

    @Autowired
    private IipCouponRecordMapper couponRecordMapper;

    @Autowired
    private IipCouponMapper couponMapper;

    @Autowired
    private IipActivityMapper activityMapper;

    @Autowired
    private IipActivityCouponMapper activityCouponMapper;

    @Autowired
    private IIipPointsService pointsService;

    /**
     * 通过ID查询券实例
     * 
     * @param recordId 记录ID
     * @return 券实例信息，不存在时返回null
     */
    @Override
    public IipCouponRecord selectIipCouponRecordById(Long recordId)
    {
        return couponRecordMapper.selectIipCouponRecordById(recordId);
    }

    /**
     * 查询券实例列表（管理端，按券名/用户/状态/核销码/兑换时间筛选）
     * 
     * @param iipCouponRecord 查询条件
     * @return 券实例集合
     */
    @Override
    public List<IipCouponRecord> selectIipCouponRecordList(IipCouponRecord iipCouponRecord)
    {
        return couponRecordMapper.selectIipCouponRecordList(iipCouponRecord);
    }

    /**
     * 查询用户本人的券实例列表（按兑换时间倒序）
     * 
     * @param memberId 用户ID
     * @param status 状态（0未使用 1已使用 2已过期 3已作废），null 或空表示全部
     * @return 券实例集合
     */
    @Override
    public List<IipCouponRecord> selectByMember(Long memberId, String status)
    {
        return couponRecordMapper.selectByMember(memberId, status);
    }

    /**
     * 统计兑换数量；memberId 为 null 时统计该券全表兑换数量
     * 
     * @param couponId 券ID
     * @param memberId 用户ID，可为 null
     * @return 兑换数量
     */
    @Override
    public int countByCouponAndMember(Long couponId, Long memberId)
    {
        return couponRecordMapper.countByCouponAndMember(couponId, memberId);
    }

    /**
     * 将用户本人已过期的未使用券批量置为已过期
     * 
     * @param memberId 用户ID
     * @return 影响行数
     */
    @Override
    public int expireByMember(Long memberId)
    {
        return couponRecordMapper.expireByMember(memberId, new Date());
    }

    /**
     * 兑换券（事务：上架与窗口校验、限兑校验、原子扣库存、多活动兑换归因、生成核销码、插入券实例、扣减积分）
     * 
     * @param memberId 用户ID
     * @param couponId 券ID
     * @return 兑换成功的券实例（含核销码）
     */
    @Override
    @Transactional
    public IipCouponRecord exchangeCoupon(Long memberId, Long couponId)
    {
        // a. 券存在且上架
        IipCoupon coupon = couponMapper.selectIipCouponById(couponId);
        if (coupon == null)
        {
            throw new ServiceException("券不存在");
        }
        if (!COUPON_STATUS_ON_SHELF.equals(coupon.getStatus()))
        {
            throw new ServiceException("券已下架，无法兑换");
        }

        // b. 兑换窗口校验（窗口为空表示不限制）
        Date now = new Date();
        if (coupon.getExchangeStartTime() != null && now.before(coupon.getExchangeStartTime()))
        {
            throw new ServiceException("兑换尚未开始");
        }
        if (coupon.getExchangeEndTime() != null && now.after(coupon.getExchangeEndTime()))
        {
            throw new ServiceException("兑换已结束");
        }

        // c. 每人限兑校验（-1 不限）
        if (coupon.getPerMemberLimit() != null && coupon.getPerMemberLimit() != UNLIMITED)
        {
            int exchanged = couponRecordMapper.countByCouponAndMember(couponId, memberId);
            if (exchanged >= coupon.getPerMemberLimit())
            {
                throw new ServiceException("已达每人限兑数量");
            }
        }

        // d. 原子扣减库存，影响行数为 0 表示库存不足
        if (couponMapper.decrStock(couponId) == 0)
        {
            throw new ServiceException("库存不足");
        }

        // e. 兑换归因：券若被生效活动配置，按活动优先级顺序尝试原子累加发券数量，
        // 第一个累加成功的活动写入来源活动；全部失败（额度已满）时按商城通用兑换继续，来源活动为null
        Long activityId = attributeExchangeActivity(couponId, now);

        // f. 生成唯一核销码（16 位大写字母+数字，UUID 截取，查重循环）
        String verifyCode = generateVerifyCode();

        // g. 插入券实例：快照券名称/类型/积分价与有效期，状态未使用
        IipCouponRecord record = new IipCouponRecord();
        record.setCouponId(coupon.getCouponId());
        record.setCouponName(coupon.getCouponName());
        record.setCouponType(coupon.getCouponType());
        record.setMemberId(memberId);
        record.setPointsCost(coupon.getPointsCost());
        record.setVerifyCode(verifyCode);
        record.setStatus(RECORD_STATUS_UNUSED);
        record.setExchangeTime(now);
        fillValidSnapshot(record, coupon, now);
        record.setActivityId(activityId);
        couponRecordMapper.insertIipCouponRecord(record);

        // h. 扣减积分（余额不足抛 ServiceException，触发整体回滚）
        pointsService.consumePoints(memberId, coupon.getPointsCost(), BIZ_TYPE_COUPON_EXCHANGE, verifyCode,
                "兑换" + coupon.getCouponName());

        // i. 返回含核销码的券实例
        return record;
    }

    /**
     * 管理员作废未使用券，恢复库存和活动额度并退回原兑换积分。
     *
     * @param recordId 券实例ID
     * @param voidBy 操作人
     * @param voidReason 作废原因
     */
    @Override
    @Transactional
    public void voidUnusedCoupon(Long recordId, String voidBy, String voidReason)
    {
        if (recordId == null || StringUtils.isBlank(voidBy) || StringUtils.isBlank(voidReason))
        {
            throw new ServiceException("作废参数不完整");
        }
        IipCouponRecord record = couponRecordMapper.selectIipCouponRecordById(recordId);
        if (record == null)
        {
            throw new ServiceException("兑换记录不存在");
        }
        if (couponRecordMapper.voidUnusedAtomic(recordId, voidBy, voidReason) == 0)
        {
            throw new ServiceException("仅未使用券可以作废");
        }
        couponMapper.restoreStock(record.getCouponId());
        if (record.getActivityId() != null)
        {
            activityCouponMapper.decrIssued(record.getActivityId(), record.getCouponId());
        }
        if (record.getPointsCost() != null && record.getPointsCost() > 0)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, REFUND_POINTS_VALID_DAYS);
            pointsService.refundConsumedPoints(record.getMemberId(), record.getPointsCost(),
                    BIZ_TYPE_COUPON_VOID_REFUND, String.valueOf(recordId), calendar.getTime(),
                    "管理员作废券退回积分：" + record.getCouponName());
        }
    }

    /**
     * 兑换归因：遍历生效活动（已按优先级倒序），第一个配置了该券且原子累加发券数量成功的活动即为来源活动；
     * 券未被任何生效活动配置、或配置的活动额度均已满时返回null，按商城通用兑换继续。
     *
     * @param couponId 券ID
     * @param now 当前时间
     * @return 归因的活动ID，无归因时返回null
     */
    private Long attributeExchangeActivity(Long couponId, Date now)
    {
        List<IipActivity> activities = activityMapper.selectActiveActivities(now);
        if (activities.isEmpty())
        {
            return null;
        }
        List<IipActivityCoupon> activityCoupons = activityCouponMapper.selectByCouponId(couponId);
        if (activityCoupons.isEmpty())
        {
            return null;
        }
        for (IipActivity activity : activities)
        {
            IipActivityCoupon matched = activityCoupons.stream()
                    .filter(item -> activity.getActivityId().equals(item.getActivityId()))
                    .findFirst()
                    .orElse(null);
            if (matched != null && activityCouponMapper.incrIssued(matched.getId()) > 0)
            {
                return activity.getActivityId();
            }
        }
        return null;
    }

    /**
     * 填充券实例有效期快照：fixed 拷贝券区间；days 为当前时间至 now+validDays 的 23:59:59
     * 
     * @param record 券实例
     * @param coupon 券定义
     * @param now 当前时间
     */
    private void fillValidSnapshot(IipCouponRecord record, IipCoupon coupon, Date now)
    {
        if (VALID_TYPE_DAYS.equals(coupon.getValidType()))
        {
            record.setValidStartTime(now);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_YEAR, coupon.getValidDays());
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 0);
            record.setValidEndTime(calendar.getTime());
        }
        else
        {
            record.setValidStartTime(coupon.getValidStartTime());
            record.setValidEndTime(coupon.getValidEndTime());
        }
    }

    /**
     * 生成唯一核销码（16 位大写字母+数字，UUID 截取，按核销码查重循环）
     * 
     * @return 未占用的核销码
     */
    private String generateVerifyCode()
    {
        String verifyCode;
        do
        {
            verifyCode = UUID.randomUUID().toString().replace("-", "").toUpperCase()
                    .substring(0, VERIFY_CODE_LENGTH);
        }
        while (couponRecordMapper.selectByVerifyCode(verifyCode) != null);
        return verifyCode;
    }
}
