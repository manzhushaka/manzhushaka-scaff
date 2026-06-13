package com.manzhushaka.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义 DataScope 注解。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /**
     * 返回数据表别名。
     *
     * @return 处理结果
     */
    String tableAlias() default "t";

    /**
     * 返回部门字段名。
     *
     * @return 处理结果
     */
    String deptColumn() default "dept_id";

    /**
     * 返回用户字段名。
     *
     * @return 处理结果
     */
    String userColumn() default "id";
}
