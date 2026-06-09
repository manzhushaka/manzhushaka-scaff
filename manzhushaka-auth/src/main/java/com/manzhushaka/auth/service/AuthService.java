package com.manzhushaka.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.manzhushaka.auth.dto.LoginRequest;
import com.manzhushaka.auth.vo.AuthMenuVO;
import com.manzhushaka.auth.vo.LoginResponse;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.enums.DataScopeType;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysDept;
import com.manzhushaka.db.system.entity.SysLoginLog;
import com.manzhushaka.db.system.entity.SysMenu;
import com.manzhushaka.db.system.entity.SysUser;
import com.manzhushaka.db.system.mapper.SysDeptMapper;
import com.manzhushaka.db.system.mapper.SysLoginLogMapper;
import com.manzhushaka.db.system.mapper.SysMenuMapper;
import com.manzhushaka.db.system.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
        SysUserMapper sysUserMapper,
        SysDeptMapper sysDeptMapper,
        SysMenuMapper sysMenuMapper,
        SysLoginLogMapper sysLoginLogMapper
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysLoginLogMapper = sysLoginLogMapper;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            writeLoginLog(request.getUsername(), "FAIL", "用户名或密码错误");
            throw new BizException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            writeLoginLog(request.getUsername(), "FAIL", "账号已停用");
            throw new BizException(403, "账号已停用");
        }
        boolean matched = request.getPassword().equals(user.getPassword()) || passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matched) {
            writeLoginLog(request.getUsername(), "FAIL", "用户名或密码错误");
            throw new BizException(401, "用户名或密码错误");
        }

        StpUtil.login(user.getId());
        LoginUser loginUser = loadLoginUser(user.getId());
        StpUtil.getSession().set("loginUser", loginUser);
        writeLoginLog(request.getUsername(), "SUCCESS", "登录成功");

        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        response.setUserInfo(toUserInfo(loginUser, user.getNickname(), resolveDeptName(user.getDeptId())));
        return response;
    }

    public void logout() {
        StpUtil.logout();
    }

    public LoginResponse.UserInfo currentUser() {
        SysUser user = currentUserEntity();
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toUserInfo(loadLoginUser(user.getId()), user.getNickname(), resolveDeptName(user.getDeptId()));
    }

    public List<AuthMenuVO> currentMenus() {
        SysUser user = currentUserEntity();
        return buildMenuTree(sysMenuMapper.selectMenusByUserId(user.getId()));
    }

    public List<String> currentPermissions() {
        LoginUser loginUser = currentLoginUser();
        return loginUser.getPermCodes();
    }

    public LoginUser loadLoginUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRoleCodes(sysUserMapper.selectRoleCodes(userId));
        loginUser.setPermCodes(sysUserMapper.selectPermCodes(userId));
        loginUser.setDataScopes(sysUserMapper.selectDataScopes(userId).stream().map(this::toScope).toList());
        return loginUser;
    }

    private SysUser currentUserEntity() {
        Long userId = currentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private LoginUser currentLoginUser() {
        Object loginUser = StpUtil.getSession().get("loginUser");
        if (loginUser instanceof LoginUser value) {
            return value;
        }
        LoginUser resolved = loadLoginUser(currentUserId());
        StpUtil.getSession().set("loginUser", resolved);
        return resolved;
    }

    private Long currentUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BizException(401, "未登录");
        }
        return Long.parseLong(String.valueOf(loginId));
    }

    private String resolveDeptName(Long deptId) {
        if (deptId == null || deptId <= 0) {
            return null;
        }
        SysDept dept = sysDeptMapper.selectById(deptId);
        return dept == null ? null : dept.getDeptName();
    }

    private List<AuthMenuVO> buildMenuTree(List<SysMenu> menus) {
        Map<Long, AuthMenuVO> menuMap = new LinkedHashMap<>();
        Map<Long, SysMenu> sourceMap = new LinkedHashMap<>();
        List<SysMenu> sortedMenus = menus.stream()
            .sorted(Comparator.comparing((SysMenu item) -> item.getSort() == null ? 0 : item.getSort()).thenComparing(SysMenu::getId))
            .toList();

        for (SysMenu menu : sortedMenus) {
            sourceMap.put(menu.getId(), menu);
            AuthMenuVO node = new AuthMenuVO();
            node.setId(menu.getId());
            node.setName(StringUtils.hasText(menu.getRouteName()) ? menu.getRouteName() : "menu-" + menu.getId());
            node.setType(menu.getMenuType());
            node.setTitle(menu.getMenuName());
            node.setIcon(menu.getIcon());
            node.setHidden(Integer.valueOf(0).equals(menu.getVisible()));
            node.setPermission(menu.getPerms());
            menuMap.put(menu.getId(), node);
        }

        List<AuthMenuVO> roots = new ArrayList<>();
        for (Map.Entry<Long, AuthMenuVO> entry : menuMap.entrySet()) {
            SysMenu source = sourceMap.get(entry.getKey());
            Long parentId = source.getParentId();
            if (parentId == null || parentId == 0L || !menuMap.containsKey(parentId)) {
                attachResolvedRoute(entry.getValue(), source, null);
                roots.add(entry.getValue());
                continue;
            }
            AuthMenuVO parent = menuMap.get(parentId);
            attachResolvedRoute(entry.getValue(), source, parent);
            parent.getChildren().add(entry.getValue());
        }

        fillRedirects(roots);
        return roots;
    }

    private void attachResolvedRoute(AuthMenuVO node, SysMenu source, AuthMenuVO parent) {
        node.setPath(resolvePath(source, parent));
        node.setComponent(resolveComponent(source));
    }

    private void fillRedirects(List<AuthMenuVO> nodes) {
        for (AuthMenuVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                AuthMenuVO firstVisibleChild = node.getChildren().stream()
                    .filter(child -> !"BUTTON".equals(child.getType()))
                    .findFirst()
                    .orElse(null);
                if (firstVisibleChild != null) {
                    node.setRedirect(resolveRedirect(firstVisibleChild));
                }
                fillRedirects(node.getChildren());
            }
        }
    }

    private String resolveRedirect(AuthMenuVO menu) {
        if (!menu.getChildren().isEmpty()) {
            AuthMenuVO firstVisibleChild = menu.getChildren().stream()
                .filter(child -> !"BUTTON".equals(child.getType()))
                .findFirst()
                .orElse(null);
            if (firstVisibleChild != null) {
                return resolveRedirect(firstVisibleChild);
            }
        }
        return menu.getPath();
    }

    private String resolvePath(SysMenu menu, AuthMenuVO parent) {
        if (!StringUtils.hasText(menu.getRoutePath())) {
            return "BUTTON".equals(menu.getMenuType()) ? "" : fallbackPath(menu, parent);
        }
        String routePath = menu.getRoutePath().trim();
        if (routePath.startsWith("/")) {
            return routePath;
        }
        if (parent == null || !StringUtils.hasText(parent.getPath())) {
            return "/" + routePath;
        }
        return trimTrailingSlash(parent.getPath()) + "/" + routePath;
    }

    private String resolveComponent(SysMenu menu) {
        if (!StringUtils.hasText(menu.getComponent())) {
            return null;
        }
        return menu.getComponent().trim();
    }

    private String fallbackPath(SysMenu menu, AuthMenuVO parent) {
        if (parent == null || !StringUtils.hasText(parent.getPath())) {
            return "/" + menu.getId();
        }
        return trimTrailingSlash(parent.getPath()) + "/" + menu.getId();
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value) || "/".equals(value)) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private void writeLoginLog(String username, String loginStatus, String message) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginStatus(loginStatus);
        loginLog.setMessage(message);
        loginLog.setCreateTime(LocalDateTime.now());
        HttpServletRequest request = currentRequest();
        if (request != null) {
            loginLog.setIp(request.getRemoteAddr());
            loginLog.setUserAgent(request.getHeader("User-Agent"));
        }
        sysLoginLogMapper.insert(loginLog);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private DataScopeType toScope(String value) {
        if (value == null) {
            return DataScopeType.SELF;
        }
        try {
            return DataScopeType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return switch (value) {
                case "4" -> DataScopeType.ALL;
                case "3" -> DataScopeType.DEPT_AND_CHILD;
                case "2" -> DataScopeType.DEPT;
                default -> DataScopeType.SELF;
            };
        }
    }

    private LoginResponse.UserInfo toUserInfo(LoginUser loginUser, String nickname, String deptName) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(loginUser.getUserId());
        userInfo.setUsername(loginUser.getUsername());
        userInfo.setNickname(nickname);
        userInfo.setDeptId(loginUser.getDeptId());
        userInfo.setDeptName(deptName);
        userInfo.setRoleCodes(loginUser.getRoleCodes());
        userInfo.setPermCodes(loginUser.getPermCodes());
        return userInfo;
    }
}
