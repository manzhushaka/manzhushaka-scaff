package com.manzhushaka.web.controller.miniapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import com.manzhushaka.common.annotation.Anonymous;

/**
 * 小程序活动控制器测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class MiniappActivityControllerTest
{
    /**
     * 游客首页依赖的活动接口必须允许匿名访问。
     *
     * @throws NoSuchMethodException 接口方法不存在时抛出
     */
    @Test
    void activityQueriesShouldAllowAnonymousAccess() throws NoSuchMethodException
    {
        Method currentMethod = MiniappActivityController.class.getMethod("current");
        Method listMethod = MiniappActivityController.class.getMethod("list");

        assertThat(currentMethod.isAnnotationPresent(Anonymous.class)).isTrue();
        assertThat(listMethod.isAnnotationPresent(Anonymous.class)).isTrue();
    }
}
