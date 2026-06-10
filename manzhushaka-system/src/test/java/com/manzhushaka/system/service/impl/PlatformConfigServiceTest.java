package com.manzhushaka.system.service.impl;

import com.manzhushaka.db.system.entity.SysConfig;
import com.manzhushaka.db.system.mapper.SysConfigMapper;
import com.manzhushaka.system.vo.config.PlatformConfigVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformConfigServiceTest {

    @Test
    void shouldLoadPlatformConfigFromFixedKeys() {
        SysConfigMapper configMapper = mock(SysConfigMapper.class);
        PlatformConfigService service = new PlatformConfigService(configMapper);

        SysConfig appName = new SysConfig();
        appName.setId(100L);
        appName.setConfigName("系统名称");
        appName.setConfigKey("sys.platform.name");
        appName.setConfigValue("山海平台");
        appName.setStatus(1);

        SysConfig logo = new SysConfig();
        logo.setId(101L);
        logo.setConfigName("平台 Logo");
        logo.setConfigKey("sys.platform.logo-url");
        logo.setConfigValue("https://cdn.example.com/logo.png");
        logo.setStatus(1);

        SysConfig subtitle = new SysConfig();
        subtitle.setId(102L);
        subtitle.setConfigName("系统副标题");
        subtitle.setConfigKey("sys.platform.subtitle");
        subtitle.setConfigValue("Unified Operations Center");
        subtitle.setStatus(1);

        when(configMapper.selectList(any())).thenReturn(List.of(appName, subtitle, logo));

        PlatformConfigVO result = service.getPlatformConfig();

        assertEquals("山海平台", result.getPlatformName());
        assertEquals("Unified Operations Center", result.getPlatformSubtitle());
        assertEquals("https://cdn.example.com/logo.png", result.getLogoUrl());
    }

    @Test
    void shouldCreateMissingPlatformConfigRowsWhenSaving() {
        SysConfigMapper configMapper = mock(SysConfigMapper.class);
        PlatformConfigService service = new PlatformConfigService(configMapper);
        PlatformConfigVO payload = new PlatformConfigVO();
        payload.setPlatformName("山海平台");
        payload.setPlatformSubtitle("Unified Operations Center");
        payload.setLogoUrl("https://cdn.example.com/logo.png");

        when(configMapper.selectList(any())).thenReturn(List.of());

        service.savePlatformConfig(payload);

        verify(configMapper, times(3)).insert(any(SysConfig.class));
    }

    @Test
    void shouldFallbackToDefaultSubtitleWhenConfigIsMissing() {
        SysConfigMapper configMapper = mock(SysConfigMapper.class);
        PlatformConfigService service = new PlatformConfigService(configMapper);

        when(configMapper.selectList(any())).thenReturn(List.of());

        PlatformConfigVO result = service.getPlatformConfig();

        assertEquals("PLATFORM CONSOLE", result.getPlatformSubtitle());
    }
}
