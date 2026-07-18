package com.manzhushaka.system.application.command;

/**
 * 字典类型保存命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveDictTypeCommand(Long dictId, String dictName, String dictType,
        String status, String remark)
{
}
