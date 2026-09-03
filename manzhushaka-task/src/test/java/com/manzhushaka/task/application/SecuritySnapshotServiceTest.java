package com.manzhushaka.task.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.model.LoginPrincipal;

import tools.jackson.databind.ObjectMapper;

/** 安全上下文快照测试。 */
class SecuritySnapshotServiceTest
{
    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldExcludePasswordAndTokenAndRestoreIdentity()
    {
        SecuritySnapshotService service = new SecuritySnapshotService(new ObjectMapper());
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(8L)
                .deptId(12L)
                .username("operator")
                .password("secret-password")
                .token("secret-token")
                .permissions(Set.of("system:user:list"))
                .roleKeys(Set.of("common"))
                .roleIds(Set.of(2L))
                .build();

        String snapshot = service.create(principal);
        service.install(snapshot);

        assertFalse(snapshot.contains("secret-password"));
        assertFalse(snapshot.contains("secret-token"));
        assertEquals(8L, SecurityContextHelper.getUserId());
        assertEquals("operator", SecurityContextHelper.getUsername());
    }
}
