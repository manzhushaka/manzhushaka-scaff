package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.db.system.entity.SysLoginLog;
import com.manzhushaka.db.system.entity.SysOpLog;
import com.manzhushaka.db.system.mapper.SysLoginLogMapper;
import com.manzhushaka.db.system.mapper.SysOpLogMapper;
import com.manzhushaka.system.dto.log.LoginLogQuery;
import com.manzhushaka.system.dto.log.OpLogQuery;
import com.manzhushaka.system.service.LogQueryService;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.LoginLogVO;
import com.manzhushaka.system.vo.log.OpLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LogQueryServiceImpl implements LogQueryService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysOpLogMapper opLogMapper;

    public LogQueryServiceImpl(SysLoginLogMapper loginLogMapper, SysOpLogMapper opLogMapper) {
        this.loginLogMapper = loginLogMapper;
        this.opLogMapper = opLogMapper;
    }

    @Override
    public PageResult<LoginLogVO> pageLoginLogs(LoginLogQuery query) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
            .like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
            .eq(StringUtils.hasText(query.getLoginStatus()), SysLoginLog::getLoginStatus, query.getLoginStatus())
            .orderByDesc(SysLoginLog::getCreateTime, SysLoginLog::getId);
        Page<SysLoginLog> page = loginLogMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toLoginLogVO);
    }

    @Override
    public PageResult<OpLogVO> pageOpLogs(OpLogQuery query) {
        LambdaQueryWrapper<SysOpLog> wrapper = new LambdaQueryWrapper<SysOpLog>()
            .like(StringUtils.hasText(query.getModule()), SysOpLog::getModule, query.getModule())
            .like(StringUtils.hasText(query.getAction()), SysOpLog::getAction, query.getAction())
            .like(StringUtils.hasText(query.getOperatorName()), SysOpLog::getOperatorName, query.getOperatorName())
            .eq(query.getSuccess() != null, SysOpLog::getSuccess, query.getSuccess())
            .orderByDesc(SysOpLog::getCreateTime, SysOpLog::getId);
        Page<SysOpLog> page = opLogMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toOpLogVO);
    }

    private LoginLogVO toLoginLogVO(SysLoginLog entity) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setLoginStatus(entity.getLoginStatus());
        vo.setIp(entity.getIp());
        vo.setUserAgent(entity.getUserAgent());
        vo.setMessage(entity.getMessage());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    private OpLogVO toOpLogVO(SysOpLog entity) {
        OpLogVO vo = new OpLogVO();
        vo.setId(entity.getId());
        vo.setTraceId(entity.getTraceId());
        vo.setModule(entity.getModule());
        vo.setAction(entity.getAction());
        vo.setBusinessType(entity.getBusinessType());
        vo.setRequestUri(entity.getRequestUri());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setOperatorId(entity.getOperatorId());
        vo.setOperatorName(entity.getOperatorName());
        vo.setCostMs(entity.getCostMs());
        vo.setSuccess(entity.getSuccess());
        vo.setErrorMsg(entity.getErrorMsg());
        vo.setRequestSnapshot(entity.getRequestSnapshot());
        vo.setResponseSnapshot(entity.getResponseSnapshot());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
