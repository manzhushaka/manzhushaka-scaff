package com.manzhushaka.iip.application.merchant.service;

import java.util.List;
import com.manzhushaka.iip.application.merchant.command.AuditMerchantCommand;
import com.manzhushaka.iip.application.merchant.command.MerchantApplyCommand;
import com.manzhushaka.iip.application.merchant.command.MerchantVerifyCommand;
import com.manzhushaka.iip.application.merchant.command.SaveMerchantCommand;
import com.manzhushaka.iip.application.merchant.query.MerchantQuery;
import com.manzhushaka.iip.application.merchant.result.MerchantResult;
import com.manzhushaka.iip.application.merchant.result.MerchantVerifyResult;
import com.manzhushaka.iip.application.merchant.result.MerchantVerifyStatsResult;
import com.manzhushaka.iip.application.merchant.result.VerifyRecordResult;

/**
 * 商户应用服务（管理端与小程序端共用编排入口）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface MerchantAppService
{
    /**
     * 查询商户列表（管理端）。
     *
     * @param query 查询条件
     * @return 商户列表
     */
    List<MerchantResult> listMerchants(MerchantQuery query);

    /**
     * 查询商户详情（管理端）。
     *
     * @param merchantId 商户ID
     * @return 商户详情，不存在时返回null
     */
    MerchantResult getMerchant(Long merchantId);

    /**
     * 新增商户（管理端）。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createMerchant(SaveMerchantCommand command, String operatorUsername);

    /**
     * 修改商户（管理端）。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateMerchant(SaveMerchantCommand command, String operatorUsername);

    /**
     * 批量删除商户（管理端），不影响已发放券实例与历史核销记录。
     *
     * @param merchantIds 商户ID数组
     */
    void deleteMerchants(Long[] merchantIds);

    /**
     * 审核商户（管理端）。
     *
     * @param command 审核命令（approve=true 通过，false 驳回；驳回时 auditRemark 必填）
     * @param operatorUsername 审核人账号
     */
    void auditMerchant(AuditMerchantCommand command, String operatorUsername);

    /**
     * 小程序商户入驻申请。
     *
     * @param command 申请命令
     * @param memberId 当前用户ID
     * @return 新商户ID
     */
    Long applyMerchant(MerchantApplyCommand command, Long memberId);

    /**
     * 查询当前用户绑定的商户（小程序"我的商户"，任意状态）。
     *
     * @param memberId 当前用户ID
     * @return 商户信息，未申请过时返回null
     */
    MerchantResult getMyMerchant(Long memberId);

    /**
     * 查询当前用户名下可核销商户（必须存在且状态正常，小程序核销相关接口前置校验）。
     *
     * @param memberId 当前用户ID
     * @return 可核销商户信息
     */
    MerchantResult getVerifiableMerchant(Long memberId);

    /**
     * 小程序商户核销券。
     *
     * @param command 核销命令
     * @param memberId 当前用户ID
     * @param operatorName 核销操作人账号
     * @return 核销成功结果
     */
    MerchantVerifyResult verifyCoupon(MerchantVerifyCommand command, Long memberId, String operatorName);

    /**
     * 查询指定商户的核销记录（小程序，按核销时间倒序）。
     *
     * @param merchantId 商户ID
     * @param days 最近天数（可空，1/7/30 语义为最近 N 天；null 为全部）
     * @return 核销记录列表
     */
    List<VerifyRecordResult> listVerifyRecords(Long merchantId, Integer days);

    /**
     * 统计指定商户的核销工作台数据（小程序，仅已核销记录）。
     *
     * @param merchantId 商户ID
     * @return 核销统计（今日笔数/今日积分/累计笔数/累计积分）
     */
    MerchantVerifyStatsResult getVerifyStats(Long merchantId);
}
