package com.manzhushaka.common.enums;

/**
 * 字段脱敏类型枚举。
 */
public enum SensitiveType {
    /**
     * 全量脱敏。
     */
    FULL,

    /**
     * 手机号脱敏。
     */
    MOBILE,

    /**
     * 自定义前后保留位数脱敏。
     */
    CUSTOM
}
