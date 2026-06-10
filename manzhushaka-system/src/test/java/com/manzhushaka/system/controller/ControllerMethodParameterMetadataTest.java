package com.manzhushaka.system.controller;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerMethodParameterMetadataTest {

    @Test
    void deptDetailPathVariableShouldRetainParameterName() throws NoSuchMethodException {
        Method method = DeptController.class.getMethod("getById", Long.class);
        Parameter parameter = method.getParameters()[0];

        assertTrue(
            parameter.isNamePresent(),
            "DeptController#getById path variable parameter name should be retained for Spring binding"
        );
        assertEquals("id", parameter.getName());
    }
}
