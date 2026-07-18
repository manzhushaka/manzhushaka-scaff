package com.manzhushaka.web.controller.system;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.text.Convert;
import com.manzhushaka.common.utils.DateUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.SysLoginService;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.framework.web.command.LoginCommand;
import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;
import com.manzhushaka.system.application.service.SystemSecurityQueryService;
import com.manzhushaka.system.service.ISysConfigService;
import com.manzhushaka.system.application.service.SystemMenuAppService;
import com.manzhushaka.web.converter.system.AuthAdminConverter;
import com.manzhushaka.web.dto.system.LoginRequest;
import com.manzhushaka.web.vo.system.user.AuthUserProfileVO;

/**
 * 登录验证
 *
 * @author manzhushaka
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private SystemMenuAppService menuAppService;

    @Autowired
    private SystemSecurityQueryService systemSecurityQueryService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginRequest request)
    {
        AjaxResult ajax = AjaxResult.success();
        LoginCommand command = AuthAdminConverter.toLoginCommand(request);
        // 生成令牌
        String token = loginService.login(command.username(), command.password(), command.code(),
                command.uuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        // 通过系统安全查询服务获取用户认证信息，不再直接操作 SysUser 实体
        AuthUserProfileResult profile = null;
        if (principal.getUserId() != null) {
            profile = systemSecurityQueryService.loadAuthProfileByUserId(principal.getUserId());
        }
        // 角色集合
        Set<String> roles = profile != null ? profile.roleKeys() : Collections.emptySet();
        // 权限集合
        Set<String> permissions = profile != null ? profile.permissions() : Collections.emptySet();
        if (profile != null && !principal.getPermissions().equals(permissions))
        {
            principal.setPermissions(permissions);
            tokenService.refreshToken(principal);
        }
        AjaxResult ajax = AjaxResult.success();
        AuthUserProfileVO user = AuthAdminConverter.toAuthUserProfileVO(profile);
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        ajax.put("pwdChrtype", getSysAccountChrtype());
        ajax.put("isDefaultModifyPwd", profile != null ? initPasswordIsModify(profile.pwdUpdateDate()) : false);
        ajax.put("isPasswordExpired", profile != null ? passwordIsExpiration(profile.pwdUpdateDate()) : false);
        return ajax;
    }

    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityContextHelper.getUserId();
        return AjaxResult.success(menuAppService.listRouterResults(userId));
    }

    // 获取用户密码自定义配置规则
    public String getSysAccountChrtype()
    {
        return Convert.toStr(configService.selectConfigByKey("sys.account.chrtype"), "0");
    }

    // 检查初始密码是否提醒修改
    public boolean initPasswordIsModify(Date pwdUpdateDate)
    {
        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
    }

    // 检查密码是否过期
    public boolean passwordIsExpiration(Date pwdUpdateDate)
    {
        Integer passwordValidateDays = Convert.toInt(
                configService.selectConfigByKey("sys.account.passwordValidateDays"));
        if (passwordValidateDays != null && passwordValidateDays > 0)
        {
            if (StringUtils.isNull(pwdUpdateDate))
            {
                // 如果从未修改过初始密码，直接提醒过期
                return true;
            }
            Date nowDate = DateUtils.getNowDate();
            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
        }
        return false;
    }
}
