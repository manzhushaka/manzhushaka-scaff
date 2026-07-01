package com.manzhushaka.system.service.impl;

import java.util.List;

import com.manzhushaka.system.domain.SysSlowSqlLog;
import com.manzhushaka.system.mapper.SysSlowSqlLogMapper;
import com.manzhushaka.system.service.ISysSlowSqlLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 慢 SQL 日志 服务层处理。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Service
public class SysSlowSqlLogServiceImpl implements ISysSlowSqlLogService
{
    @Autowired
    private SysSlowSqlLogMapper slowSqlLogMapper;

    @Override
    public void insertSlowSqlLog(SysSlowSqlLog slowSqlLog)
    {
        slowSqlLogMapper.insertSlowSqlLog(slowSqlLog);
    }

    @Override
    public List<SysSlowSqlLog> selectSlowSqlLogList(SysSlowSqlLog slowSqlLog)
    {
        return slowSqlLogMapper.selectSlowSqlLogList(slowSqlLog);
    }

    @Override
    public SysSlowSqlLog selectSlowSqlLogById(Long slowSqlId)
    {
        return slowSqlLogMapper.selectSlowSqlLogById(slowSqlId);
    }

    @Override
    public int deleteSlowSqlLogByIds(Long[] slowSqlIds)
    {
        return slowSqlLogMapper.deleteSlowSqlLogByIds(slowSqlIds);
    }

    @Override
    public void cleanSlowSqlLog()
    {
        slowSqlLogMapper.cleanSlowSqlLog();
    }
}
