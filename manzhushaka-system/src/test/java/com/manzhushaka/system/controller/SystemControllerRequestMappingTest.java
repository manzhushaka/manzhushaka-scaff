package com.manzhushaka.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemControllerRequestMappingTest {

    @Test
    void systemControllersShouldExposeApiPrefixedMappings() {
        assertRequestMapping(CacheController.class, "/api/system/cache");
        assertRequestMapping(UserController.class, "/api/system/users");
        assertRequestMapping(RoleController.class, "/api/system/roles");
        assertRequestMapping(DeptController.class, "/api/system/depts");
        assertRequestMapping(MenuController.class, "/api/system/menus");
        assertRequestMapping(DictController.class, "/api/system/dicts");
        assertRequestMapping(ConfigController.class, "/api/system/configs");
        assertRequestMapping(PlatformConfigController.class, "/api/system/platform-config");
        assertRequestMapping(ServerMonitorController.class, "/api/system/monitor");
        assertRequestMapping(LogQueryController.class, "/api/system/logs");
        assertRequestMapping(ImportExportTaskController.class, "/api/system/io-tasks");
        assertRequestMapping(PlatformJobController.class, "/api/system/jobs");
    }

    private void assertRequestMapping(Class<?> controllerClass, String expectedPath) {
        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);

        assertNotNull(requestMapping, controllerClass.getSimpleName() + " should declare @RequestMapping");
        assertTrue(
            Arrays.asList(requestMapping.value()).contains(expectedPath),
            () -> controllerClass.getSimpleName() + " should expose " + expectedPath
        );
    }
}
