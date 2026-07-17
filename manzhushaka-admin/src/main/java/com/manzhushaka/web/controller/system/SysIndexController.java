package com.manzhushaka.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.config.ManzhushakaConfig;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.system.application.service.SystemSecurityQueryService;
import com.manzhushaka.web.dto.system.UnlockScreenRequest;

/**
 * 首页
 *
 * @author manzhushaka
 */
@RestController
public class SysIndexController
{
    /** 系统基础配置 */
    @Autowired
    private ManzhushakaConfig manzhushakaConfig;

    @Autowired
    private SystemSecurityQueryService systemSecurityQueryService;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index()
    {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", manzhushakaConfig.getName(), manzhushakaConfig.getVersion());
    }

    /**
     * 解锁屏幕。
     *
     * @param request 解锁请求
     * @return 解锁结果
     */
    @Log(title = "屏幕解锁", businessType = BusinessType.OTHER)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/unlockscreen")
    public AjaxResult unlockScreen(@RequestBody UnlockScreenRequest request)
    {
        String password = request == null ? null : request.getPassword();
        if (StringUtils.isEmpty(password))
        {
            return AjaxResult.error("密码不能为空");
        }
        String username = SecurityContextHelper.getUsername();
        if (!systemSecurityQueryService.matchesPassword(username, password))
        {
            return AjaxResult.error("密码错误，请重新输入");
        }

        return AjaxResult.success("解锁成功");
    }
}
