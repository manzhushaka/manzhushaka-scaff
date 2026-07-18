package com.manzhushaka.iip.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.domain.IipPointsAccount;
import com.manzhushaka.iip.domain.IipPointsRecord;
import com.manzhushaka.iip.mapper.IipPointsAccountMapper;
import com.manzhushaka.iip.mapper.IipPointsRecordMapper;
import com.manzhushaka.iip.service.IIipPointsService;

/**
 * 积分跨域契约 服务实现
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipPointsServiceImpl implements IIipPointsService
{
    /** 变动类型：获得 */
    private static final String CHANGE_TYPE_EARN = "earn";

    /** 变动类型：消费 */
    private static final String CHANGE_TYPE_CONSUME = "consume";

    @Autowired
    private IipPointsAccountMapper pointsAccountMapper;

    @Autowired
    private IipPointsRecordMapper pointsRecordMapper;

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
    @Override
    @Transactional
    public int awardPoints(Long memberId, Integer points, String bizType, String bizId, Date expireTime, String remark)
    {
        if (memberId == null || points == null || points <= 0)
        {
            throw new ServiceException("发放积分参数不合法");
        }
        IipPointsRecord existRecord = pointsRecordMapper.selectByBiz(bizType, bizId);
        if (existRecord != null && CHANGE_TYPE_EARN.equals(existRecord.getChangeType()))
        {
            return 0;
        }
        IipPointsAccount account = pointsAccountMapper.selectByMemberId(memberId);
        if (account == null)
        {
            account = new IipPointsAccount();
            account.setMemberId(memberId);
            account.setTotalPoints(0);
            account.setAvailablePoints(0);
            account.setUsedPoints(0);
            account.setExpiredPoints(0);
            pointsAccountMapper.insertIipPointsAccount(account);
        }
        int rows = pointsAccountMapper.incrAvailable(memberId, points);
        if (rows == 0)
        {
            throw new ServiceException("积分账户不存在");
        }
        IipPointsRecord record = new IipPointsRecord();
        record.setMemberId(memberId);
        record.setChangeType(CHANGE_TYPE_EARN);
        record.setPoints(points);
        record.setBalanceAfter(getAvailablePoints(memberId));
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setRemaining(points);
        record.setExpireTime(expireTime);
        record.setRemark(remark);
        pointsRecordMapper.insertIipPointsRecord(record);
        return points;
    }

    /**
     * 消费积分（FIFO 扣减 earn 批次；余额不足抛 ServiceException）
     * 
     * @param memberId 用户ID
     * @param points 消费数量（正数）
     * @param bizType 业务来源（如 coupon_exchange）
     * @param bizId 业务单据ID
     * @param remark 备注
     * @return 实际消费积分
     */
    @Override
    @Transactional
    public int consumePoints(Long memberId, Integer points, String bizType, String bizId, String remark)
    {
        if (memberId == null || points == null || points <= 0)
        {
            throw new ServiceException("消费积分参数不合法");
        }
        int available = getAvailablePoints(memberId);
        if (available < points)
        {
            throw new ServiceException("积分不足");
        }
        int need = points;
        List<IipPointsRecord> fifoList = pointsRecordMapper.selectEarnFifoList(memberId);
        for (IipPointsRecord batch : fifoList)
        {
            if (need <= 0)
            {
                break;
            }
            int use = Math.min(batch.getRemaining(), need);
            int rows = pointsRecordMapper.decrRemaining(batch.getRecordId(), use);
            if (rows == 0)
            {
                throw new ServiceException("积分扣减冲突，请重试");
            }
            need -= use;
        }
        if (need > 0)
        {
            throw new ServiceException("积分不足");
        }
        int rows = pointsAccountMapper.incrUsed(memberId, points);
        if (rows == 0)
        {
            throw new ServiceException("积分不足");
        }
        IipPointsRecord record = new IipPointsRecord();
        record.setMemberId(memberId);
        record.setChangeType(CHANGE_TYPE_CONSUME);
        record.setPoints(points);
        record.setBalanceAfter(getAvailablePoints(memberId));
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setRemark(remark);
        pointsRecordMapper.insertIipPointsRecord(record);
        return points;
    }

    /**
     * 查询可用积分，无账户返回 0
     * 
     * @param memberId 用户ID
     * @return 可用积分数
     */
    @Override
    public int getAvailablePoints(Long memberId)
    {
        if (memberId == null)
        {
            return 0;
        }
        IipPointsAccount account = pointsAccountMapper.selectByMemberId(memberId);
        return account == null || account.getAvailablePoints() == null ? 0 : account.getAvailablePoints();
    }
}
