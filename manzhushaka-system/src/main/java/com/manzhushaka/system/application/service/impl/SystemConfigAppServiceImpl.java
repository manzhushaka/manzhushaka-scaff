package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.application.command.SaveConfigCommand;
import com.manzhushaka.system.application.query.ConfigQuery;
import com.manzhushaka.system.application.result.system.ConfigResult;
import com.manzhushaka.system.application.service.SystemConfigAppService;
import com.manzhushaka.system.domain.SysConfig;
import com.manzhushaka.system.service.ISysConfigService;

/**
 * 系统参数配置应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class SystemConfigAppServiceImpl implements SystemConfigAppService
{
    @Autowired
    private ISysConfigService configService;

    @Override
    public List<ConfigResult> listConfigs(ConfigQuery query)
    {
        return configService.selectConfigList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public ConfigResult getConfig(Long configId)
    {
        return toResult(configService.selectConfigById(configId));
    }

    @Override
    public String getConfigValue(String configKey)
    {
        return configService.selectConfigByKey(configKey);
    }

    @Override
    @Transactional
    public int createConfig(SaveConfigCommand command, String operatorUsername)
    {
        SysConfig config = toEntity(command);
        if (!configService.checkConfigKeyUnique(config))
        {
            throw new ServiceException("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setCreateBy(operatorUsername);
        return configService.insertConfig(config);
    }

    @Override
    @Transactional
    public int updateConfig(SaveConfigCommand command, String operatorUsername)
    {
        SysConfig config = toEntity(command);
        if (!configService.checkConfigKeyUnique(config))
        {
            throw new ServiceException("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setUpdateBy(operatorUsername);
        return configService.updateConfig(config);
    }

    @Override
    @Transactional
    public void deleteConfigs(Long[] configIds)
    {
        configService.deleteConfigByIds(configIds);
    }

    @Override
    public void refreshCache()
    {
        configService.resetConfigCache();
    }

    private SysConfig toEntity(ConfigQuery query)
    {
        SysConfig config = new SysConfig();
        if (query == null)
        {
            return config;
        }
        config.setConfigName(query.configName());
        config.setConfigKey(query.configKey());
        config.setConfigType(query.configType());
        putDateRange(config, query.beginTime(), query.endTime());
        return config;
    }

    private SysConfig toEntity(SaveConfigCommand command)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(command.configId());
        config.setConfigName(command.configName());
        config.setConfigKey(command.configKey());
        config.setConfigValue(command.configValue());
        config.setConfigType(command.configType());
        config.setRemark(command.remark());
        return config;
    }

    private void putDateRange(SysConfig config, String beginTime, String endTime)
    {
        if (beginTime != null)
        {
            config.getParams().put("beginTime", beginTime);
        }
        if (endTime != null)
        {
            config.getParams().put("endTime", endTime);
        }
    }

    private ConfigResult toResult(SysConfig config)
    {
        if (config == null)
        {
            return null;
        }
        return new ConfigResult(config.getConfigId(), config.getConfigName(), config.getConfigKey(),
                config.getConfigValue(), config.getConfigType(), config.getCreateBy(), config.getCreateTime(),
                config.getUpdateBy(), config.getUpdateTime(), config.getRemark());
    }
}
