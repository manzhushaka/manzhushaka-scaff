package com.manzhushaka.framework.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.manzhushaka.framework.security.model.LoginPrincipal;

/**
 * Redis FastJson 序列化测试
 *
 * @author manzhushaka
 */
class FastJson2JsonRedisSerializerTest {

    /**
     * 反序列化登录主体时应保留认证所需字段
     */
    @Test
    void deserializeLoginPrincipalShouldKeepIdentityFields() {
        FastJson2JsonRedisSerializer<Object> serializer = new FastJson2JsonRedisSerializer<>(Object.class);
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(1L)
                .deptId(103L)
                .deptName("研发部门")
                .username("admin")
                .permissions(Set.of("*:*:*"))
                .roleKeys(Set.of("admin"))
                .token("token-1")
                .build();

        Object deserialized = serializer.deserialize(serializer.serialize(principal));

        assertThat(deserialized).isInstanceOf(LoginPrincipal.class);
        LoginPrincipal actual = (LoginPrincipal) deserialized;
        assertThat(actual.getUserId()).isEqualTo(1L);
        assertThat(actual.getDeptId()).isEqualTo(103L);
        assertThat(actual.getDeptName()).isEqualTo("研发部门");
        assertThat(actual.getUsername()).isEqualTo("admin");
        assertThat(actual.getPermissions()).containsExactly("*:*:*");
        assertThat(actual.getRoleKeys()).containsExactly("admin");
        assertThat(actual.getToken()).isEqualTo("token-1");
    }
}
