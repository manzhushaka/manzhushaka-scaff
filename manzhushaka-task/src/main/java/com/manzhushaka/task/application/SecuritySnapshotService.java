package com.manzhushaka.task.application;

import java.util.Collections;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.manzhushaka.framework.security.model.LoginPrincipal;

import tools.jackson.databind.ObjectMapper;

/**
 * 创建和恢复异步任务所需的最小安全上下文。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Component
public class SecuritySnapshotService
{
    private final ObjectMapper objectMapper;

    public SecuritySnapshotService(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    /** 将当前身份序列化为不含密码和 Token 的快照。 */
    public String create(LoginPrincipal principal)
    {
        SecuritySnapshot snapshot = new SecuritySnapshot(principal.getUserId(), principal.getDeptId(),
                principal.getDeptName(), principal.getUsername(), safeSet(principal.getPermissions()),
                safeSet(principal.getRoleKeys()), principal.getRoleIds() == null
                        ? Collections.emptySet() : Set.copyOf(principal.getRoleIds()));
        return objectMapper.writeValueAsString(snapshot);
    }

    /** 在当前线程安装快照并返回原上下文，调用方必须在 finally 中恢复。 */
    public SecurityContext install(String snapshotJson)
    {
        SecuritySnapshot snapshot = objectMapper.readValue(snapshotJson, SecuritySnapshot.class);
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(snapshot.userId())
                .deptId(snapshot.deptId())
                .deptName(snapshot.deptName())
                .username(snapshot.username())
                .permissions(snapshot.permissions())
                .roleKeys(snapshot.roleKeys())
                .roleIds(snapshot.roleIds())
                .build();
        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        return previous;
    }

    /** 恢复安装前的上下文。 */
    public void restore(SecurityContext previous)
    {
        if (previous == null)
        {
            SecurityContextHolder.clearContext();
        }
        else
        {
            SecurityContextHolder.setContext(previous);
        }
    }

    private Set<String> safeSet(Set<String> values)
    {
        return values == null ? Collections.emptySet() : Set.copyOf(values);
    }

    /** 最小身份快照。 */
    public record SecuritySnapshot(Long userId, Long deptId, String deptName, String username,
            Set<String> permissions, Set<String> roleKeys, Set<Long> roleIds)
    {
    }
}
