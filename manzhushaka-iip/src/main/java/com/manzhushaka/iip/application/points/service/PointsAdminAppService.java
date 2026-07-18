package com.manzhushaka.iip.application.points.service;

import java.util.List;
import com.manzhushaka.iip.application.points.command.PointsAdjustCommand;
import com.manzhushaka.iip.application.points.query.PointsAccountQuery;
import com.manzhushaka.iip.application.points.query.PointsRecordQuery;
import com.manzhushaka.iip.application.points.result.PointsAccountResult;
import com.manzhushaka.iip.application.points.result.PointsRecordResult;

/**
 * 积分管理应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface PointsAdminAppService
{
    /**
     * 查询积分账户列表。
     *
     * @param query 查询条件
     * @return 积分账户列表
     */
    List<PointsAccountResult> listPointsAccounts(PointsAccountQuery query);

    /**
     * 查询积分流水列表。
     *
     * @param query 查询条件
     * @return 积分流水列表
     */
    List<PointsRecordResult> listPointsRecords(PointsRecordQuery query);

    /**
     * 查询小程序当前用户的积分明细（仅本人数据，按创建时间倒序）。
     *
     * @param memberId 用户ID（由登录 token 解析，不允许入参指定他人）
     * @param changeType 变动类型（earn/consume/expire/adjust），为 null 或空表示不过滤
     * @return 积分明细列表
     */
    List<PointsRecordResult> listMemberRecords(Long memberId, String changeType);

    /**
     * 手工调整积分（正数发放、负数扣减）。
     *
     * @param command 调整命令
     */
    void adjustPoints(PointsAdjustCommand command);
}
