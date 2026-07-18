package com.manzhushaka.system.application.command;

/**
 * 字典数据保存命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveDictDataCommand(Long dictCode, Long dictSort, String dictLabel,
        String dictValue, String dictType, String cssClass, String listClass,
        String isDefault, String status, String remark)
{
}
