package com.manzhushaka.system.application.query;

/**
 * 消息队列台账查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MqMessageLogQuery(String messageType, String streamKey, String businessKey,
        String status, String beginTime, String endTime)
{
}
