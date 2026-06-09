package com.manzhushaka.auth.service;

import com.manzhushaka.auth.dto.LoginRequest;
import com.manzhushaka.auth.vo.AuthMenuVO;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysMenu;
import com.manzhushaka.db.system.mapper.SysDeptMapper;
import com.manzhushaka.db.system.mapper.SysLoginLogMapper;
import com.manzhushaka.db.system.mapper.SysMenuMapper;
import com.manzhushaka.db.system.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthServiceTest {

    @Test
    void buildMenuTreeResolvesChildPathAgainstResolvedParentPath() throws Exception {
        AuthService authService = new AuthService(null, null, null, null, null);
        Method buildMenuTree = AuthService.class.getDeclaredMethod("buildMenuTree", List.class);
        buildMenuTree.setAccessible(true);

        SysMenu logs = menu(270L, 0L, "DIR", "日志管理", "/logs", "SystemLogs", null, 3);
        SysMenu loginLogs = menu(271L, 270L, "MENU", "登录日志", "login", "SystemLoginLogs", "system/login-logs", 1);

        @SuppressWarnings("unchecked")
        List<AuthMenuVO> routes = (List<AuthMenuVO>) buildMenuTree.invoke(authService, List.of(logs, loginLogs));

        assertEquals(1, routes.size());
        AuthMenuVO logsRoute = routes.get(0);
        assertEquals("/logs", logsRoute.getPath());
        assertEquals("/logs/login", logsRoute.getRedirect());
        assertEquals(1, logsRoute.getChildren().size());
        assertEquals("/logs/login", logsRoute.getChildren().get(0).getPath());
    }

    @Test
    void loginRejectsInvalidCaptchaBeforeLoadingUser() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        SysDeptMapper deptMapper = mock(SysDeptMapper.class);
        SysMenuMapper menuMapper = mock(SysMenuMapper.class);
        SysLoginLogMapper loginLogMapper = mock(SysLoginLogMapper.class);
        AuthCaptchaService captchaService = mock(AuthCaptchaService.class);
        AuthService authService = new AuthService(userMapper, deptMapper, menuMapper, loginLogMapper, captchaService);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");
        request.setCaptchaKey("captcha-key");
        request.setCaptchaCode("oops");

        BizException expected = new BizException(400, "验证码错误");
        doThrow(expected).when(captchaService).validate("captcha-key", "oops");

        BizException actual = assertThrows(BizException.class, () -> authService.login(request));

        assertEquals("验证码错误", actual.getMessage());
        verifyNoInteractions(userMapper);
    }

    private SysMenu menu(
        Long id,
        Long parentId,
        String menuType,
        String menuName,
        String routePath,
        String routeName,
        String component,
        Integer sort
    ) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuType(menuType);
        menu.setMenuName(menuName);
        menu.setRoutePath(routePath);
        menu.setRouteName(routeName);
        menu.setComponent(component);
        menu.setSort(sort);
        menu.setVisible(1);
        return menu;
    }
}
