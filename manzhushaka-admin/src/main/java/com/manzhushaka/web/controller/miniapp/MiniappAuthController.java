package com.manzhushaka.web.controller.miniapp;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.iip.application.member.result.MemberProfileResult;
import com.manzhushaka.iip.application.member.service.MiniappAuthAppService;
import com.manzhushaka.web.converter.miniapp.MiniappAuthConverter;
import com.manzhushaka.web.dto.miniapp.LoginRequest;

/**
 * 小程序认证 信息操作处理
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/miniapp")
public class MiniappAuthController
{
    @Autowired
    private MiniappAuthAppService authAppService;

    @Autowired
    private TokenService tokenService;

    /**
     * 小程序登录（匿名访问，平台 code 换取 openid 后签发 token）
     *
     * @param request 登录请求
     * @return token 与用户资料
     */
    @Anonymous
    @Log(title = "小程序登录", businessType = BusinessType.OTHER, isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping("/auth/login")
    public AjaxResult login(@Validated @RequestBody LoginRequest request)
    {
        MemberProfileResult profile = authAppService.login(MiniappAuthConverter.toCommand(request));
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(profile.memberId())
                .username("m_" + profile.memberId())
                .permissions(Collections.emptySet())
                .build();
        String token = tokenService.createToken(principal);
        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        ajax.put("member", profile);
        return ajax;
    }

    /**
     * 获取当前登录用户资料
     *
     * @return 用户资料
     */
    @GetMapping("/member/profile")
    public AjaxResult profile()
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("member", authAppService.getProfile(SecurityContextHelper.getUserId()));
        return ajax;
    }
}
