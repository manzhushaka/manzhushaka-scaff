package com.manzhushaka.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.context.SaHolder;
import com.manzhushaka.common.context.LoginUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置 SaTokenConfig 相关组件。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 创建 add Interceptors 数据。
     *
     * @param registry registry 参数
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> SaRouter.match("/**")
            .check(r -> {
                if (SaHolder.getRequest().isMethod("OPTIONS")) {
                    return;
                }
            })
            .notMatch("/error", "/actuator/health", "/actuator/health/**")
            .check(r -> StpUtil.checkLogin()))).addPathPatterns("/**");
        registry.addInterceptor(new LoginUserInterceptor()).addPathPatterns("/**");
    }

    /**
     * 配置管理台跨域策略，仅允许本地受控前端携带凭据访问。
     *
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5173", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    static class LoginUserInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        /**
         * 执行 pre Handle 逻辑。
         *
         * @param request 请求参数
         * @param response 响应数据
         * @param handler handler 参数
         * @return 处理结果
         */
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            Object loginUser = StpUtil.isLogin() ? StpUtil.getSession().get("loginUser") : null;
            if (loginUser instanceof com.manzhushaka.common.context.LoginUser value) {
                LoginUserContext.set(value);
            }
            return true;
        }

        /**
         * 执行 after Completion 逻辑。
         *
         * @param request 请求参数
         * @param response 响应数据
         * @param handler handler 参数
         * @param ex ex 参数
         */
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            LoginUserContext.clear();
        }
    }
}
