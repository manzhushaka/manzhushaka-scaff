package com.manzhushaka.system.application.query;

/**
 * 操作日志查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record OperLogQuery(String title, Integer businessType, Integer[] businessTypes,
        Integer status, String operName, String operIp, String beginTime, String endTime)
{
}
