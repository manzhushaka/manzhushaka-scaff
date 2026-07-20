package com.manzhushaka.iip.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.iip.domain.IipActivity;
import com.manzhushaka.iip.domain.IipInvoice;
import com.manzhushaka.iip.domain.IipMerchant;
import com.manzhushaka.iip.domain.IipPointsRule;
import com.manzhushaka.iip.mapper.IipActivityMapper;
import com.manzhushaka.iip.mapper.IipInvoiceMapper;
import com.manzhushaka.iip.mapper.IipMerchantMapper;
import com.manzhushaka.iip.service.IIipInvoiceService;
import com.manzhushaka.iip.service.IIipPointsService;
import com.manzhushaka.iip.service.IIipPointsRuleService;

/**
 * 发票 服务层实现
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipInvoiceServiceImpl implements IIipInvoiceService
{
    /** 发票状态：待审核 */
    private static final String STATUS_PENDING = "0";

    /** 发票状态：已通过 */
    private static final String STATUS_APPROVED = "1";

    /** 发票状态：已驳回 */
    private static final String STATUS_REJECTED = "2";

    /** 积分流水业务来源：发票审核 */
    private static final String BIZ_TYPE_INVOICE_AUDIT = "invoice_audit";

    /** 积分有效期天数（自发放起） */
    private static final long POINTS_VALID_DAYS = 365L;

    /** 一天的毫秒数 */
    private static final long MILLIS_PER_DAY = 24L * 3600 * 1000;

    @Autowired
    private IipInvoiceMapper invoiceMapper;

    @Autowired
    private IipActivityMapper activityMapper;

    @Autowired
    private IipMerchantMapper merchantMapper;

    @Autowired
    private IIipPointsService pointsService;

    @Autowired
    private IIipPointsRuleService pointsRuleService;

    /**
     * 通过ID查询发票
     *
     * @param invoiceId 发票ID
     * @return 发票信息，不存在时返回null
     */
    @Override
    public IipInvoice selectInvoiceById(Long invoiceId)
    {
        return invoiceMapper.selectIipInvoiceById(invoiceId);
    }

    /**
     * 查询发票列表（管理端，支持状态/发票号码/商户名称/上传时间筛选）
     *
     * @param iipInvoice 查询条件
     * @return 发票集合
     */
    @Override
    public List<IipInvoice> selectInvoiceList(IipInvoice iipInvoice)
    {
        return invoiceMapper.selectIipInvoiceList(iipInvoice);
    }

    /**
     * 按用户查询发票列表（小程序端，状态可选，按创建时间倒序）
     *
     * @param memberId 用户ID
     * @param status 状态（0待审核 1已通过 2已驳回），null 或空表示全部
     * @return 发票集合
     */
    @Override
    public List<IipInvoice> selectMemberInvoiceList(Long memberId, String status)
    {
        IipInvoice query = new IipInvoice();
        query.setMemberId(memberId);
        query.setStatus(status);
        return invoiceMapper.selectMemberInvoiceList(query);
    }

    /**
     * 用户提交发票（校验必填与金额，防重复上传，初始状态为待审核）
     *
     * @param iipInvoice 发票信息（memberId 由调用方按登录用户写入）
     * @return 发票ID
     */
    @Override
    @Transactional
    public Long submitInvoice(IipInvoice iipInvoice)
    {
        validateSubmit(iipInvoice);
        String invoiceCode = iipInvoice.getInvoiceCode() == null ? "" : iipInvoice.getInvoiceCode().trim();
        iipInvoice.setInvoiceCode(invoiceCode);
        iipInvoice.setInvoiceNo(iipInvoice.getInvoiceNo().trim());
        iipInvoice.setMerchantName(iipInvoice.getMerchantName().trim());
        iipInvoice.setImageUrl(iipInvoice.getImageUrl().trim());
        if (invoiceMapper.selectByCodeAndNo(invoiceCode, iipInvoice.getInvoiceNo()) != null)
        {
            throw new ServiceException("该发票已上传过");
        }
        iipInvoice.setStatus(STATUS_PENDING);
        iipInvoice.setPoints(0);
        invoiceMapper.insertIipInvoice(iipInvoice);
        return iipInvoice.getInvoiceId();
    }

    /**
     * 审核发票（仅待审核可审；通过时按多活动匹配规则确定积分比例发放积分，无匹配按1:1兜底，驳回必须填写原因）
     *
     * @param invoiceId 发票ID
     * @param pass true 通过，false 驳回
     * @param auditRemark 审核备注（驳回必填原因）
     * @param auditBy 审核人账号
     */
    @Override
    @Transactional
    public void auditInvoice(Long invoiceId, boolean pass, String auditRemark, String auditBy)
    {
        IipInvoice invoice = invoiceMapper.selectIipInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (!STATUS_PENDING.equals(invoice.getStatus()))
        {
            throw new ServiceException("该发票已审核过");
        }
        if (!pass && StringUtils.isBlank(auditRemark))
        {
            throw new ServiceException("驳回时必须填写驳回原因");
        }

        Date now = new Date();
        Integer points = null;
        Long activityId = null;
        Long pointsRuleId = null;
        BigDecimal pointsRatioSnapshot = null;
        String pointsRuleSnapshot = null;
        if (pass)
        {
            PointsActivityMatch match = matchPointsActivity(invoice, now);
            PointsCalculation calculation = calculatePoints(invoice, match, now);
            points = calculation.actualPoints();
            activityId = match.activity() == null ? null : match.activity().getActivityId();
            pointsRuleId = match.rule().getRuleId();
            pointsRatioSnapshot = calculation.ratio();
            pointsRuleSnapshot = calculation.snapshot();
        }

        IipInvoice update = new IipInvoice();
        update.setInvoiceId(invoiceId);
        update.setStatus(pass ? STATUS_APPROVED : STATUS_REJECTED);
        update.setPoints(points);
        update.setActivityId(activityId);
        update.setPointsRuleId(pointsRuleId);
        update.setPointsRatioSnapshot(pointsRatioSnapshot);
        update.setPointsRuleSnapshot(pointsRuleSnapshot);
        update.setAuditBy(auditBy);
        update.setAuditTime(now);
        update.setAuditRemark(auditRemark);
        update.setUpdateBy(auditBy);
        int rows = invoiceMapper.updateAuditStatus(update);
        if (rows == 0)
        {
            throw new ServiceException("该发票已审核过");
        }

        if (pass && points > 0)
        {
            Date expireTime = new Date(System.currentTimeMillis() + POINTS_VALID_DAYS * MILLIS_PER_DAY);
            pointsService.awardPoints(invoice.getMemberId(), points, BIZ_TYPE_INVOICE_AUDIT,
                    String.valueOf(invoiceId), expireTime, "发票审核通过");
        }
    }

    /**
     * 按多活动匹配规则确定发分依据活动：遍历生效活动（已按优先级倒序），
     * 第一个满足「活动 city 为空（全省）或 活动 city 等于商户所在市县」的活动命中；
     * 无商户或商户无市县时只匹配全省活动；均无匹配时返回null按1:1兜底。
     *
     * @param invoice 发票信息
     * @param now 当前时间
     * @return 命中的活动与积分规则；无生效活动或无地域匹配时返回1:1兼容规则
     */
    private PointsActivityMatch matchPointsActivity(IipInvoice invoice, Date now)
    {
        List<IipActivity> activities = activityMapper.selectActiveActivities(now);
        if (activities.isEmpty())
        {
            return new PointsActivityMatch(null, defaultRule());
        }
        String merchantCity = resolveMerchantCity(invoice);
        boolean regionMatched = false;
        for (IipActivity activity : activities)
        {
            boolean matchesRegion = StringUtils.isEmpty(activity.getCity())
                    || (StringUtils.isNotEmpty(merchantCity) && activity.getCity().equals(merchantCity));
            if (!matchesRegion)
            {
                continue;
            }
            regionMatched = true;
            IipPointsRule rule = pointsRuleService.getRule(activity.getActivityId());
            if (pointsRuleService.isMerchantEligible(rule, invoice.getMerchantId()))
            {
                return new PointsActivityMatch(activity, rule);
            }
        }
        if (regionMatched)
        {
            throw new ServiceException("发票商户不在当前积分活动范围");
        }
        return new PointsActivityMatch(null, defaultRule());
    }

    /**
     * 按比例、单张上限和月度剩余额度计算最终积分。
     *
     * @param invoice 发票
     * @param match 活动与规则匹配结果
     * @param now 当前时间
     * @return 积分计算结果
     */
    private PointsCalculation calculatePoints(IipInvoice invoice, PointsActivityMatch match, Date now)
    {
        IipActivity activity = match.activity();
        IipPointsRule rule = match.rule();
        BigDecimal ratio = activity != null && activity.getPointsRatio() != null
                ? activity.getPointsRatio() : BigDecimal.ONE;
        int rawPoints = invoice.getAmount().multiply(ratio).setScale(0, RoundingMode.HALF_UP).intValueExact();
        if (rawPoints <= 0)
        {
            throw new ServiceException("发票金额过低无法发放积分");
        }
        int singleCappedPoints = applyCap(rawPoints, rule.getSingleInvoiceCap());
        int actualPoints = pointsRuleService.reserveMonthlyPoints(rule, invoice.getMemberId(), singleCappedPoints,
                now);
        String snapshot = StringUtils.format("金额={}，比例={}，原始积分={}，单张上限={}，月度上限={}，实际积分={}",
                invoice.getAmount(), ratio, rawPoints, formatCap(rule.getSingleInvoiceCap()),
                formatCap(rule.getMonthlyMemberCap()), actualPoints);
        return new PointsCalculation(ratio, actualPoints, snapshot);
    }

    /**
     * 应用积分上限。
     *
     * @param points 原始积分
     * @param cap 上限，-1表示不限
     * @return 封顶后的积分
     */
    private int applyCap(int points, Integer cap)
    {
        return cap == null || cap == -1 ? points : Math.min(points, cap);
    }

    /**
     * 格式化规则快照中的上限。
     *
     * @param cap 上限
     * @return 上限文案
     */
    private String formatCap(Integer cap)
    {
        return cap == null || cap == -1 ? "不限" : cap.toString();
    }

    /**
     * 构造兼容旧行为的不限额默认规则。
     *
     * @return 默认规则
     */
    private IipPointsRule defaultRule()
    {
        IipPointsRule rule = new IipPointsRule();
        rule.setSingleInvoiceCap(-1);
        rule.setMonthlyMemberCap(-1);
        rule.setMerchantScope("all");
        return rule;
    }

    private record PointsActivityMatch(IipActivity activity, IipPointsRule rule)
    {
    }

    private record PointsCalculation(BigDecimal ratio, int actualPoints, String snapshot)
    {
    }

    /**
     * 查询发票关联商户的所在市县
     *
     * @param invoice 发票信息
     * @return 商户所在市县，无关联商户、商户不存在或商户无市县时返回null
     */
    private String resolveMerchantCity(IipInvoice invoice)
    {
        if (invoice.getMerchantId() == null)
        {
            return null;
        }
        IipMerchant merchant = merchantMapper.selectIipMerchantById(invoice.getMerchantId());
        return merchant == null ? null : merchant.getCity();
    }

    /**
     * 校验用户提交发票的必填字段与金额
     *
     * @param iipInvoice 发票信息
     */
    private void validateSubmit(IipInvoice iipInvoice)
    {
        if (StringUtils.isBlank(iipInvoice.getInvoiceNo()))
        {
            throw new ServiceException("发票号码不能为空");
        }
        if (iipInvoice.getAmount() == null || iipInvoice.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("发票金额必须大于0");
        }
        if (StringUtils.isBlank(iipInvoice.getImageUrl()))
        {
            throw new ServiceException("发票图片不能为空");
        }
        if (StringUtils.isBlank(iipInvoice.getMerchantName()))
        {
            throw new ServiceException("商户名称不能为空");
        }
    }
}
