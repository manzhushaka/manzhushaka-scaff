package com.manzhushaka.iip.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.iip.domain.IipActivity;
import com.manzhushaka.iip.domain.IipActivityCoupon;
import com.manzhushaka.iip.domain.IipActivityMerchant;
import com.manzhushaka.iip.domain.IipCoupon;
import com.manzhushaka.iip.domain.IipMerchant;
import com.manzhushaka.iip.mapper.IipActivityCouponMapper;
import com.manzhushaka.iip.mapper.IipActivityMapper;
import com.manzhushaka.iip.mapper.IipActivityMerchantMapper;
import com.manzhushaka.iip.mapper.IipCouponMapper;
import com.manzhushaka.iip.mapper.IipMerchantMapper;
import com.manzhushaka.iip.service.IIipActivityService;

/**
 * 活动 服务层实现
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipActivityServiceImpl implements IIipActivityService
{
    /** 不限量标识（merchant_limit、coupon_quota、issue_limit、total_stock 通用） */
    private static final int UNLIMITED = -1;

    /** 启用状态 */
    private static final String STATUS_ENABLED = "0";

    /** 地域类型：全省 */
    private static final String REGION_TYPE_PROVINCE = "province";

    /** 地域类型：市县 */
    private static final String REGION_TYPE_CITY = "city";

    /** 地域类型：商圈 */
    private static final String REGION_TYPE_BUSINESS_DISTRICT = "business_district";

    /** 地域类型：景区 */
    private static final String REGION_TYPE_SCENIC = "scenic";

    /** 活动编号前缀 */
    private static final String ACTIVITY_NO_PREFIX = "A";

    /** 活动编号年月格式 */
    private static final DateTimeFormatter ACTIVITY_NO_MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    /** 活动编号序号长度 */
    private static final int ACTIVITY_NO_SEQ_LENGTH = 4;

    /** 活动编号生成最大重试次数（并发冲突时重新生成） */
    private static final int ACTIVITY_NO_MAX_RETRY = 3;

    private static final Logger log = LoggerFactory.getLogger(IipActivityServiceImpl.class);

    @Autowired
    private IipActivityMapper activityMapper;

    @Autowired
    private IipActivityMerchantMapper activityMerchantMapper;

    @Autowired
    private IipActivityCouponMapper activityCouponMapper;

    @Autowired
    private IipMerchantMapper merchantMapper;

    @Autowired
    private IipCouponMapper couponMapper;

    /**
     * 通过ID查询活动
     * 
     * @param activityId 活动ID
     * @return 活动信息，不存在时返回null
     */
    @Override
    public IipActivity selectIipActivityById(Long activityId)
    {
        return activityMapper.selectIipActivityById(activityId);
    }

    /**
     * 查询活动列表
     * 
     * @param iipActivity 查询条件
     * @return 活动集合
     */
    @Override
    public List<IipActivity> selectIipActivityList(IipActivity iipActivity)
    {
        return activityMapper.selectIipActivityList(iipActivity);
    }

    /**
     * 新增活动（服务端生成活动编号 A+yyyyMM+4位序号，校验起止时间与积分比例）
     * 
     * @param iipActivity 活动信息
     * @return 结果
     */
    @Override
    public int insertIipActivity(IipActivity iipActivity)
    {
        validateActivity(iipActivity);
        fillActivityDefaults(iipActivity);
        for (int attempt = 0; attempt < ACTIVITY_NO_MAX_RETRY; attempt++)
        {
            iipActivity.setActivityNo(generateActivityNo());
            try
            {
                return activityMapper.insertIipActivity(iipActivity);
            }
            catch (DuplicateKeyException e)
            {
                // 并发下活动编号序号冲突，记录原因后重新生成序号重试，不影响数据一致性
                log.warn("活动编号{}冲突，重新生成序号", iipActivity.getActivityNo(), e);
            }
        }
        throw new ServiceException("活动编号生成失败，请重试");
    }

    /**
     * 修改活动（校验活动存在、起止时间与积分比例，活动编号不可变更）
     * 
     * @param iipActivity 活动信息
     * @return 结果
     */
    @Override
    public int updateIipActivity(IipActivity iipActivity)
    {
        IipActivity existing = activityMapper.selectIipActivityById(iipActivity.getActivityId());
        if (existing == null)
        {
            throw new ServiceException("活动不存在");
        }
        validateActivity(iipActivity);
        fillActivityDefaults(iipActivity);
        return activityMapper.updateIipActivity(iipActivity);
    }

    /**
     * 批量删除活动（启用中且在时间窗内的活动禁止删除；级联删除活动商户与活动券配置）
     * 
     * @param activityIds 需要删除的活动ID
     */
    @Override
    @Transactional
    public void deleteIipActivityByIds(Long[] activityIds)
    {
        Date now = new Date();
        for (Long activityId : activityIds)
        {
            IipActivity activity = activityMapper.selectIipActivityById(activityId);
            if (activity == null)
            {
                continue;
            }
            if (isActivityInProgress(activity, now))
            {
                throw new ServiceException("活动进行中，请先停用");
            }
            activityMerchantMapper.deleteByActivityId(activityId);
            activityCouponMapper.deleteByActivityId(activityId);
            activityMapper.deleteIipActivityById(activityId);
        }
    }

    /**
     * 查询当前生效活动（启用且在时间窗内，取优先级最高的一个）
     * 
     * @param now 当前时间
     * @return 当前活动信息，不存在时返回null
     */
    @Override
    public IipActivity selectCurrentActivity(Date now)
    {
        return activityMapper.selectCurrentActivity(now);
    }

    /**
     * 查询全部生效活动（启用且在时间窗内，按优先级倒序、开始时间倒序）
     *
     * @param now 当前时间
     * @return 生效活动集合，无生效活动时返回空集合
     */
    @Override
    public List<IipActivity> selectActiveActivities(Date now)
    {
        return activityMapper.selectActiveActivities(now);
    }

    /**
     * 按活动ID查询参与商户关联列表（join iip_merchant 携带商户编号/名称/类别/状态）
     * 
     * @param activityId 活动ID
     * @return 活动商户关联集合（含商户展示字段）
     */
    @Override
    public List<IipActivityMerchant> selectMerchantJoinList(Long activityId)
    {
        return activityMerchantMapper.selectMerchantJoinList(activityId);
    }

    /**
     * 统计活动已配置商户数量
     * 
     * @param activityId 活动ID
     * @return 已配置商户数量
     */
    @Override
    public int countMerchantByActivityId(Long activityId)
    {
        return activityMerchantMapper.countByActivityId(activityId);
    }

    /**
     * 新增活动商户配置（校验活动存在、商户存在且正常、参与商户数上限、唯一键冲突）
     * 
     * @param activityMerchant 活动商户关联信息
     * @return 结果
     */
    @Override
    public int insertActivityMerchant(IipActivityMerchant activityMerchant)
    {
        IipActivity activity = getActivityOrThrow(activityMerchant.getActivityId());
        IipMerchant merchant = merchantMapper.selectIipMerchantById(activityMerchant.getMerchantId());
        if (merchant == null)
        {
            throw new ServiceException("商户不存在");
        }
        if (!STATUS_ENABLED.equals(merchant.getStatus()))
        {
            throw new ServiceException("商户未处于正常状态，无法配置");
        }
        Integer merchantLimit = activity.getMerchantLimit();
        if (merchantLimit != null && merchantLimit != UNLIMITED
                && activityMerchantMapper.countByActivityId(activity.getActivityId()) >= merchantLimit)
        {
            throw new ServiceException("参与商户数已达上限");
        }
        if (StringUtils.isEmpty(activityMerchant.getStatus()))
        {
            activityMerchant.setStatus(STATUS_ENABLED);
        }
        try
        {
            return activityMerchantMapper.insertIipActivityMerchant(activityMerchant);
        }
        catch (DuplicateKeyException e)
        {
            // 唯一键 uk_act_mer 冲突，ServiceException 无法携带 cause，记录日志保留根因
            log.warn("活动{}商户{}配置唯一键冲突", activityMerchant.getActivityId(), activityMerchant.getMerchantId(), e);
            throw new ServiceException("该商户已配置");
        }
    }

    /**
     * 通过ID删除活动商户配置
     * 
     * @param id 主键ID
     * @return 结果
     */
    @Override
    public int deleteActivityMerchantById(Long id)
    {
        return activityMerchantMapper.deleteIipActivityMerchantById(id);
    }

    /**
     * 通过ID查询活动券配置
     * 
     * @param id 主键ID
     * @return 活动券配置信息，不存在时返回null
     */
    @Override
    public IipActivityCoupon selectActivityCouponById(Long id)
    {
        return activityCouponMapper.selectIipActivityCouponById(id);
    }

    /**
     * 按活动ID查询活动券配置列表（join iip_coupon 携带券名称/积分价/封面/库存）
     * 
     * @param activityId 活动ID
     * @return 活动券配置集合（含券展示字段）
     */
    @Override
    public List<IipActivityCoupon> selectCouponJoinList(Long activityId)
    {
        return activityCouponMapper.selectCouponJoinList(activityId);
    }

    /**
     * 统计活动已配置券数量
     * 
     * @param activityId 活动ID
     * @return 已配置券数量
     */
    @Override
    public int countCouponByActivityId(Long activityId)
    {
        return activityCouponMapper.countByActivityId(activityId);
    }

    /**
     * 新增活动券配置（校验活动存在、券存在、活动发券额度、券总库存、唯一键冲突）
     * 
     * @param activityCoupon 活动券配置信息
     * @return 结果
     */
    @Override
    public int insertActivityCoupon(IipActivityCoupon activityCoupon)
    {
        IipActivity activity = getActivityOrThrow(activityCoupon.getActivityId());
        IipCoupon coupon = getCouponOrThrow(activityCoupon.getCouponId());
        validateIssueLimit(activity, coupon, activityCoupon.getIssueLimit(), null);
        activityCoupon.setIssuedCount(0);
        try
        {
            return activityCouponMapper.insertIipActivityCoupon(activityCoupon);
        }
        catch (DuplicateKeyException e)
        {
            // 唯一键 uk_act_coupon 冲突，ServiceException 无法携带 cause，记录日志保留根因
            log.warn("活动{}券{}配置唯一键冲突", activityCoupon.getActivityId(), activityCoupon.getCouponId(), e);
            throw new ServiceException("该券已配置");
        }
    }

    /**
     * 修改活动券配置发行上限（同新增校验，额度合计排除自身）
     * 
     * @param activityCoupon 活动券配置信息（仅需主键ID与发行上限）
     * @return 结果
     */
    @Override
    public int updateActivityCouponIssueLimit(IipActivityCoupon activityCoupon)
    {
        IipActivityCoupon existing = activityCouponMapper.selectIipActivityCouponById(activityCoupon.getId());
        if (existing == null)
        {
            throw new ServiceException("活动券配置不存在");
        }
        IipActivity activity = getActivityOrThrow(existing.getActivityId());
        IipCoupon coupon = getCouponOrThrow(existing.getCouponId());
        validateIssueLimit(activity, coupon, activityCoupon.getIssueLimit(), existing.getId());

        IipActivityCoupon update = new IipActivityCoupon();
        update.setId(existing.getId());
        update.setIssueLimit(activityCoupon.getIssueLimit());
        update.setUpdateBy(activityCoupon.getUpdateBy());
        return activityCouponMapper.updateIipActivityCoupon(update);
    }

    /**
     * 通过ID删除活动券配置
     * 
     * @param id 主键ID
     * @return 结果
     */
    @Override
    public int deleteActivityCouponById(Long id)
    {
        return activityCouponMapper.deleteIipActivityCouponById(id);
    }

    /**
     * 查询活动，不存在时抛出业务异常
     * 
     * @param activityId 活动ID
     * @return 活动信息
     */
    private IipActivity getActivityOrThrow(Long activityId)
    {
        IipActivity activity = activityMapper.selectIipActivityById(activityId);
        if (activity == null)
        {
            throw new ServiceException("活动不存在");
        }
        return activity;
    }

    /**
     * 查询券，不存在时抛出业务异常
     * 
     * @param couponId 券ID
     * @return 券信息
     */
    private IipCoupon getCouponOrThrow(Long couponId)
    {
        IipCoupon coupon = couponMapper.selectIipCouponById(couponId);
        if (coupon == null)
        {
            throw new ServiceException("券不存在");
        }
        return coupon;
    }

    /**
     * 校验活动起止时间、积分比例与地域字段（商圈/景区活动必须填区域名称，市县活动必须填适用市县）
     * 
     * @param iipActivity 活动信息
     */
    private void validateActivity(IipActivity iipActivity)
    {
        if (iipActivity.getStartTime() == null || iipActivity.getEndTime() == null)
        {
            throw new ServiceException("活动起止时间不能为空");
        }
        if (!iipActivity.getStartTime().before(iipActivity.getEndTime()))
        {
            throw new ServiceException("开始时间必须早于结束时间");
        }
        if (iipActivity.getPointsRatio() == null
                || iipActivity.getPointsRatio().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("积分比例必须大于0");
        }
        String regionType = iipActivity.getRegionType();
        if ((REGION_TYPE_BUSINESS_DISTRICT.equals(regionType) || REGION_TYPE_SCENIC.equals(regionType))
                && StringUtils.isBlank(iipActivity.getRegionName()))
        {
            throw new ServiceException("商圈/景区活动必须填写商圈或景区名称");
        }
        if (REGION_TYPE_CITY.equals(regionType) && StringUtils.isBlank(iipActivity.getCity()))
        {
            throw new ServiceException("市县活动必须填写适用市县");
        }
    }

    /**
     * 填充活动默认值（参与商户数上限、发券总额度默认不限，地域类型默认全省，优先级默认0，状态默认启用）
     * 
     * @param iipActivity 活动信息
     */
    private void fillActivityDefaults(IipActivity iipActivity)
    {
        if (iipActivity.getMerchantLimit() == null)
        {
            iipActivity.setMerchantLimit(UNLIMITED);
        }
        if (iipActivity.getCouponQuota() == null)
        {
            iipActivity.setCouponQuota(UNLIMITED);
        }
        if (StringUtils.isEmpty(iipActivity.getRegionType()))
        {
            iipActivity.setRegionType(REGION_TYPE_PROVINCE);
        }
        if (iipActivity.getPriority() == null)
        {
            iipActivity.setPriority(0);
        }
        if (StringUtils.isEmpty(iipActivity.getStatus()))
        {
            iipActivity.setStatus(STATUS_ENABLED);
        }
    }

    /**
     * 判断活动是否进行中（启用且当前时间在活动时间窗内）
     * 
     * @param activity 活动信息
     * @param now 当前时间
     * @return true 表示活动进行中
     */
    private boolean isActivityInProgress(IipActivity activity, Date now)
    {
        return STATUS_ENABLED.equals(activity.getStatus())
                && activity.getStartTime() != null && activity.getEndTime() != null
                && !now.before(activity.getStartTime()) && !now.after(activity.getEndTime());
    }

    /**
     * 生成活动编号（A+yyyyMM+4位序号，基于当月最大序号递增并查重）
     * 
     * @return 活动编号
     */
    private String generateActivityNo()
    {
        String prefix = ACTIVITY_NO_PREFIX + LocalDate.now().format(ACTIVITY_NO_MONTH);
        int seq = 1;
        String maxNo = activityMapper.selectMaxActivityNo(prefix);
        if (StringUtils.isNotEmpty(maxNo) && maxNo.length() == prefix.length() + ACTIVITY_NO_SEQ_LENGTH)
        {
            seq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
        }
        String activityNo = prefix + String.format("%04d", seq);
        while (activityMapper.countByActivityNo(activityNo) > 0)
        {
            seq++;
            activityNo = prefix + String.format("%04d", seq);
        }
        return activityNo;
    }

    /**
     * 校验活动券配置发行上限（取值、活动发券总额度、券总库存）
     * 
     * @param activity 活动信息
     * @param coupon 券信息
     * @param issueLimit 发行上限（-1不限）
     * @param excludeId 额度合计需要排除的配置ID（修改场景排除自身），可为null
     */
    private void validateIssueLimit(IipActivity activity, IipCoupon coupon, Integer issueLimit, Long excludeId)
    {
        if (issueLimit == null || (issueLimit != UNLIMITED && issueLimit <= 0))
        {
            throw new ServiceException("发行上限必须为-1（不限）或大于0的整数");
        }
        Integer couponQuota = activity.getCouponQuota();
        if (couponQuota != null && couponQuota != UNLIMITED)
        {
            if (issueLimit == UNLIMITED)
            {
                throw new ServiceException("活动额度有限时不允许不限量配置");
            }
            int usedQuota = activityCouponMapper.sumIssueLimitByActivityId(activity.getActivityId(), excludeId);
            if (usedQuota + issueLimit > couponQuota)
            {
                throw new ServiceException("活动发券总额度不足");
            }
        }
        Integer totalStock = coupon.getTotalStock();
        if (totalStock != null && totalStock != UNLIMITED && issueLimit > totalStock)
        {
            throw new ServiceException("发行上限超过券总库存");
        }
    }
}
