package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.db.system.entity.SysLoginLog;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.entity.SysOpLog;
import com.manzhushaka.db.system.mapper.SysLoginLogMapper;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.db.system.mapper.SysOpLogMapper;
import com.manzhushaka.mq.properties.MqProperties;
import com.manzhushaka.system.dto.log.LoginLogQuery;
import com.manzhushaka.system.dto.log.MqMessageQuery;
import com.manzhushaka.system.dto.log.OpLogQuery;
import com.manzhushaka.system.service.LogQueryService;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.LoginLogVO;
import com.manzhushaka.system.vo.log.MqMessageVO;
import com.manzhushaka.system.vo.log.OpLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class LogQueryServiceImpl implements LogQueryService {

    private final SysLoginLogMapper loginLogMapper;
    private final SysOpLogMapper opLogMapper;
    private final SysMqMessageMapper mqMessageMapper;
    private final MqProperties mqProperties;

    public LogQueryServiceImpl(
        SysLoginLogMapper loginLogMapper,
        SysOpLogMapper opLogMapper,
        SysMqMessageMapper mqMessageMapper,
        MqProperties mqProperties
    ) {
        this.loginLogMapper = loginLogMapper;
        this.opLogMapper = opLogMapper;
        this.mqMessageMapper = mqMessageMapper;
        this.mqProperties = mqProperties;
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

    @Override
    public PageResult<MqMessageVO> pageMqMessages(MqMessageQuery query) {
        LambdaQueryWrapper<SysMqMessage> wrapper = new LambdaQueryWrapper<SysMqMessage>();
        if (usesSharedKeyword(query)) {
            String keyword = query.getStreamKey();
            wrapper.and(nested -> nested
                .like(SysMqMessage::getStreamKey, keyword)
                .or()
                .like(SysMqMessage::getEventType, keyword)
                .or()
                .like(SysMqMessage::getBizKey, keyword)
                .or()
                .like(SysMqMessage::getTraceId, keyword));
        } else {
            wrapper
                .like(StringUtils.hasText(query.getStreamKey()), SysMqMessage::getStreamKey, query.getStreamKey())
                .like(StringUtils.hasText(query.getEventType()), SysMqMessage::getEventType, query.getEventType())
                .like(StringUtils.hasText(query.getBizKey()), SysMqMessage::getBizKey, query.getBizKey())
                .like(StringUtils.hasText(query.getTraceId()), SysMqMessage::getTraceId, query.getTraceId());
        }
        wrapper
            .eq(StringUtils.hasText(query.getStatus()), SysMqMessage::getStatus, query.getStatus())
            .like(StringUtils.hasText(query.getSource()), SysMqMessage::getSource, query.getSource())
            .orderByDesc(SysMqMessage::getCreateTime, SysMqMessage::getId);
        Page<SysMqMessage> page = mqMessageMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toMqMessageVO);
    }

    private boolean usesSharedKeyword(MqMessageQuery query) {
        if (!StringUtils.hasText(query.getStreamKey())) {
            return false;
        }
        return query.getStreamKey().equals(query.getEventType())
            && query.getStreamKey().equals(query.getBizKey())
            && query.getStreamKey().equals(query.getTraceId());
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

    private MqMessageVO toMqMessageVO(SysMqMessage entity) {
        MqMessageVO vo = new MqMessageVO();
        vo.setId(entity.getId());
        vo.setEventId(entity.getEventId());
        vo.setStreamKey(entity.getStreamKey());
        vo.setEventType(entity.getEventType());
        vo.setBizKey(entity.getBizKey());
        vo.setTraceId(entity.getTraceId());
        vo.setSource(entity.getSource());
        vo.setStatus(entity.getStatus());
        vo.setRetryCount(entity.getRetryCount());
        vo.setLastError(entity.getLastError());
        vo.setProcessingDeadlineAt(entity.getProcessingDeadlineAt());
        vo.setProcessingTimedOut(isProcessingTimedOut(entity, LocalDateTime.now()));
        vo.setPublishedAt(entity.getPublishedAt());
        vo.setConsumeStartedAt(entity.getConsumeStartedAt());
        vo.setConsumedAt(entity.getConsumedAt());
        vo.setCreateTime(entity.getCreateTime());
        vo.setPayloadSnapshot(entity.getPayloadSnapshot());
        return vo;
    }

    private boolean isProcessingTimedOut(SysMqMessage entity, LocalDateTime now) {
        MqMessageStatus status = resolveStatus(entity.getStatus());
        if (status == null) {
            return false;
        }
        return switch (status) {
            case PROCESSING -> entity.getProcessingDeadlineAt() != null
                && !entity.getProcessingDeadlineAt().isAfter(now);
            case PUBLISHED -> entity.getConsumeStartedAt() == null
                && entity.getPublishedAt() != null
                && !entity.getPublishedAt()
                    .plusSeconds(mqProperties.getProcessingTimeoutSeconds())
                    .isAfter(now);
            default -> false;
        };
    }

    private MqMessageStatus resolveStatus(String status) {
        try {
            return MqMessageStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }
}
