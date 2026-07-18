package com.manzhushaka.iip.application.points.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.uuid.IdUtils;
import com.manzhushaka.iip.application.points.command.PointsAdjustCommand;
import com.manzhushaka.iip.application.points.query.PointsAccountQuery;
import com.manzhushaka.iip.application.points.query.PointsRecordQuery;
import com.manzhushaka.iip.application.points.result.PointsAccountResult;
import com.manzhushaka.iip.application.points.result.PointsRecordResult;
import com.manzhushaka.iip.application.points.service.PointsAdminAppService;
import com.manzhushaka.iip.domain.IipPointsAccount;
import com.manzhushaka.iip.domain.IipPointsRecord;
import com.manzhushaka.iip.service.IIipPointsQueryService;
import com.manzhushaka.iip.service.IIipPointsService;

/**
 * 积分管理应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class PointsAdminAppServiceImpl implements PointsAdminAppService
{
    /** 业务来源：管理端手工调整 */
    private static final String BIZ_TYPE_ADMIN_ADJUST = "admin_adjust";

    /** 调整单据号随机段长度 */
    private static final int ADJUST_BIZ_ID_SUFFIX_LENGTH = 8;

    @Autowired
    private IIipPointsService pointsService;

    @Autowired
    private IIipPointsQueryService pointsQueryService;

    @Override
    public List<PointsAccountResult> listPointsAccounts(PointsAccountQuery query)
    {
        Long memberId = query == null ? null : query.memberId();
        String nickname = query == null ? null : query.nickname();
        return pointsQueryService.listPointsAccounts(memberId, nickname).stream()
                .map(this::toAccountResult)
                .toList();
    }

    @Override
    public List<PointsRecordResult> listPointsRecords(PointsRecordQuery query)
    {
        return pointsQueryService.listPointsRecords(toEntity(query)).stream()
                .map(this::toRecordResult)
                .toList();
    }

    @Override
    public List<PointsRecordResult> listMemberRecords(Long memberId, String changeType)
    {
        return listPointsRecords(new PointsRecordQuery(memberId, changeType, null, null, null));
    }

    @Override
    @Transactional
    public void adjustPoints(PointsAdjustCommand command)
    {
        if (command == null || command.memberId() == null)
        {
            throw new ServiceException("用户ID不能为空");
        }
        if (command.points() == null || command.points() == 0)
        {
            throw new ServiceException("调整积分不能为0");
        }
        if (StringUtils.isBlank(command.remark()))
        {
            throw new ServiceException("调整备注不能为空");
        }
        String bizId = "ADJ" + IdUtils.fastSimpleUUID().substring(0, ADJUST_BIZ_ID_SUFFIX_LENGTH).toUpperCase();
        if (command.points() > 0)
        {
            pointsService.awardPoints(command.memberId(), command.points(), BIZ_TYPE_ADMIN_ADJUST, bizId,
                    null, command.remark());
            return;
        }

        int deductPoints = -command.points();
        int available = pointsService.getAvailablePoints(command.memberId());
        if (available < deductPoints)
        {
            throw new ServiceException("扣减积分超过账户余额");
        }
        pointsService.consumePoints(command.memberId(), deductPoints, BIZ_TYPE_ADMIN_ADJUST, bizId,
                command.remark());
    }

    private IipPointsRecord toEntity(PointsRecordQuery query)
    {
        IipPointsRecord record = new IipPointsRecord();
        if (query == null)
        {
            return record;
        }
        record.setMemberId(query.memberId());
        record.setChangeType(query.changeType());
        record.setBizType(query.bizType());
        if (query.beginTime() != null)
        {
            record.getParams().put("beginTime", query.beginTime());
        }
        if (query.endTime() != null)
        {
            record.getParams().put("endTime", query.endTime());
        }
        return record;
    }

    private PointsAccountResult toAccountResult(IipPointsAccount account)
    {
        if (account == null)
        {
            return null;
        }
        return new PointsAccountResult(account.getAccountId(), account.getMemberId(), account.getNickname(),
                account.getTotalPoints(), account.getAvailablePoints(), account.getUsedPoints(),
                account.getExpiredPoints(), account.getCreateTime(), account.getUpdateTime(), account.getRemark());
    }

    private PointsRecordResult toRecordResult(IipPointsRecord record)
    {
        if (record == null)
        {
            return null;
        }
        return new PointsRecordResult(record.getRecordId(), record.getMemberId(), record.getChangeType(),
                record.getPoints(), record.getBalanceAfter(), record.getBizType(), record.getBizId(),
                record.getRemaining(), record.getExpireTime(), record.getCreateTime(), record.getRemark());
    }
}
