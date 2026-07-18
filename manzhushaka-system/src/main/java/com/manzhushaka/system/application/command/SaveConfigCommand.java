package com.manzhushaka.system.application.command;

/**
 * 参数配置保存命令。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record SaveConfigCommand(Long configId, String configName, String configKey,
        String configValue, String configType, String remark)
{
    @Override
    public String toString()
    {
        return "SaveConfigCommand[configId=" + configId + ", configName=" + configName
                + ", configKey=" + configKey + ", configType=" + configType + "]";
    }
}
