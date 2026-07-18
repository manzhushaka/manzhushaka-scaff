package com.manzhushaka.system.application.query;

/**
 * 字典类型查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record DictTypeQuery(String dictName, String dictType, String status,
        String beginTime, String endTime)
{
}
