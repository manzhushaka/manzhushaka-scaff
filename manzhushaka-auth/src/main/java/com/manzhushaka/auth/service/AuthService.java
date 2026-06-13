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
import org.springframework.security.crypto.password.PasswordEncoder;
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

/**
 * 定义 AuthService。
 */
@Service
public class AuthService {
    private final SysUserMapper sysUserMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final AuthCaptchaService authCaptchaService;
    /**
     * 执行 BCrypt Password Encoder 逻辑。
     *
     * @return 处理结果
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
        SysUserMapper sysUserMapper,
        SysDeptMapper sysDeptMapper,
        SysMenuMapper sysMenuMapper,
        SysLoginLogMapper sysLoginLogMapper,
        AuthCaptchaService authCaptchaService
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysLoginLogMapper = sysLoginLogMapper;
        this.authCaptchaService = authCaptchaService;
    }

    /**
     * 执行登录。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    public LoginResponse login(LoginRequest request) {
        String loginPrincipal = buildLoginPrincipal(request.getUsername());
        authCaptchaService.assertLoginAllowed(loginPrincipal);
        try {
            authCaptchaService.validate(request.getCaptchaKey(), request.getCaptchaCode());
        } catch (BizException exception) {
            authCaptchaService.recordLoginFailure(loginPrincipal);
            writeLoginLog(request.getUsername(), "FAIL", exception.getMessage());
            throw exception;
        }
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            authCaptchaService.recordLoginFailure(loginPrincipal);
            writeLoginLog(request.getUsername(), "FAIL", "用户名或密码错误");
            throw new BizException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            authCaptchaService.recordLoginFailure(loginPrincipal);
            writeLoginLog(request.getUsername(), "FAIL", "账号已停用");
            throw new BizException(403, "账号已停用");
        }
        boolean matched = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matched) {
            authCaptchaService.recordLoginFailure(loginPrincipal);
            writeLoginLog(request.getUsername(), "FAIL", "用户名或密码错误");
            throw new BizException(401, "用户名或密码错误");
        }

        StpUtil.login(user.getId());
        LoginUser loginUser = loadLoginUser(user.getId());
        StpUtil.getSession().set("loginUser", loginUser);
        authCaptchaService.clearLoginFailures(loginPrincipal);
        writeLoginLog(request.getUsername(), "SUCCESS", "登录成功");

        LoginResponse response = new LoginResponse();
        response.setUserInfo(toUserInfo(loginUser, user.getNickname(), resolveDeptName(user.getDeptId())));
        return response;
    }

    /**
     * 对用户密码进行 BCrypt 哈希，供系统模块与初始化流程复用。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 哈希后的密码串
     */
    public String encodePassword(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new BizException(400, "密码不能为空");
        }
        return passwordEncoder.encode(rawPassword.trim());
    }

    /**
     * 执行登出。
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 查询当前用户。
     *
     * @return 查询结果
     */
    public LoginResponse.UserInfo currentUser() {
        SysUser user = currentUserEntity();
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toUserInfo(loadLoginUser(user.getId()), user.getNickname(), resolveDeptName(user.getDeptId()));
    }

    /**
     * 查询当前用户菜单。
     *
     * @return 查询结果
     */
    public List<AuthMenuVO> currentMenus() {
        SysUser user = currentUserEntity();
        return buildMenuTree(sysMenuMapper.selectMenusByUserId(user.getId()));
    }

    /**
     * 查询当前用户权限。
     *
     * @return 查询结果
     */
    public List<String> currentPermissions() {
        LoginUser loginUser = currentLoginUser();
        return loginUser.getPermCodes();
    }

    /**
     * 查询 load Login User 结果。
     *
     * @param userId 用户 ID
     * @return 查询结果
     */
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

    /**
     * 生成登录限流主体，优先绑定用户名与来源 IP。
     *
     * @param username 登录用户名
     * @return 登录限流主体
     */
    private String buildLoginPrincipal(String username) {
        HttpServletRequest request = currentRequest();
        String remoteAddr = request == null ? "" : request.getRemoteAddr();
        String normalizedUsername = StringUtils.hasText(username) ? username.trim() : "anonymous";
        return normalizedUsername + "@" + remoteAddr;
    }

    /**
     * 查询 current User Entity 结果。
     *
     * @return 查询结果
     */
    private SysUser currentUserEntity() {
        Long userId = currentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    /**
     * 查询 current Login User 结果。
     *
     * @return 查询结果
     */
    private LoginUser currentLoginUser() {
        Object loginUser = StpUtil.getSession().get("loginUser");
        if (loginUser instanceof LoginUser value) {
            return value;
        }
        LoginUser resolved = loadLoginUser(currentUserId());
        StpUtil.getSession().set("loginUser", resolved);
        return resolved;
    }

    /**
     * 查询 current User Id 结果。
     *
     * @return 查询结果
     */
    private Long currentUserId() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            throw new BizException(401, "未登录");
        }
        return Long.parseLong(String.valueOf(loginId));
    }

    /**
     * 构建 resolve Dept Name 结果。
     *
     * @param deptId 部门 ID
     * @return 处理结果
     */
    private String resolveDeptName(Long deptId) {
        if (deptId == null || deptId <= 0) {
            return null;
        }
        SysDept dept = sysDeptMapper.selectById(deptId);
        return dept == null ? null : dept.getDeptName();
    }

    /**
     * 构建菜单树。
     *
     * @param menus menus 参数
     * @return 处理结果
     */
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
                roots.add(entry.getValue());
                continue;
            }
            AuthMenuVO parent = menuMap.get(parentId);
            parent.getChildren().add(entry.getValue());
        }

        populateResolvedRoutes(roots, sourceMap, null);
        fillRedirects(roots);
        return roots;
    }

    /**
     * 更新 populate Resolved Routes 数据。
     *
     * @param nodes nodes 参数
     * @param sourceMap sourceMap 参数
     * @param parent parent 参数
     */
    private void populateResolvedRoutes(List<AuthMenuVO> nodes, Map<Long, SysMenu> sourceMap, AuthMenuVO parent) {
        for (AuthMenuVO node : nodes) {
            SysMenu source = sourceMap.get(node.getId());
            attachResolvedRoute(node, source, parent);
            if (!node.getChildren().isEmpty()) {
                populateResolvedRoutes(node.getChildren(), sourceMap, node);
            }
        }
    }

    /**
     * 更新 attach Resolved Route 数据。
     *
     * @param node node 参数
     * @param source source 参数
     * @param parent parent 参数
     */
    private void attachResolvedRoute(AuthMenuVO node, SysMenu source, AuthMenuVO parent) {
        node.setPath(resolvePath(source, parent));
        node.setComponent(resolveComponent(source));
    }

    /**
     * 更新 fill Redirects 数据。
     *
     * @param nodes nodes 参数
     */
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

    /**
     * 构建 resolve Redirect 结果。
     *
     * @param menu menu 参数
     * @return 处理结果
     */
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

    /**
     * 构建 resolve Path 结果。
     *
     * @param menu menu 参数
     * @param parent parent 参数
     * @return 处理结果
     */
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

    /**
     * 构建 resolve Component 结果。
     *
     * @param menu menu 参数
     * @return 处理结果
     */
    private String resolveComponent(SysMenu menu) {
        if (!StringUtils.hasText(menu.getComponent())) {
            return null;
        }
        return menu.getComponent().trim();
    }

    /**
     * 执行 fallback Path 逻辑。
     *
     * @param menu menu 参数
     * @param parent parent 参数
     * @return 处理结果
     */
    private String fallbackPath(SysMenu menu, AuthMenuVO parent) {
        if (parent == null || !StringUtils.hasText(parent.getPath())) {
            return "/" + menu.getId();
        }
        return trimTrailingSlash(parent.getPath()) + "/" + menu.getId();
    }

    /**
     * 执行 trim Trailing Slash 逻辑。
     *
     * @param value 字段值
     * @return 处理结果
     */
    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value) || "/".equals(value)) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /**
     * 更新 write Login Log 数据。
     *
     * @param username 用户名
     * @param loginStatus loginStatus 参数
     * @param message message 参数
     */
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

    /**
     * 查询 current Request 结果。
     *
     * @return 查询结果
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 构建 to Scope 结果。
     *
     * @param value 字段值
     * @return 处理结果
     */
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

    /**
     * 构建 to User Info 结果。
     *
     * @param loginUser loginUser 参数
     * @param nickname nickname 参数
     * @param deptName deptName 参数
     * @return 处理结果
     */
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
