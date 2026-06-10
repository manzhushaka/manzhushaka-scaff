package com.manzhushaka.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest {

    @Test
    void captchaEndpointShouldAllowAnonymousAccess() throws NoSuchMethodException {
        Method captchaMethod = AuthController.class.getDeclaredMethod("captcha");

        assertTrue(captchaMethod.isAnnotationPresent(SaIgnore.class));
    }
}
