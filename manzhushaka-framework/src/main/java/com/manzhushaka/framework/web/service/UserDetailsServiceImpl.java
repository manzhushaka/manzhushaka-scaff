package com.manzhushaka.framework.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.manzhushaka.common.enums.UserStatus;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.MessageUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;
import com.manzhushaka.system.application.service.SystemSecurityQueryService;

/**
 * 用户验证处理
 * <p>
 * 通过 {@link SystemSecurityQueryService} 从 system 模块获取认证信息，
 * 不再直接操作持久化实体 {@code SysUser}。
 * </p>
 *
 * @author manzhushaka
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private SystemSecurityQueryService systemSecurityQueryService;
    
    @Autowired
    private SysPasswordService passwordService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        AuthUserProfileResult profile = systemSecurityQueryService.loadAuthProfileByUsername(username);
        if (profile == null)
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (UserStatus.DELETED.getCode().equals(profile.delFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (UserStatus.DISABLE.getCode().equals(profile.status()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        passwordService.validate(profile.password());

        return createLoginPrincipal(profile);
    }

    /**
     * 从 {@link AuthUserProfileResult} 构建扁平化的 {@link LoginPrincipal}
     */
    public LoginPrincipal createLoginPrincipal(AuthUserProfileResult profile)
    {
        return LoginPrincipal.builder()
                .userId(profile.userId())
                .deptId(profile.deptId())
                .deptName(profile.deptName())
                .username(profile.username())
                .password(profile.password())
                .permissions(profile.permissions())
                .roleKeys(profile.roleKeys())
                .build();
    }
}
