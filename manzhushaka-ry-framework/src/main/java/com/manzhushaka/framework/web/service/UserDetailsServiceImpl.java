package com.manzhushaka.framework.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.enums.UserStatus;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.MessageUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysUserService;

/**
 * 用户验证处理
 *
 * @author manzhushaka
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        passwordService.validate(user);

        return createLoginPrincipal(user);
    }

    /**
     * 从 SysUser 构建扁平化的 LoginPrincipal
     */
    public LoginPrincipal createLoginPrincipal(SysUser user)
    {
        Set<String> permissions = permissionService.getMenuPermission(user);
        Set<String> roleKeys = extractRoleKeys(user.getRoles());

        LoginPrincipal.Builder builder = LoginPrincipal.builder()
                .userId(user.getUserId())
                .deptId(user.getDeptId())
                .username(user.getUserName())
                .password(user.getPassword())
                .permissions(permissions)
                .roleKeys(roleKeys);

        // 填充 deptName（如果存在）
        if (user.getDept() != null)
        {
            builder.deptName(user.getDept().getDeptName());
        }

        return builder.build();
    }

    /**
     * 从角色列表中提取角色键集合
     */
    private Set<String> extractRoleKeys(List<SysRole> roles)
    {
        if (CollectionUtils.isEmpty(roles))
        {
            return new HashSet<>();
        }
        return roles.stream()
                .map(SysRole::getRoleKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 兼容旧的 createLoginUser 方法签名（返回 UserDetails）
     * 用于外部调用
     */
    public UserDetails createLoginUser(SysUser user)
    {
        return createLoginPrincipal(user);
    }
}
