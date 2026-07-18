package com.manzhushaka.system.application.query;

/**
 * 参数配置查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ConfigQuery(String configName, String configKey, String configType,
        String beginTime, String endTime)
{
}
