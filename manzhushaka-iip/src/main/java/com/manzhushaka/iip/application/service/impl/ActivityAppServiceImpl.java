package com.manzhushaka.iip.application.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.application.command.ActivityMerchantCommand;
import com.manzhushaka.iip.application.command.SaveActivityCommand;
import com.manzhushaka.iip.application.command.SaveActivityCouponCommand;
import com.manzhushaka.iip.application.query.ActivityQuery;
import com.manzhushaka.iip.application.result.activity.ActiveActivityResult;
import com.manzhushaka.iip.application.result.activity.ActivityCouponResult;
import com.manzhushaka.iip.application.result.activity.ActivityMerchantResult;
import com.manzhushaka.iip.application.result.activity.ActivityResult;
import com.manzhushaka.iip.application.result.activity.CurrentActivityCouponResult;
import com.manzhushaka.iip.application.result.activity.CurrentActivityResult;
import com.manzhushaka.iip.application.service.ActivityAppService;
import com.manzhushaka.iip.domain.IipActivity;
import com.manzhushaka.iip.domain.IipActivityCoupon;
import com.manzhushaka.iip.domain.IipActivityMerchant;
import com.manzhushaka.iip.domain.IipPointsRule;
import com.manzhushaka.iip.service.IIipActivityService;
import com.manzhushaka.iip.service.IIipPointsRuleService;

