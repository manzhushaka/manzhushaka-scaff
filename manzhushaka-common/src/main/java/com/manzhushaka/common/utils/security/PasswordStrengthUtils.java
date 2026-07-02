package com.manzhushaka.common.utils.security;

import java.util.Locale;
import java.util.Set;

/**
 * 密码强度校验工具。
 *
 * @author manzhushaka
 * @date 2026-07-02
 */
public final class PasswordStrengthUtils
{
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;
    private static final int MIN_CHAR_TYPE_COUNT = 3;
    private static final int WEAK_SEQUENCE_LENGTH = 4;
    private static final String ILLEGAL_CHARS = "<>\"'\\|";
    private static final Set<String> COMMON_WEAK_PASSWORDS = Set.of(
            "123456", "12345678", "123456789", "111111", "000000", "666666", "888888",
            "password", "password1", "password1!", "qwerty", "qwerty123", "admin",
            "admin123", "abc123", "abcd1234", "welcome", "welcome1", "letmein");

    private PasswordStrengthUtils()
    {
    }

    /**
     * 判断是否为强密码。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return true 表示强密码
     */
    public static boolean isStrongPassword(String username, String password)
    {
        return getWeakPasswordMessage(username, password) == null;
    }

    /**
     * 获取弱密码原因。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 弱密码原因；强密码返回 null
     */
    public static String getWeakPasswordMessage(String username, String password)
    {
        if (password == null || password.isEmpty())
        {
            return "密码不能为空";
        }
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH)
        {
            return "密码长度必须介于8到20个字符之间";
        }
        if (hasIllegalChar(password))
        {
            return "密码只能包含可见ASCII字符，且不能包含非法字符：< > \" ' \\ |";
        }
        String normalizedPassword = password.toLowerCase(Locale.ENGLISH);
        if (containsUsername(username, normalizedPassword))
        {
            return "密码不能包含用户名";
        }
        if (COMMON_WEAK_PASSWORDS.contains(normalizedPassword))
        {
            return "密码过于常见，请更换为更复杂的密码";
        }
        if (countCharTypes(password) < MIN_CHAR_TYPE_COUNT)
        {
            return "密码至少需要包含大写字母、小写字母、数字、特殊字符中的三类";
        }
        if (hasRepeatedChars(normalizedPassword))
        {
            return "密码不能包含4位及以上重复字符";
        }
        if (hasSequentialChars(normalizedPassword))
        {
            return "密码不能包含4位及以上连续字符";
        }
        return null;
    }

    /**
     * 判断密码是否包含用户名。
     *
     * @param username 用户名
     * @param normalizedPassword 小写化后的密码
     * @return true 表示包含用户名
     */
    private static boolean containsUsername(String username, String normalizedPassword)
    {
        if (username == null || username.length() < 3)
        {
            return false;
        }
        String normalizedUsername = username.toLowerCase(Locale.ENGLISH);
        return normalizedPassword.contains(normalizedUsername);
    }

    /**
     * 判断密码是否包含不可见字符或禁用字符。
     *
     * @param password 明文密码
     * @return true 表示包含非法字符
     */
    private static boolean hasIllegalChar(String password)
    {
        for (int i = 0; i < password.length(); i++)
        {
            char ch = password.charAt(i);
            if (ch < 33 || ch > 126 || ILLEGAL_CHARS.indexOf(ch) >= 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计密码中包含的字符类型数量。
     *
     * @param password 明文密码
     * @return 字符类型数量
     */
    private static int countCharTypes(String password)
    {
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (int i = 0; i < password.length(); i++)
        {
            char ch = password.charAt(i);
            if (Character.isUpperCase(ch))
            {
                hasUppercase = true;
            }
            else if (Character.isLowerCase(ch))
            {
                hasLowercase = true;
            }
            else if (Character.isDigit(ch))
            {
                hasDigit = true;
            }
            else
            {
                hasSpecial = true;
            }
        }
        int count = 0;
        count += hasUppercase ? 1 : 0;
        count += hasLowercase ? 1 : 0;
        count += hasDigit ? 1 : 0;
        count += hasSpecial ? 1 : 0;
        return count;
    }

    /**
     * 判断是否存在 4 位及以上重复字符。
     *
     * @param normalizedPassword 小写化后的密码
     * @return true 表示存在重复字符
     */
    private static boolean hasRepeatedChars(String normalizedPassword)
    {
        int repeatedCount = 1;
        for (int i = 1; i < normalizedPassword.length(); i++)
        {
            if (normalizedPassword.charAt(i) == normalizedPassword.charAt(i - 1))
            {
                repeatedCount++;
                if (repeatedCount >= WEAK_SEQUENCE_LENGTH)
                {
                    return true;
                }
            }
            else
            {
                repeatedCount = 1;
            }
        }
        return false;
    }

    /**
     * 判断是否存在 4 位及以上连续字符。
     *
     * @param normalizedPassword 小写化后的密码
     * @return true 表示存在连续字符
     */
    private static boolean hasSequentialChars(String normalizedPassword)
    {
        for (int i = 0; i <= normalizedPassword.length() - WEAK_SEQUENCE_LENGTH; i++)
        {
            if (isSequentialWindow(normalizedPassword, i, 1) || isSequentialWindow(normalizedPassword, i, -1))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定窗口是否按固定步长连续。
     *
     * @param normalizedPassword 小写化后的密码
     * @param startIndex 起始位置
     * @param step 连续步长
     * @return true 表示窗口连续
     */
    private static boolean isSequentialWindow(String normalizedPassword, int startIndex, int step)
    {
        for (int i = 1; i < WEAK_SEQUENCE_LENGTH; i++)
        {
            if (normalizedPassword.charAt(startIndex + i) - normalizedPassword.charAt(startIndex + i - 1) != step)
            {
                return false;
            }
        }
        return true;
    }
}
