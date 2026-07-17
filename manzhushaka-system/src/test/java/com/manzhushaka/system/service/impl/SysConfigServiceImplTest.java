package com.manzhushaka.system.service.impl;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.RedisTemplate;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.system.domain.SysConfig;
import com.manzhushaka.system.mapper.SysConfigMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 系统配置服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-17
 */
class SysConfigServiceImplTest
{
    /**
     * 未配置验证码开关时应默认返回开启状态。
     */
    @Test
    void selectCaptchaEnabledShouldDefaultToTrue()
    {
        contextRunner().run(context -> assertThat(context.getBean(SysConfigServiceImpl.class)
                .selectCaptchaEnabled()).isTrue());
    }

    /**
     * 配置文件关闭验证码时应返回关闭状态。
     */
    @Test
    void selectCaptchaEnabledShouldReturnFalseWhenConfigurationIsDisabled()
    {
        contextRunner().withPropertyValues("manzhushaka.captcha.enabled=false")
                .run(context -> assertThat(context.getBean(SysConfigServiceImpl.class)
                        .selectCaptchaEnabled()).isFalse());
    }

    /**
     * 创建系统配置服务测试上下文。
     *
     * @return 测试上下文运行器
     */
    private ApplicationContextRunner contextRunner()
    {
        SysConfigMapper configMapper = mock(SysConfigMapper.class);
        when(configMapper.selectConfigList(any(SysConfig.class))).thenReturn(Collections.emptyList());

        return new ApplicationContextRunner()
                .withBean(SysConfigMapper.class, () -> configMapper)
                .withBean(RedisTemplate.class, () -> mock(RedisTemplate.class))
                .withBean(RedisCache.class)
                .withBean(SysConfigServiceImpl.class);
    }
}
