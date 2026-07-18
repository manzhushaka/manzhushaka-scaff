package com.manzhushaka.system.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.system.application.command.SaveConfigCommand;
import com.manzhushaka.system.domain.SysConfig;
import com.manzhushaka.system.service.ISysConfigService;

/**
 * 系统参数配置应用服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class SystemConfigAppServiceImplTest
{
    /** 新增参数配置应返回底层真实影响行数。 */
    @Test
    void createConfigShouldReturnAffectedRows()
    {
        SystemConfigAppServiceImpl service = new SystemConfigAppServiceImpl();
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(service, "configService", configService);
        ArgumentCaptor<SysConfig> configCaptor = ArgumentCaptor.forClass(SysConfig.class);
        when(configService.checkConfigKeyUnique(any(SysConfig.class))).thenReturn(true);
        when(configService.insertConfig(configCaptor.capture())).thenReturn(1);

        int affectedRows = service.createConfig(new SaveConfigCommand(
                null, "测试参数", "test.config", "value", "N", "remark"), "operator");

        assertThat(affectedRows).isEqualTo(1);
        assertThat(configCaptor.getValue().getCreateBy()).isEqualTo("operator");
        assertThat(configCaptor.getValue().getConfigKey()).isEqualTo("test.config");
    }
}
