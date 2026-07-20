package com.manzhushaka.iip.service;

import java.util.Date;

/**
 * 积分跨域契约 服务层（骨架阶段只定义签名，实现归积分域代理）
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IIipPointsService
{
    /**
     * 发放积分（幂等：bizType+bizId 已存在 earn 流水则直接返回 0）
     * 
     * @param memberId 用户ID
     * @param points 发放数量（正数）
     * @param bizType 业务来源（如 invoice_audit、admin_adjust）
     * @param bizId 业务单据ID（幂等键）
     * @param expireTime 批次过期时间，null 表示不过期
     * @param remark 备注
     * @return 实际发放积分，幂等命中时返回 0
     */
    public int awardPoints(Long memberId, Integer points, String bizType, String bizId, Date expireTime, String remark);

    /**
     * 消费积分（FIFO 扣减 earn 批次；余额不足抛 ServiceException）
     * 
     * @param memberId 用户ID
     * @param points 消费数量（正数）
     * @param bizType 业务来源（如 coupon_exchange）
     * @param bizId 业务单据ID
     * @param remark 备注
     * @return 实际消费积分
     * @throws com.manzhushaka.common.exception.ServiceException 余额不足或账户不存在时抛出
     */
    public int consumePoints(Long memberId, Integer points, String bizType, String bizId, String remark);

    /**
     * 退回已消费积分，恢复可用余额并减少已使用积分；退款批次可继续按 FIFO 消费。
     *
     * @param memberId 用户ID
     * @param points 退回数量（正数）
     * @param bizType 退款业务来源
     * @param bizId 退款业务单据ID
     * @param expireTime 退款积分过期时间
     * @param remark 备注
     * @return 实际退回积分，幂等命中时返回0
     */
    public int refundConsumedPoints(Long memberId, Integer points, String bizType, String bizId,
            Date expireTime, String remark);

    /**
     * 查询可用积分，无账户返回 0
     * 
     * @param memberId 用户ID
     * @return 可用积分数
     */
    public int getAvailablePoints(Long memberId);
}
