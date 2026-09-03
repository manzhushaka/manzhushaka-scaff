package com.manzhushaka.framework.security.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;

/** PermissionContextHolder 无请求线程测试。 */
class PermissionContextHolderTest
{
    @AfterEach
    void tearDown()
    {
        PermissionContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldStorePermissionWithoutHttpRequest()
    {
        RequestContextHolder.resetRequestAttributes();

        PermissionContextHolder.setContext("system:user:list");

        assertEquals("system:user:list", PermissionContextHolder.getContext());
        PermissionContextHolder.clearContext();
        assertNull(PermissionContextHolder.getContext());
    }
}
