package com.manzhushaka.common.config;

import java.lang.reflect.Method;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 项目配置测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class ManzhushakaConfigTest
{
    /**
     * 项目配置不应再暴露已废弃的验证码样式绑定。
     */
    @Test
    void shouldNotExposeCaptchaConfiguration()
    {
        assertThat(memberNames()).noneMatch(name -> name.toLowerCase(Locale.ENGLISH).contains("captcha"));
    }

    /**
     * 获取 {@link ManzhushakaConfig} 的字段和方法名列表。
     *
     * @return 字段和方法名列表
     */
    private String[] memberNames()
    {
        Method[] methods = ManzhushakaConfig.class.getDeclaredMethods();
        String[] memberNames = new String[ManzhushakaConfig.class.getDeclaredFields().length + methods.length];
        for (int i = 0; i < ManzhushakaConfig.class.getDeclaredFields().length; i++)
        {
            memberNames[i] = ManzhushakaConfig.class.getDeclaredFields()[i].getName();
        }
        for (int i = 0; i < methods.length; i++)
        {
            memberNames[ManzhushakaConfig.class.getDeclaredFields().length + i] = methods[i].getName();
        }
        return memberNames;
    }
}
