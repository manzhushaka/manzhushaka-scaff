package com.manzhushaka.framework.security.context;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PatternMatchUtils;

import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.constant.HttpStatus;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.security.model.LoginPrincipal;

/**
 * 安全上下文辅助工具类
 * <p>
 * 替代原 {@code com.manzhushaka.common.utils.SecurityUtils}，
 * 提供从 {@link SecurityContextHolder} 获取当前登录用户信息的静态方法。
 * <p>
 * 所有方法通过 {@link #getPrincipal()} 获取 {@link LoginPrincipal} 后提取所需字段。
 *
 * @author manzhushaka
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
        // 工具类，防止实例化
    }

    // ========== 获取安全上下文 ==========

    /**
     * 获取当前 Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户主体
     *
     * @return LoginPrincipal
     * @throws ServiceException 如果未认证或 Principal 类型不正确
     */
    public static LoginPrincipal getPrincipal() {
        Authentication auth = getAuthentication();
        LoginPrincipal principal = auth == null ? null : LoginPrincipal.restore(auth.getPrincipal());
        if (principal == null) {
            throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
        }
        return principal;
    }

    /**
     * 获取当前登录用户主体（安全版本，异常时返回 null）
     */
    public static LoginPrincipal getPrincipalQuietly() {
        try {
            Authentication auth = getAuthentication();
            if (auth != null) {
                return LoginPrincipal.restore(auth.getPrincipal());
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    // ========== 用户基本信息 ==========

    /**
     * 获取当前用户 ID
     */
    public static Long getUserId() {
        try {
            return getPrincipal().getUserId();
        } catch (Exception e) {
            throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取当前部门 ID
     */
    public static Long getDeptId() {
        try {
            return getPrincipal().getDeptId();
        } catch (Exception e) {
            throw new ServiceException("获取部门ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        try {
            return getPrincipal().getUsername();
        } catch (Exception e) {
            throw new ServiceException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取当前部门名称
     */
    public static String getDeptName() {
        try {
            return getPrincipal().getDeptName();
        } catch (Exception e) {
            throw new ServiceException("获取部门名称异常", HttpStatus.UNAUTHORIZED);
        }
    }

    // ========== 管理员判断 ==========

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        Long userId = getUserId();
        return userId != null && 1L == userId;
    }

    /**
     * 判断指定用户 ID 是否为管理员
     *
     * @param userId 用户 ID
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    // ========== 权限 / 角色判断 ==========

    /**
     * 验证当前用户是否具备某权限
     *
     * @param permission 权限字符串
     */
    public static boolean hasPermi(String permission) {
        return hasPermi(getPrincipal().getPermissions(), permission);
    }

    /**
     * 判断是否包含权限
     *
     * @param authorities 权限列表
     * @param permission  权限字符串
     */
    public static boolean hasPermi(Collection<String> authorities, String permission) {
        return authorities.stream()
                .filter(StringUtils::hasText)
                .anyMatch(x -> Constants.ALL_PERMISSION.equals(x) || PatternMatchUtils.simpleMatch(x, permission));
    }

    /**
     * 验证当前用户是否拥有某个角色
     *
     * @param roleKey 角色标识
     */
    public static boolean hasRole(String roleKey) {
        return hasRole(getPrincipal().getRoleKeys(), roleKey);
    }

    /**
     * 判断是否包含角色
     *
     * @param roleKeys 角色键集合
     * @param roleKey  角色标识
     */
    public static boolean hasRole(Collection<String> roleKeys, String roleKey) {
        return roleKeys.stream()
                .filter(StringUtils::hasText)
                .anyMatch(x -> Constants.SUPER_ADMIN.equals(x) || PatternMatchUtils.simpleMatch(x, roleKey));
    }
}
