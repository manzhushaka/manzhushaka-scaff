package com.manzhushaka.iip.application.service;

import java.util.List;
import com.manzhushaka.iip.application.command.ActivityMerchantCommand;
import com.manzhushaka.iip.application.command.SaveActivityCommand;
import com.manzhushaka.iip.application.command.SaveActivityCouponCommand;
import com.manzhushaka.iip.application.query.ActivityQuery;
import com.manzhushaka.iip.application.result.activity.ActiveActivityResult;
import com.manzhushaka.iip.application.result.activity.ActivityCouponResult;
import com.manzhushaka.iip.application.result.activity.ActivityMerchantResult;
import com.manzhushaka.iip.application.result.activity.ActivityResult;
import com.manzhushaka.iip.application.result.activity.CurrentActivityResult;

/**
 * 活动应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface ActivityAppService
{
    /**
     * 查询活动列表。
     *
     * @param query 查询条件
     * @return 活动列表
     */
    List<ActivityResult> listActivities(ActivityQuery query);

    /**
     * 查询活动详情。
     *
     * @param activityId 活动ID
     * @return 活动详情，不存在时返回null
     */
    ActivityResult getActivity(Long activityId);

    /**
     * 新增活动。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createActivity(SaveActivityCommand command, String operatorUsername);

    /**
     * 修改活动。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateActivity(SaveActivityCommand command, String operatorUsername);

    /**
     * 批量删除活动（级联删除活动商户与活动券配置，进行中的活动禁止删除）。
     *
     * @param activityIds 活动ID数组
     */
    void deleteActivities(Long[] activityIds);

    /**
     * 查询活动已配置商户列表（join iip_merchant 名称/类别/状态）。
     *
     * @param activityId 活动ID
     * @return 活动商户配置列表
     */
    List<ActivityMerchantResult> listActivityMerchants(Long activityId);

    /**
     * 新增活动商户配置。
     *
     * @param command 配置命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int addActivityMerchant(ActivityMerchantCommand command, String operatorUsername);

    /**
     * 删除活动商户配置。
     *
     * @param id 配置主键ID
     * @return 影响行数
     */
    int removeActivityMerchant(Long id);

    /**
     * 查询活动已配置券列表（join iip_coupon 名称/库存/已发数量）。
     *
     * @param activityId 活动ID
     * @return 活动券配置列表
     */
    List<ActivityCouponResult> listActivityCoupons(Long activityId);

    /**
     * 新增活动券配置。
     *
     * @param command 配置命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int addActivityCoupon(SaveActivityCouponCommand command, String operatorUsername);

    /**
     * 修改活动券配置发行上限。
     *
     * @param command 配置命令（仅需主键ID与发行上限）
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateActivityCoupon(SaveActivityCouponCommand command, String operatorUsername);

    /**
     * 删除活动券配置。
     *
     * @param id 配置主键ID
     * @return 影响行数
     */
    int removeActivityCoupon(Long id);

    /**
     * 查询当前生效活动（小程序端：活动全字段 + 参与商户数 + 配置券数 + 券列表）。
     *
     * @return 当前活动，不存在时返回null
     */
    CurrentActivityResult getCurrentActivity();

    /**
     * 查询全部生效活动（小程序端：启用且在时间窗内，按优先级与开始时间倒序，含参与商户数与配置券数）。
     *
     * @return 生效活动列表，无生效活动时返回空列表
     */
    List<ActiveActivityResult> listActiveActivities();
}
