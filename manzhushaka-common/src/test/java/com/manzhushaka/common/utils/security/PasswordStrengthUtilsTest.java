package com.manzhushaka.common.utils.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 密码强度工具测试。
 *
 * @author manzhushaka
 * @date 2026-07-02
 */
class PasswordStrengthUtilsTest
{
    /**
     * 常见弱密码和简单规律密码应被拒绝。
     */
    @Test
    void shouldRejectWeakPasswords()
    {
        assertThat(PasswordStrengthUtils.isStrongPassword("admin", "123456")).isFalse();
        assertThat(PasswordStrengthUtils.isStrongPassword("admin", "admin123")).isFalse();
        assertThat(PasswordStrengthUtils.isStrongPassword("zhangsan", "Aaaa1111!")).isFalse();
        assertThat(PasswordStrengthUtils.isStrongPassword("zhangsan", "Abcd1234!")).isFalse();
        assertThat(PasswordStrengthUtils.isStrongPassword("zhangsan", "Password1!")).isFalse();
    }

    /**
     * 强密码应满足长度、复杂度且不包含用户名。
     */
    @Test
    void shouldAcceptStrongPassword()
    {
        assertThat(PasswordStrengthUtils.isStrongPassword("zhangsan", "Mzs@7294")).isTrue();
    }

    /**
     * 初始化默认密码必须满足强密码规则。
     */
    @Test
    void shouldAcceptInitialDefaultPassword()
    {
        assertThat(PasswordStrengthUtils.isStrongPassword("admin", "Qx9@Rv72")).isTrue();
    }

    /**
     * 弱密码原因应返回可展示文案。
     */
    @Test
    void shouldReturnWeakPasswordMessage()
    {
        assertThat(PasswordStrengthUtils.getWeakPasswordMessage("admin", "admin123"))
                .isEqualTo("密码不能包含用户名");
        assertThat(PasswordStrengthUtils.getWeakPasswordMessage("zhangsan", "Mzs@7294")).isNull();
    }
}
