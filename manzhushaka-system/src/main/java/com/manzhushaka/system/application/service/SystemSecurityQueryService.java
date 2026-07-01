package com.manzhushaka.system.application.service;

import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;

import java.util.Set;

/**
 * 系统安全认证查询服务
 * <p>
 * 提供认证所需的用户、角色、权限查询契约，
 * 供 {@code framework} 和 {@code admin} 模块通过稳定的应用层接口读取认证信息，
 * 不再直接操作持久化实体。
 * </p>
 *
 * @author manzhushaka
 */
public interface SystemSecurityQueryService {

    /**
     * 根据用户名加载用户认证信息
     *
     * @param username 用户名
     * @return 用户认证信息，不存在时返回 {@code null}
     */
    AuthUserProfileResult loadAuthProfileByUsername(String username);

    /**
     * 根据用户ID加载用户认证信息
     *
     * @param userId 用户ID
     * @return 用户认证信息，不存在时返回 {@code null}
     */
    AuthUserProfileResult loadAuthProfileByUserId(Long userId);

    /**
     * 加载用户角色键集合
     *
     * @param userId 用户ID
     * @return 角色键集合
     */
    Set<String> loadRoleKeys(Long userId);

    /**
     * 加载用户权限集合
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> loadPermissions(Long userId);
}