package com.manzhushaka.iip.service;

import java.util.List;
import com.manzhushaka.iip.application.merchant.result.MerchantVerifyStatsResult;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.domain.IipMerchant;

/**
 * 商户 服务层。
 *
 * 承载商户 CRUD、merchant_no 生成、入驻申请、审核与小程序端券核销等商户域业务规则。
 * 商户状态变更与删除不影响已发放的券实例。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IIipMerchantService
{
    /**
     * 查询商户列表
     *
     * @param iipMerchant 查询条件
     * @return 商户集合
     */
    public List<IipMerchant> selectIipMerchantList(IipMerchant iipMerchant);

    /**
     * 通过ID查询商户
     *
     * @param merchantId 商户ID
     * @return 商户信息，不存在时返回null
     */
    public IipMerchant selectIipMerchantById(Long merchantId);

    /**
     * 按绑定用户ID查询商户（任意状态，小程序"我的商户"使用）
     *
     * @param memberId 用户ID
     * @return 商户信息，不存在时返回null
     */
    public IipMerchant selectMerchantByMemberId(Long memberId);

    /**
     * 查询当前用户名下可核销商户（必须存在且 status='0' 正常）
     *
     * @param memberId 用户ID
     * @return 可核销商户信息
     * @throws com.manzhushaka.common.exception.ServiceException 非商户账号或商户未通过审核时抛出
     */
    public IipMerchant getVerifiableMerchant(Long memberId);

    /**
     * 新增商户（管理端）。
     *
     * merchant_no 为空时自动生成（M+yyyyMM+5位序号，查重循环保证唯一）；
     * status 为空时默认 '0' 正常（管理端直接创建无需审核）。
     *
     * @param iipMerchant 商户信息
     * @return 影响行数
     */
    public int insertIipMerchant(IipMerchant iipMerchant);

    /**
     * 修改商户（管理端）。
     *
     * merchant_no 生成后不可变，本方法不更新审核字段。
     *
     * @param iipMerchant 商户信息
     * @return 影响行数
     */
    public int updateIipMerchant(IipMerchant iipMerchant);

    /**
     * 批量删除商户（管理端）。
     *
     * 仅删除商户主体，不影响已发放的券实例与历史核销记录。
     *
     * @param merchantIds 需要删除的商户ID
     * @return 影响行数
     */
    public int deleteIipMerchantByIds(Long[] merchantIds);

    /**
     * 审核商户。
     *
     * 仅 status='2' 待审核商户可审；通过置 '0' 正常，驳回置 '1' 停用并记录审核备注；
     * 记录审核人与审核时间；SQL 携带 status='2' 条件守护，重复审核不会重复生效。
     *
     * @param merchantId 商户ID
     * @param approve true 通过，false 驳回
     * @param auditRemark 审核备注（驳回时必填，由调用方校验；无备注传null按空串落库）
     * @param auditBy 审核人账号
     * @return 影响行数，0 表示商户不存在或不处于待审核状态
     */
    public int auditMerchant(Long merchantId, boolean approve, String auditRemark, String auditBy);

    /**
     * 小程序商户入驻申请。
     *
     * 当前用户已绑定商户（任意状态）时抛出 ServiceException；
     * 插入商户 status='2' 待审核，member_id 为当前用户，merchant_no 自动生成。
     *
     * @param iipMerchant 申请信息（merchantName/category/contactName/contactPhone/address/businessLicense 必填，memberId 必填）
     * @return 新商户ID
     * @throws com.manzhushaka.common.exception.ServiceException 重复申请或缺少绑定用户时抛出
     */
    public Long applyMerchant(IipMerchant iipMerchant);

    /**
     * 小程序商户核销券。
     *
     * 流程：校验本用户商户存在且 status='0'；按核销码查券实例；校验未使用、未过期、
     * 券指定商户时必须为本商户；最后以 status='0' 条件原子更新为已使用。
     * 发现已过期但未置位的券，先以独立事务将其状态置为 '2' 已过期，再抛出"券已过期"。
     *
     * @param memberId 当前用户ID
     * @param verifyCode 核销码
     * @param verifyBy 核销操作人
     * @return 核销成功的券实例（核销前快照，verifyTime 等核销字段以落库值为准）
     * @throws com.manzhushaka.common.exception.ServiceException 各类校验失败时抛出
     */
    public IipCouponRecord verifyCoupon(Long memberId, String verifyCode, String verifyBy);

    /**
     * 将未使用券实例置为已过期（独立事务）。
     *
     * 供核销流程在抛出"券已过期"前独立提交置位，避免外层事务回滚导致状态未落库。
     * 必须通过 Spring 代理调用，类内自调用不会开启新事务。
     *
     * @param recordId 券实例ID
     * @return 影响行数
     */
    public int markCouponRecordExpired(Long recordId);

    /**
     * 查询指定商户的核销记录（按核销时间倒序）
     *
     * @param merchantId 商户ID
     * @param days 最近天数（可空，如 1/7/30 表示最近 N 天；null 为全部）
     * @return 核销记录集合
     */
    public List<IipCouponRecord> selectVerifyRecordsByMerchantId(Long merchantId, Integer days);

    /**
     * 统计指定商户的核销数据（仅已核销 status='1' 记录）
     *
     * @param merchantId 商户ID
     * @return 核销统计（今日笔数/今日积分/累计笔数/累计积分）
     */
    public MerchantVerifyStatsResult getVerifyStatsByMerchantId(Long merchantId);
}
