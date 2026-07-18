package com.manzhushaka.iip.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.manzhushaka.iip.domain.IipPointsAccount;
import com.manzhushaka.iip.domain.IipPointsRecord;
import com.manzhushaka.iip.mapper.IipPointsAccountMapper;
import com.manzhushaka.iip.mapper.IipPointsRecordMapper;
import com.manzhushaka.iip.service.IIipPointsQueryService;

/**
 * 积分查询 服务实现（管理端账户与流水列表）
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipPointsQueryServiceImpl implements IIipPointsQueryService
{
    @Autowired
    private IipPointsAccountMapper pointsAccountMapper;

    @Autowired
    private IipPointsRecordMapper pointsRecordMapper;

    /**
     * 查询积分账户列表（关联用户昵称）
     * 
     * @param memberId 用户ID，null 不限制
     * @param nickname 昵称关键字，null 或空不限制
     * @return 积分账户集合（含用户昵称）
     */
    @Override
    public List<IipPointsAccount> listPointsAccounts(Long memberId, String nickname)
    {
        return pointsAccountMapper.selectAccountPageList(memberId, nickname);
    }

    /**
     * 查询积分流水列表
     * 
     * @param query 查询条件（memberId/changeType/bizType 与时间范围 params）
     * @return 积分流水集合
     */
    @Override
    public List<IipPointsRecord> listPointsRecords(IipPointsRecord query)
    {
        return pointsRecordMapper.selectIipPointsRecordList(query);
    }
}
