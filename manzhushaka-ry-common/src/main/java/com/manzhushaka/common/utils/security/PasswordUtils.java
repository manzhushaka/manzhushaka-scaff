package com.manzhushaka.common.utils.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * <p>
 * 提供密码加密和匹配功能，替代原 {@code SecurityUtils} 中的密码相关方法。
 *
 * @author manzhushaka
 */
public final class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 对原始密码进行 BCrypt 加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证原始密码与加密密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    private PasswordUtils() {
        // 工具类，防止实例化
    }
}