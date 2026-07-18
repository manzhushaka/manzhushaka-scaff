package com.manzhushaka.iip.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.DateUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.iip.domain.IipCoupon;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.domain.IipMerchant;
import com.manzhushaka.iip.mapper.IipCouponMapper;
import com.manzhushaka.iip.mapper.IipCouponRecordMapper;
import com.manzhushaka.iip.mapper.IipMerchantMapper;
import com.manzhushaka.iip.mapper.IipMerchantVerifyMapper;
import com.manzhushaka.iip.service.IIipMerchantService;

/**
 * 商户 服务层实现
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipMerchantServiceImpl implements IIipMerchantService
{
    /** 商户状态：正常 */
    private static final String MERCHANT_STATUS_NORMAL = "0";

    /** 商户状态：停用（审核驳回同用） */
    private static final String MERCHANT_STATUS_DISABLED = "1";

    /** 商户状态：待审核 */
    private static final String MERCHANT_STATUS_PENDING = "2";

    /** 券实例状态：未使用 */
    private static final String RECORD_STATUS_UNUSED = "0";

    /** 商户编号前缀字母 */
    private static final String MERCHANT_NO_LETTER = "M";

    /** 商户编号日期段格式 */
    private static final String MERCHANT_NO_DATE_PATTERN = "yyyyMM";

    /** 商户编号序号长度 */
    private static final int MERCHANT_NO_SEQ_LENGTH = 5;

    /** 商户编号查重循环上限，防止异常情况下死循环 */
    private static final int MERCHANT_NO_MAX_RETRY = 100;

    @Autowired
    private IipMerchantMapper merchantMapper;

    @Autowired
    private IipMerchantVerifyMapper merchantVerifyMapper;

    /** 券域数据只读使用，禁止修改券域 Mapper 文件 */
    @Autowired
    private IipCouponRecordMapper couponRecordMapper;

    /** 券域数据只读使用，用于核销前校验券的指定商户 */
    @Autowired
    private IipCouponMapper couponMapper;

    /** 自代理，用于在核销事务中以独立事务提交过期置位 */
    @Autowired
    @Lazy
    private IIipMerchantService selfProxy;

    /**
     * 查询商户列表
     *
     * @param iipMerchant 查询条件
     * @return 商户集合
     */
    @Override
    public List<IipMerchant> selectIipMerchantList(IipMerchant iipMerchant)
    {
        return merchantMapper.selectIipMerchantList(iipMerchant);
    }

    /**
     * 通过ID查询商户
     *
     * @param merchantId 商户ID
     * @return 商户信息，不存在时返回null
     */
    @Override
    public IipMerchant selectIipMerchantById(Long merchantId)
    {
        return merchantMapper.selectIipMerchantById(merchantId);
    }

    /**
     * 按绑定用户ID查询商户（任意状态）
     *
     * @param memberId 用户ID
     * @return 商户信息，不存在时返回null
     */
    @Override
    public IipMerchant selectMerchantByMemberId(Long memberId)
    {
        return merchantMapper.selectByMemberId(memberId);
    }

    /**
     * 查询当前用户名下可核销商户（必须存在且 status='0' 正常）
     *
     * @param memberId 用户ID
     * @return 可核销商户信息
     */
    @Override
    public IipMerchant getVerifiableMerchant(Long memberId)
    {
        IipMerchant merchant = merchantMapper.selectByMemberId(memberId);
        if (merchant == null || !MERCHANT_STATUS_NORMAL.equals(merchant.getStatus()))
        {
            throw new ServiceException("非商户账号或商户未通过审核");
        }
        return merchant;
    }

    /**
     * 新增商户（管理端）
     *
     * @param iipMerchant 商户信息
     * @return 影响行数
     */
    @Override
    @Transactional
    public int insertIipMerchant(IipMerchant iipMerchant)
    {
        if (StringUtils.isBlank(iipMerchant.getMerchantNo()))
        {
            iipMerchant.setMerchantNo(generateMerchantNo());
        }
        if (StringUtils.isBlank(iipMerchant.getStatus()))
        {
            iipMerchant.setStatus(MERCHANT_STATUS_NORMAL);
        }
        return merchantMapper.insertIipMerchant(iipMerchant);
    }

    /**
     * 修改商户（管理端）
     *
     * @param iipMerchant 商户信息
     * @return 影响行数
     */
    @Override
    @Transactional
    public int updateIipMerchant(IipMerchant iipMerchant)
    {
        return merchantMapper.updateIipMerchant(iipMerchant);
    }

    /**
     * 批量删除商户（管理端），不影响已发放券实例与历史核销记录
     *
     * @param merchantIds 需要删除的商户ID
     * @return 影响行数
     */
    @Override
    @Transactional
    public int deleteIipMerchantByIds(Long[] merchantIds)
    {
        return merchantMapper.deleteIipMerchantByIds(merchantIds);
    }

    /**
     * 审核商户（仅待审核可审，SQL 条件守护幂等）
     *
     * @param merchantId 商户ID
     * @param approve true 通过，false 驳回
     * @param auditRemark 审核备注
     * @param auditBy 审核人账号
     * @return 影响行数，0 表示商户不存在或不处于待审核状态
     */
    @Override
    @Transactional
    public int auditMerchant(Long merchantId, boolean approve, String auditRemark, String auditBy)
    {
        String targetStatus = approve ? MERCHANT_STATUS_NORMAL : MERCHANT_STATUS_DISABLED;
        String remark = auditRemark == null ? "" : auditRemark;
        return merchantMapper.updateAuditStatus(merchantId, targetStatus, auditBy, remark);
    }

    /**
     * 小程序商户入驻申请
     *
     * @param iipMerchant 申请信息
     * @return 新商户ID
     */
    @Override
    @Transactional
    public Long applyMerchant(IipMerchant iipMerchant)
    {
        if (iipMerchant.getMemberId() == null)
        {
            throw new ServiceException("入驻申请缺少绑定用户信息");
        }
        IipMerchant existing = merchantMapper.selectByMemberId(iipMerchant.getMemberId());
        if (existing != null)
        {
            throw new ServiceException("已申请过商户入驻，请勿重复提交");
        }
        iipMerchant.setMerchantNo(generateMerchantNo());
        iipMerchant.setStatus(MERCHANT_STATUS_PENDING);
        merchantMapper.insertIipMerchant(iipMerchant);
        return iipMerchant.getMerchantId();
    }

    /**
     * 小程序商户核销券
     *
     * @param memberId 当前用户ID
     * @param verifyCode 核销码
     * @param verifyBy 核销操作人
     * @return 核销成功的券实例
     */
    @Override
    @Transactional
    public IipCouponRecord verifyCoupon(Long memberId, String verifyCode, String verifyBy)
    {
        IipMerchant merchant = getVerifiableMerchant(memberId);
        IipCouponRecord record = couponRecordMapper.selectByVerifyCode(verifyCode);
        if (record == null)
        {
            throw new ServiceException("核销码无效");
        }
        if (!RECORD_STATUS_UNUSED.equals(record.getStatus()))
        {
            throw new ServiceException("券已使用或已过期");
        }
        if (record.getValidEndTime() != null && record.getValidEndTime().before(DateUtils.getNowDate()))
        {
            // 独立事务先提交过期置位，再抛出业务异常，避免外层事务回滚导致状态未落库
            selfProxy.markCouponRecordExpired(record.getRecordId());
            throw new ServiceException("券已过期");
        }
        IipCoupon coupon = couponMapper.selectIipCouponById(record.getCouponId());
        if (coupon != null && coupon.getMerchantId() != null
                && !coupon.getMerchantId().equals(merchant.getMerchantId()))
        {
            throw new ServiceException("本券不可在贵店核销");
        }
        int rows = merchantVerifyMapper.verifyCouponRecordAtomic(record.getRecordId(), merchant.getMerchantId(),
                verifyBy);
        if (rows == 0)
        {
            throw new ServiceException("券状态已变化，请重试");
        }
        return record;
    }

    /**
     * 将未使用券实例置为已过期（独立事务提交）
     *
     * @param recordId 券实例ID
     * @return 影响行数
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int markCouponRecordExpired(Long recordId)
    {
        return merchantVerifyMapper.markCouponRecordExpired(recordId);
    }

    /**
     * 查询指定商户的核销记录（按核销时间倒序）
     *
     * @param merchantId 商户ID
     * @return 核销记录集合
     */
    @Override
    public List<IipCouponRecord> selectVerifyRecordsByMerchantId(Long merchantId)
    {
        return merchantVerifyMapper.selectVerifyRecordsByMerchantId(merchantId);
    }

    /**
     * 生成商户编号（M+yyyyMM+5位序号）。
     *
     * 以当月最大编号推算下一序号，并逐个查重循环，保证编号唯一；
     * 数据库 merchant_no 唯一索引为最终兜底。
     *
     * @return 唯一商户编号
     */
    private String generateMerchantNo()
    {
        String prefix = MERCHANT_NO_LETTER + DateUtils.dateTimeNow(MERCHANT_NO_DATE_PATTERN);
        int seq = 1;
        String maxNo = merchantMapper.selectMaxMerchantNo(prefix);
        if (StringUtils.isNotEmpty(maxNo) && maxNo.length() == prefix.length() + MERCHANT_NO_SEQ_LENGTH)
        {
            seq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
        }
        for (int i = 0; i < MERCHANT_NO_MAX_RETRY; i++)
        {
            String candidate = prefix + String.format("%0" + MERCHANT_NO_SEQ_LENGTH + "d", seq + i);
            if (merchantMapper.selectByMerchantNo(candidate) == null)
            {
                return candidate;
            }
        }
        throw new ServiceException("商户编号生成失败，请重试");
    }
}
