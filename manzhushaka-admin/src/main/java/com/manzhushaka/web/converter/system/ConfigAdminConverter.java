package com.manzhushaka.web.converter.system;

import com.manzhushaka.system.application.command.SaveConfigCommand;
import com.manzhushaka.system.application.query.ConfigQuery;
import com.manzhushaka.web.dto.system.ConfigRequest;

/**
 * 参数配置 Web 模型转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class ConfigAdminConverter
{
    private ConfigAdminConverter()
    {
    }

    public static ConfigQuery toQuery(ConfigRequest request)
    {
        return new ConfigQuery(request.getConfigName(), request.getConfigKey(), request.getConfigType(),
                request.getBeginTime(), request.getEndTime());
    }

    public static SaveConfigCommand toCommand(ConfigRequest request)
    {
        return new SaveConfigCommand(request.getConfigId(), request.getConfigName(), request.getConfigKey(),
                request.getConfigValue(), request.getConfigType(), request.getRemark());
    }
}
