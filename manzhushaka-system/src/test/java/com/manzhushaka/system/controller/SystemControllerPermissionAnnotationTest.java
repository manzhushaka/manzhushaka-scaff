package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemControllerPermissionAnnotationTest {

    @Test
    void systemControllerEndpointsShouldDeclarePermissionAnnotations() {
        List<Class<?>> controllers = List.of(
            CacheController.class,
            ConfigController.class,
            DeptController.class,
            DictController.class,
            ImportExportTaskController.class,
            LogQueryController.class,
            MenuController.class,
            PlatformConfigController.class,
            PlatformJobController.class,
            RoleController.class,
            ServerMonitorController.class,
            UserController.class
        );

        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                if (!isRequestHandler(method)) {
                    continue;
                }
                assertTrue(
                    method.isAnnotationPresent(SaCheckPermission.class) || method.isAnnotationPresent(SaCheckRole.class),
                    () -> controller.getSimpleName() + "#" + method.getName() + " should declare permission protection"
                );
            }
        }
    }

    /**
     * 判断方法是否为对外暴露的请求处理方法。
     *
     * @param method 控制器方法
     * @return true 表示需要进行权限注解校验
     */
    private boolean isRequestHandler(Method method) {
        return hasAnyAnnotation(
            method,
            RequestMapping.class,
            GetMapping.class,
            PostMapping.class,
            PutMapping.class,
            DeleteMapping.class,
            PatchMapping.class
        );
    }

    /**
     * 判断方法是否标注了任意指定注解。
     *
     * @param method 控制器方法
     * @param annotations 注解类型集合
     * @return true 表示至少命中一种注解
     */
    @SafeVarargs
    private final boolean hasAnyAnnotation(Method method, Class<? extends Annotation>... annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (method.isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }
}
