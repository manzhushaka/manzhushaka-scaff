package com.manzhushaka.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.manzhushaka.auth.dto.LoginRequest;
import com.manzhushaka.auth.service.AuthCaptchaService;
import com.manzhushaka.auth.service.AuthService;
import com.manzhushaka.auth.vo.AuthMenuVO;
import com.manzhushaka.auth.vo.CaptchaResponse;
import com.manzhushaka.auth.vo.LoginResponse;
import com.manzhushaka.common.annotation.OpLog;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.model.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthCaptchaService authCaptchaService;

    public AuthController(AuthService authService, AuthCaptchaService authCaptchaService) {
        this.authService = authService;
        this.authCaptchaService = authCaptchaService;
    }

    @SaIgnore
    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        return ApiResponse.success(authCaptchaService.createCaptcha());
    }

    @SaIgnore
    @PostMapping("/login")
    @OpLog(module = "认证", action = "登录", businessType = BusinessType.LOGIN, recordRequest = false)
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    @OpLog(module = "认证", action = "登出", businessType = BusinessType.LOGOUT, recordResponse = false)
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success(null);
    }

    @GetMapping({"/me", "/profile"})
    public ApiResponse<LoginResponse.UserInfo> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }

    @GetMapping("/menus")
    public ApiResponse<List<AuthMenuVO>> menus() {
        return ApiResponse.success(authService.currentMenus());
    }

    @GetMapping("/permissions")
    public ApiResponse<List<String>> permissions() {
        return ApiResponse.success(authService.currentPermissions());
    }
}
