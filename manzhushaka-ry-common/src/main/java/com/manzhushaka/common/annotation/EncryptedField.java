package com.manzhushaka.common.annotation;

import com.manzhushaka.common.enums.EncryptedFieldType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 存储加密字段标记。
 *
 * @author manzhushaka
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EncryptedField {

    /**
     * 字段类型。
     *
     * @return 字段类型
     */
    EncryptedFieldType type() default EncryptedFieldType.GENERAL;

    /**
     * 精确查询使用的摘要字段名。
     *
     * @return 摘要字段名
     */
    String hashField() default "";
}