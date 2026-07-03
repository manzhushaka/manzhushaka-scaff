package com.manzhushaka.framework.security.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 登录用户身份信息（扁平化设计）
 * <p>
 * 替代旧的 {@code LoginUser}，不再持有 {@code SysUser} 对象。
 * 所有用户信息字段扁平化存储，直接实现 {@link UserDetails} 接口。
 * <p>
 * 设计为不可变（通过 Builder 构建）——唯一可变的字段为 {@code password} 和 {@code permissions}，
 * 以支持密码更新与权限刷新场景。
 *
 * @author manzhushaka
 */
public class LoginPrincipal implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 用户身份字段 ==========

    /** 用户ID */
    private Long userId;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 商户档案ID（PII 业务可选字段） */
    private Long merchantId;

    /** 用户名 */
    private String username;

    /** 密码（加密后） */
    private String password;

    // ========== 权限 / 角色 ==========

    /** 权限列表 */
    private Set<String> permissions;

    /** 角色键集合 */
    private Set<String> roleKeys;

    // ========== Token & 会话 ==========

    /** 用户唯一标识（token） */
    private String token;

    /** 登录时间 */
    private Long loginTime;

    /** 过期时间 */
    private Long expireTime;

    // ========== 客户端信息 ==========

    /** 登录IP地址 */
    private String ipaddr;

    /** 登录地点 */
    private String loginLocation;

    /** 浏览器类型 */
    private String browser;

    /** 操作系统 */
    private String os;

    // ========== 构造器 ==========

    private LoginPrincipal() {
    }

    /**
     * 使用 Builder 构建实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 从弱类型对象中恢复登录主体
     *
     * @param source 原始主体对象
     * @return 恢复后的登录主体；无法恢复时返回 null
     */
    public static LoginPrincipal restore(Object source) {
        try {
            if (source == null) {
                return null;
            }
            if (source instanceof LoginPrincipal) {
                return (LoginPrincipal) source;
            }
            if (source instanceof CharSequence) {
                String text = source.toString();
                if (text.isBlank() || "anonymousUser".equals(text)) {
                    return null;
                }
                return JSON.parseObject(text, LoginPrincipal.class);
            }
            if (source instanceof Map || LoginPrincipal.class.getName().equals(source.getClass().getName())) {
                return JSON.parseObject(JSON.toJSONString(source), LoginPrincipal.class);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    // ========== 便捷方法 ==========

    /**
     * 判断是否为管理员（userId == 1L）
     */
    public boolean isAdmin() {
        return userId != null && 1L == userId;
    }

    // ========== UserDetails 实现 ==========

    @JSONField(serialize = false)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(Objects::nonNull)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @JSONField(serialize = false)
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JSONField(serialize = false)
    @Override
    public boolean isEnabled() {
        return true;
    }

    // ========== Getter / Setter ==========

    public Long getUserId() {
        return userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public Set<String> getRoleKeys() {
        return roleKeys;
    }

    public String getToken() {
        return token;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public String getOs() {
        return os;
    }

    // ========== Setter（供框架反射 / 刷新使用） ==========

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 设置部门ID
     *
     * @param deptId 部门ID
     */
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    /**
     * 设置部门名称
     *
     * @param deptName 部门名称
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * 设置角色键集合
     *
     * @param roleKeys 角色键集合
     */
    public void setRoleKeys(Set<String> roleKeys) {
        this.roleKeys = roleKeys;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setOs(String os) {
        this.os = os;
    }

    // ========== Builder ==========

    /**
     * LoginPrincipal 构建器
     */
    public static final class Builder {
        private final LoginPrincipal instance = new LoginPrincipal();

        private Builder() {
        }

        public Builder userId(Long userId) {
            instance.userId = userId;
            return this;
        }

        public Builder deptId(Long deptId) {
            instance.deptId = deptId;
            return this;
        }

        public Builder deptName(String deptName) {
            instance.deptName = deptName;
            return this;
        }

        public Builder merchantId(Long merchantId) {
            instance.merchantId = merchantId;
            return this;
        }

        public Builder username(String username) {
            instance.username = username;
            return this;
        }

        public Builder password(String password) {
            instance.password = password;
            return this;
        }

        public Builder permissions(Set<String> permissions) {
            instance.permissions = permissions;
            return this;
        }

        public Builder roleKeys(Set<String> roleKeys) {
            instance.roleKeys = roleKeys;
            return this;
        }

        public Builder token(String token) {
            instance.token = token;
            return this;
        }

        public Builder loginTime(Long loginTime) {
            instance.loginTime = loginTime;
            return this;
        }

        public Builder expireTime(Long expireTime) {
            instance.expireTime = expireTime;
            return this;
        }

        public Builder ipaddr(String ipaddr) {
            instance.ipaddr = ipaddr;
            return this;
        }

        public Builder loginLocation(String loginLocation) {
            instance.loginLocation = loginLocation;
            return this;
        }

        public Builder browser(String browser) {
            instance.browser = browser;
            return this;
        }

        public Builder os(String os) {
            instance.os = os;
            return this;
        }

        public LoginPrincipal build() {
            return instance;
        }
    }
}
