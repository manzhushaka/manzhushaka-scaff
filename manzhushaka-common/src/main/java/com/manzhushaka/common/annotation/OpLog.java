package com.manzhushaka.common.annotation;

import com.manzhushaka.common.enums.BusinessType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {
    String module();

    String action();

    BusinessType businessType() default BusinessType.OTHER;

    boolean recordRequest() default true;

    boolean recordResponse() default false;
}
