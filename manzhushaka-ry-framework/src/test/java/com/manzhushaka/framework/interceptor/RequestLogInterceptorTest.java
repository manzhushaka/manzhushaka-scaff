package com.manzhushaka.framework.interceptor;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 请求日志拦截器测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class RequestLogInterceptorTest {

    /**
     * 请求日志查询自身不能再次记录请求日志，避免日志噪声。
     */
    @Test
    void shouldExcludeRequestLogEndpoint() {
        RequestLogInterceptor interceptor = new RequestLogInterceptor();

        assertThat(interceptor.shouldExclude("/monitor/requestLog/list")).isTrue();
        assertThat(interceptor.shouldExclude("/monitor/server")).isFalse();
    }

    /**
     * 静态资源、Druid、验证码等路径不记录请求日志。
     */
    @Test
    void shouldExcludeStaticAndSystemPaths() {
        RequestLogInterceptor interceptor = new RequestLogInterceptor();

        assertThat(interceptor.shouldExclude("/druid/login.html")).isTrue();
        assertThat(interceptor.shouldExclude("/profile/avatar.png")).isTrue();
        assertThat(interceptor.shouldExclude("/captchaImage")).isTrue();
        assertThat(interceptor.shouldExclude("/assets/index.js")).isTrue();
    }

    /**
     * 请求参数中的敏感字段不应原文落入请求日志。
     *
     * @throws Exception 反射调用失败
     */
    @Test
    void shouldMaskSensitiveRequestParams() throws Exception {
        RequestLogInterceptor interceptor = new RequestLogInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("username", "admin");
        request.addParameter("password", "plain-secret");

        Method method = RequestLogInterceptor.class.getDeclaredMethod("resolveRequestParams",
                jakarta.servlet.http.HttpServletRequest.class);
        method.setAccessible(true);
        String requestParams = (String) method.invoke(interceptor, request);

        assertThat(requestParams).contains("username").contains("admin");
        assertThat(requestParams).contains("password");
        assertThat(requestParams).doesNotContain("plain-secret");
    }
}
