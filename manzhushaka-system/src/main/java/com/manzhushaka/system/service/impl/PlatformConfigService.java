package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manzhushaka.db.system.entity.SysConfig;
import com.manzhushaka.db.system.mapper.SysConfigMapper;
import com.manzhushaka.system.vo.config.PlatformConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现 PlatformConfigService 业务服务。
 */
@Service
public class PlatformConfigService {

    public static final String PLATFORM_NAME_KEY = "sys.platform.name";
    public static final String PLATFORM_SUBTITLE_KEY = "sys.platform.subtitle";
    public static final String PLATFORM_LOGO_URL_KEY = "sys.platform.logo-url";
    private static final String LEGACY_PLATFORM_NAME_KEY = "sys.app.name";
    private static final String DEFAULT_PLATFORM_NAME = "manzhushaka 管理台";
    private static final String DEFAULT_PLATFORM_SUBTITLE = "PLATFORM CONSOLE";

    private final SysConfigMapper configMapper;

    /**
     * 创建 PlatformConfigService 实例。
     *
     * @param configMapper configMapper 参数
     */
    public PlatformConfigService(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    /**
     * 返回 platformConfig。
     *
     * @return 字段值
     */
    public PlatformConfigVO getPlatformConfig() {
        Map<String, SysConfig> configMap = loadPlatformConfigs();
        PlatformConfigVO result = new PlatformConfigVO();
        result.setPlatformName(resolvePlatformName(configMap));
        result.setPlatformSubtitle(resolveConfigValue(configMap.get(PLATFORM_SUBTITLE_KEY), DEFAULT_PLATFORM_SUBTITLE));
        result.setLogoUrl(resolveConfigValue(configMap.get(PLATFORM_LOGO_URL_KEY), ""));
        return result;
    }

    /**
     * 更新 save Platform Config 数据。
     *
     * @param payload 请求数据
     */
    @Transactional
    public void savePlatformConfig(PlatformConfigVO payload) {
        Map<String, SysConfig> configMap = loadPlatformConfigs();
        saveConfig(configMap.get(PLATFORM_NAME_KEY), "系统名称", PLATFORM_NAME_KEY, payload.getPlatformName());
        saveConfig(configMap.get(PLATFORM_SUBTITLE_KEY), "系统副标题", PLATFORM_SUBTITLE_KEY, payload.getPlatformSubtitle());
        saveConfig(configMap.get(PLATFORM_LOGO_URL_KEY), "平台 Logo", PLATFORM_LOGO_URL_KEY, payload.getLogoUrl());
    }

    /**
     * 查询 load Platform Configs 结果。
     *
     * @return 查询结果
     */
    private Map<String, SysConfig> loadPlatformConfigs() {
        List<SysConfig> configs = configMapper.selectList(new LambdaQueryWrapper<SysConfig>()
            .in(SysConfig::getConfigKey, List.of(PLATFORM_NAME_KEY, PLATFORM_SUBTITLE_KEY, PLATFORM_LOGO_URL_KEY, LEGACY_PLATFORM_NAME_KEY)));
        Map<String, SysConfig> configMap = new HashMap<>();
        for (SysConfig config : configs) {
            configMap.put(config.getConfigKey(), config);
        }
        return configMap;
    }

    /**
     * 构建 resolve Platform Name 结果。
     *
     * @param configMap configMap 参数
     * @return 处理结果
     */
    private String resolvePlatformName(Map<String, SysConfig> configMap) {
        String platformName = resolveConfigValue(configMap.get(PLATFORM_NAME_KEY), "");
        if (StringUtils.hasText(platformName)) {
            return platformName;
        }
        return resolveConfigValue(configMap.get(LEGACY_PLATFORM_NAME_KEY), DEFAULT_PLATFORM_NAME);
    }

    /**
     * 更新 save Config 数据。
     *
     * @param existing existing 参数
     * @param configName configName 参数
     * @param configKey configKey 参数
     * @param rawValue rawValue 参数
     */
    private void saveConfig(SysConfig existing, String configName, String configKey, String rawValue) {
        String configValue = normalizeValue(rawValue, resolveDefaultValue(configKey));
        if (existing == null) {
            SysConfig entity = new SysConfig();
            entity.setConfigName(configName);
            entity.setConfigKey(configKey);
            entity.setConfigValue(configValue);
            entity.setStatus(1);
            configMapper.insert(entity);
            return;
        }
        existing.setConfigName(configName);
        existing.setConfigValue(configValue);
        existing.setStatus(1);
        configMapper.updateById(existing);
    }

    /**
     * 构建 resolve Config Value 结果。
     *
     * @param config config 参数
     * @param defaultValue defaultValue 参数
     * @return 处理结果
     */
    private String resolveConfigValue(SysConfig config, String defaultValue) {
        return normalizeValue(config == null ? null : config.getConfigValue(), defaultValue);
    }

    /**
     * 构建 normalize Value 结果。
     *
     * @param value 字段值
     * @param defaultValue defaultValue 参数
     * @return 处理结果
     */
    private String normalizeValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    /**
     * 构建 resolve Default Value 结果。
     *
     * @param configKey configKey 参数
     * @return 处理结果
     */
    private String resolveDefaultValue(String configKey) {
        if (PLATFORM_NAME_KEY.equals(configKey)) {
            return DEFAULT_PLATFORM_NAME;
        }
        if (PLATFORM_SUBTITLE_KEY.equals(configKey)) {
            return DEFAULT_PLATFORM_SUBTITLE;
        }
        return "";
    }
}
