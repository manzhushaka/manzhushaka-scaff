package com.manzhushaka.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.manzhushaka.common.context.LoginUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> SaRouter.match("/**")
            .notMatch("/api/auth/login", "/error", "/actuator/**")
            .check(r -> StpUtil.checkLogin()))).addPathPatterns("/**");
        registry.addInterceptor(new LoginUserInterceptor()).addPathPatterns("/**");
    }

    static class LoginUserInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            Object loginUser = StpUtil.isLogin() ? StpUtil.getSession().get("loginUser") : null;
            if (loginUser instanceof com.manzhushaka.common.context.LoginUser value) {
                LoginUserContext.set(value);
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            LoginUserContext.clear();
        }
    }
}
