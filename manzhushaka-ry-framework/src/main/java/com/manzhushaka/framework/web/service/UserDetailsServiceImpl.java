package com.manzhushaka.framework.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.manzhushaka.common.core.domain.model.LoginUser;
import com.manzhushaka.common.enums.UserStatus;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.MessageUtils;
import com.manzhushaka.common.utils.StringUtils;
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

        // SysPasswordService.validate() 需要 common 版本的 SysUser
        passwordService.validate(SysUserConverter.toCommon(user));

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user)
    {
        // 将 system 模块的 SysUser 转换为 common 模块的 SysUser
        com.manzhushaka.common.core.domain.entity.SysUser commonUser =
                SysUserConverter.toCommon(user);
        return new LoginUser(user.getUserId(), user.getDeptId(), commonUser,
                permissionService.getMenuPermission(commonUser));
    }
}
