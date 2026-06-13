package com.manzhushaka.common.annotation;

import com.manzhushaka.common.enums.BusinessType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义 OpLog 注解。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {
    /**
     * 执行 module 逻辑。
     *
     * @return 处理结果
     */
    String module();

    /**
     * 执行 action 逻辑。
     *
     * @return 处理结果
     */
    String action();

    /**
     * 执行 business Type 逻辑。
     *
     * @return 处理结果
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 更新 record Request 数据。
     *
     * @return 处理结果
     */
    boolean recordRequest() default true;

    /**
     * 更新 record Response 数据。
     *
     * @return 处理结果
     */
    boolean recordResponse() default false;
}
