package com.manzhushaka.system.application.service;

import com.manzhushaka.system.application.result.auth.AuthUserProfileResult;

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
     * 校验用户密码是否匹配。
     *
     * @param username 用户名
     * @param rawPassword 待校验的明文密码
     * @return 密码是否匹配
     */
    boolean matchesPassword(String username, String rawPassword);

}
