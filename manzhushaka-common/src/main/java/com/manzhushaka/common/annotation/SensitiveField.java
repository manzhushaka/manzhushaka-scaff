package com.manzhushaka.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.manzhushaka.common.enums.SensitiveType;
import com.manzhushaka.common.serialize.SensitiveFieldSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记对外返回时需要脱敏的字段。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveFieldSerializer.class)
public @interface SensitiveField {
    /**
     * 返回字段使用的脱敏策略。
     *
     * @return 脱敏策略
     */
    SensitiveType value() default SensitiveType.FULL;

    /**
     * 自定义脱敏时前缀保留位数。
     *
     * @return 前缀保留位数
     */
    int prefixKeep() default 0;

    /**
     * 自定义脱敏时后缀保留位数。
     *
     * @return 后缀保留位数
     */
    int suffixKeep() default 0;

    /**
     * 脱敏占位字符。
     *
     * @return 占位字符
     */
    char maskChar() default '*';
}
