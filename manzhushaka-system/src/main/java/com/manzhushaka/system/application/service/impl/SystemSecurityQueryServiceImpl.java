package com.manzhushaka.system.application.service.impl;

import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.constant.UserConstants;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;
import com.manzhushaka.system.application.service.SystemSecurityQueryService;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysMenuService;
import com.manzhushaka.system.service.ISysRoleService;
import com.manzhushaka.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统安全认证查询服务实现
 * <p>
 * 使用现有的 {@link ISysUserService}、{@link ISysRoleService}、{@link ISysMenuService}
 * 来装配 {@link AuthUserProfileResult}。
 * </p>
 *
 * @author manzhushaka
 */
@Service
public class SystemSecurityQueryServiceImpl implements SystemSecurityQueryService {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysMenuService menuService;

    @Override
    public AuthUserProfileResult loadAuthProfileByUsername(String username) {
        SysUser user = userService.selectUserByUserName(username);
        if (user == null) {
            return null;
        }
        return buildAuthProfile(user);
    }

    @Override
    public AuthUserProfileResult loadAuthProfileByUserId(Long userId) {
        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return null;
        }
        return buildAuthProfile(user);
    }

    @Override
    public Set<String> loadRoleKeys(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        // 管理员
        if (1L == userId) {
            Set<String> roles = new HashSet<>();
            roles.add(Constants.SUPER_ADMIN);
            return roles;
        }
        // 非管理员，从角色服务查
        return roleService.selectRolePermissionByUserId(userId);
    }

    @Override
    public Set<String> loadPermissions(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        // 管理员
        if (1L == userId) {
            Set<String> perms = new HashSet<>();
            perms.add(Constants.ALL_PERMISSION);
            return perms;
        }
        // 非管理员，从菜单服务查
        return menuService.selectMenuPermsByUserId(userId);
    }

    // ========== 私有方法 ==========

    /**
     * 从 {@link SysUser} 构建 {@link AuthUserProfileResult}
     */
    private AuthUserProfileResult buildAuthProfile(SysUser user) {
        if (user == null) {
            return null;
        }

        Long userId = user.getUserId();
        boolean isAdmin = (userId != null && 1L == userId);

        // 角色键
        Set<String> roleKeys = buildRoleKeys(user, isAdmin);
        // 权限
        Set<String> permissions = buildPermissions(user, isAdmin);

        // 部门名称
        String deptName = null;
        if (user.getDept() != null) {
            deptName = user.getDept().getDeptName();
        }

        return new AuthUserProfileResult(
                userId,
                user.getDeptId(),
                deptName,
                user.getUserName(),
                user.getNickName(),
                user.getPassword(),
                user.getStatus(),
                user.getDelFlag(),
                isAdmin,
                roleKeys,
                permissions,
                user.getPwdUpdateDate()
        );
    }

    /**
     * 构建角色键集合
     * <p>
     * 等价于原 {@code SysPermissionService.getRolePermission()} 的逻辑。
     * </p>
     */
    private Set<String> buildRoleKeys(SysUser user, boolean isAdmin) {
        if (isAdmin) {
            Set<String> roles = new HashSet<>();
            roles.add(Constants.SUPER_ADMIN);
            return roles;
        }
        return roleService.selectRolePermissionByUserId(user.getUserId());
    }

    /**
     * 构建权限集合
     * <p>
     * 等价于原 {@code SysPermissionService.getMenuPermission()} 的逻辑。
     * 管理员拥有所有权限 {@code "*:*:*"}；
     * 非管理员按角色或用户ID查询菜单权限。
     * </p>
     */
    private Set<String> buildPermissions(SysUser user, boolean isAdmin) {
        Set<String> perms = new HashSet<>();
        if (isAdmin) {
            perms.add(Constants.ALL_PERMISSION);
            return perms;
        }

        List<SysRole> roles = user.getRoles();
        if (!CollectionUtils.isEmpty(roles)) {
            // 多角色设置
            for (SysRole role : roles) {
                if (StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL) && !role.isAdmin()) {
                    Set<String> rolePerms = menuService.selectMenuPermsByRoleId(role.getRoleId());
                    role.setPermissions(rolePerms);
                    perms.addAll(rolePerms);
                }
            }
        } else {
            perms.addAll(menuService.selectMenuPermsByUserId(user.getUserId()));
        }
        return perms;
    }
}