/**
 * 活动应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class ActivityAppServiceImpl implements ActivityAppService
{
    @Autowired
    private IIipActivityService activityService;

    @Autowired
    private IIipPointsRuleService pointsRuleService;

    @Override
    public List<ActivityResult> listActivities(ActivityQuery query)
    {
        return activityService.selectIipActivityList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public ActivityResult getActivity(Long activityId)
    {
        return toResult(activityService.selectIipActivityById(activityId));
    }

    @Override
    @Transactional
    public int createActivity(SaveActivityCommand command, String operatorUsername)
    {
        IipActivity activity = toEntity(command);
        activity.setCreateBy(operatorUsername);
        int rows = activityService.insertIipActivity(activity);
        pointsRuleService.saveRule(toPointsRule(activity.getActivityId(), command, operatorUsername, true));
        return rows;
    }

    @Override
    @Transactional
    public int updateActivity(SaveActivityCommand command, String operatorUsername)
    {
        IipActivity activity = toEntity(command);
        activity.setUpdateBy(operatorUsername);
        int rows = activityService.updateIipActivity(activity);
        pointsRuleService.saveRule(toPointsRule(activity.getActivityId(), command, operatorUsername, false));
        return rows;
    }

    @Override
    @Transactional
    public void deleteActivities(Long[] activityIds)
    {
        activityService.deleteIipActivityByIds(activityIds);
        pointsRuleService.deleteByActivityIds(activityIds);
    }

    @Override
    public List<ActivityMerchantResult> listActivityMerchants(Long activityId)
    {
        return activityService.selectMerchantJoinList(activityId).stream()
                .map(this::toMerchantResult)
                .toList();
    }

    @Override
    @Transactional
    public int addActivityMerchant(ActivityMerchantCommand command, String operatorUsername)
    {
        IipActivityMerchant activityMerchant = new IipActivityMerchant();
        activityMerchant.setActivityId(command.activityId());
        activityMerchant.setMerchantId(command.merchantId());
        activityMerchant.setCreateBy(operatorUsername);
        return activityService.insertActivityMerchant(activityMerchant);
    }

    @Override
    @Transactional
    public int removeActivityMerchant(Long id)
    {
        return activityService.deleteActivityMerchantById(id);
    }

    @Override
    public List<ActivityCouponResult> listActivityCoupons(Long activityId)
    {
        return activityService.selectCouponJoinList(activityId).stream()
                .map(this::toCouponResult)
                .toList();
    }

    @Override
    @Transactional
    public int addActivityCoupon(SaveActivityCouponCommand command, String operatorUsername)
    {
        if (command.activityId() == null || command.couponId() == null)
        {
            throw new ServiceException("活动ID与券ID不能为空");
        }
        IipActivityCoupon activityCoupon = new IipActivityCoupon();
        activityCoupon.setActivityId(command.activityId());
        activityCoupon.setCouponId(command.couponId());
        activityCoupon.setIssueLimit(command.issueLimit());
        activityCoupon.setCreateBy(operatorUsername);
        return activityService.insertActivityCoupon(activityCoupon);
    }

    @Override
    @Transactional
    public int updateActivityCoupon(SaveActivityCouponCommand command, String operatorUsername)
    {
        if (command.id() == null)
        {
            throw new ServiceException("活动券配置ID不能为空");
        }
        IipActivityCoupon activityCoupon = new IipActivityCoupon();
        activityCoupon.setId(command.id());
        activityCoupon.setIssueLimit(command.issueLimit());
        activityCoupon.setUpdateBy(operatorUsername);
        return activityService.updateActivityCouponIssueLimit(activityCoupon);
    }

    @Override
    @Transactional
    public int removeActivityCoupon(Long id)
    {
        return activityService.deleteActivityCouponById(id);
    }

    @Override
    public CurrentActivityResult getCurrentActivity()
    {
        IipActivity activity = activityService.selectCurrentActivity(new Date());
        if (activity == null)
        {
            return null;
        }
        List<CurrentActivityCouponResult> coupons = activityService.selectCouponJoinList(activity.getActivityId())
                .stream()
                .map(this::toCurrentCouponResult)
                .toList();
        return new CurrentActivityResult(activity.getActivityId(), activity.getActivityNo(),
                activity.getActivityName(), activity.getCoverImage(), activity.getDescription(),
                activity.getStartTime(), activity.getEndTime(), activity.getPointsRatio(),
                activity.getMerchantLimit(), activity.getCouponQuota(), activity.getStatus(),
                activity.getCity(), activity.getRegionType(), activity.getRegionName(), activity.getPriority(),
                activity.getCreateBy(), activity.getCreateTime(), activity.getUpdateBy(), activity.getUpdateTime(),
                activity.getRemark(), activityService.countMerchantByActivityId(activity.getActivityId()),
                activityService.countCouponByActivityId(activity.getActivityId()), coupons);
    }

    /**
     * 查询全部生效活动（启用且在时间窗内，按优先级与开始时间倒序，含参与商户数与配置券数）。
     *
     * @return 生效活动列表，无生效活动时返回空列表
     */
    @Override
    public List<ActiveActivityResult> listActiveActivities()
    {
        return activityService.selectActiveActivities(new Date()).stream()
                .map(this::toActiveActivityResult)
                .toList();
    }

    private ActiveActivityResult toActiveActivityResult(IipActivity activity)
    {
        return new ActiveActivityResult(activity.getActivityId(), activity.getActivityName(),
                activity.getStartTime(), activity.getEndTime(), activity.getPointsRatio(), activity.getCity(),
                activity.getRegionType(), activity.getRegionName(), activity.getPriority(),
                activityService.countMerchantByActivityId(activity.getActivityId()),
                activityService.countCouponByActivityId(activity.getActivityId()));
    }

    private IipActivity toEntity(ActivityQuery query)
    {
        IipActivity activity = new IipActivity();
        if (query == null)
        {
            return activity;
        }
        activity.setActivityNo(query.activityNo());
        activity.setActivityName(query.activityName());
        activity.setStatus(query.status());
        if (query.beginTime() != null)
        {
            activity.getParams().put("beginTime", query.beginTime());
        }
        if (query.endTime() != null)
        {
            activity.getParams().put("endTime", query.endTime());
        }
        return activity;
    }

    private IipActivity toEntity(SaveActivityCommand command)
    {
        IipActivity activity = new IipActivity();
        activity.setActivityId(command.activityId());
        activity.setActivityName(command.activityName());
        activity.setCoverImage(command.coverImage());
        activity.setDescription(command.description());
        activity.setStartTime(command.startTime());
        activity.setEndTime(command.endTime());
        activity.setPointsRatio(command.pointsRatio());
        activity.setMerchantLimit(command.merchantLimit());
        activity.setCouponQuota(command.couponQuota());
        activity.setCity(command.city());
        activity.setRegionType(command.regionType());
        activity.setRegionName(command.regionName());
        activity.setPriority(command.priority());
        activity.setStatus(command.status());
        activity.setRemark(command.remark());
        return activity;
    }

    private ActivityResult toResult(IipActivity activity)
    {
        if (activity == null)
        {
            return null;
        }
        IipPointsRule rule = pointsRuleService.getRule(activity.getActivityId());
        return new ActivityResult(activity.getActivityId(), activity.getActivityNo(), activity.getActivityName(),
                activity.getCoverImage(), activity.getDescription(), activity.getStartTime(), activity.getEndTime(),
                activity.getPointsRatio(), activity.getMerchantLimit(), activity.getCouponQuota(),
                activity.getCity(), activity.getRegionType(), activity.getRegionName(), activity.getPriority(),
                activity.getStatus(), activity.getCreateBy(), activity.getCreateTime(), activity.getUpdateBy(),
                activity.getUpdateTime(), activity.getRemark(), rule.getSingleInvoiceCap(),
                rule.getMonthlyMemberCap(), rule.getMerchantScope());
    }

    /**
     * 将活动保存命令转换为积分规则。
     *
     * @param activityId 活动ID
     * @param command 活动保存命令
     * @param operatorUsername 操作人
     * @param create 是否新增
     * @return 积分规则
     */
    private IipPointsRule toPointsRule(Long activityId, SaveActivityCommand command, String operatorUsername,
            boolean create)
    {
        IipPointsRule rule = new IipPointsRule();
        rule.setActivityId(activityId);
        rule.setSingleInvoiceCap(command.singleInvoiceCap() == null ? -1 : command.singleInvoiceCap());
        rule.setMonthlyMemberCap(command.monthlyMemberCap() == null ? -1 : command.monthlyMemberCap());
        rule.setMerchantScope(command.merchantScope() == null ? "all" : command.merchantScope());
        if (create)
        {
            rule.setCreateBy(operatorUsername);
        }
        else
        {
            rule.setUpdateBy(operatorUsername);
        }
        return rule;
    }

    private ActivityMerchantResult toMerchantResult(IipActivityMerchant activityMerchant)
    {
        return new ActivityMerchantResult(activityMerchant.getId(), activityMerchant.getActivityId(),
                activityMerchant.getMerchantId(), activityMerchant.getMerchantNo(),
                activityMerchant.getMerchantName(), activityMerchant.getCategory(),
                activityMerchant.getMerchantStatus(), activityMerchant.getStatus(),
                activityMerchant.getCreateTime(), activityMerchant.getRemark());
    }

    private ActivityCouponResult toCouponResult(IipActivityCoupon activityCoupon)
    {
        return new ActivityCouponResult(activityCoupon.getId(), activityCoupon.getActivityId(),
                activityCoupon.getCouponId(), activityCoupon.getCouponName(), activityCoupon.getPointsCost(),
                activityCoupon.getCoverImage(), activityCoupon.getTotalStock(), activityCoupon.getRemainStock(),
                activityCoupon.getIssueLimit(), activityCoupon.getIssuedCount(), activityCoupon.getCreateTime(),
                activityCoupon.getRemark());
    }

    private CurrentActivityCouponResult toCurrentCouponResult(IipActivityCoupon activityCoupon)
    {
        return new CurrentActivityCouponResult(activityCoupon.getCouponId(), activityCoupon.getCouponName(),
                activityCoupon.getPointsCost(), activityCoupon.getCoverImage(), activityCoupon.getIssueLimit(),
                activityCoupon.getIssuedCount());
    }
}
