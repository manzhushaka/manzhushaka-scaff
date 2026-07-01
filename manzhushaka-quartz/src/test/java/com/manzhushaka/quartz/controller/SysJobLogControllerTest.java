package com.manzhushaka.quartz.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.PermissionService;
import com.manzhushaka.quartz.domain.SysJobLogDetail;
import com.manzhushaka.quartz.service.ISysJobLogService;

/**
 * 调度日志 Controller 测试。
 *
 * @author manzhushaka
 * @date 2026-07-01
 */
class SysJobLogControllerTest
{
    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void detailListShouldReturnProcessLogDetails()
    {
        ISysJobLogService jobLogService = mock(ISysJobLogService.class);
        SysJobLogController controller = createSecuredController(jobLogService);
        SysJobLogDetail detail = new SysJobLogDetail();
        detail.setJobLogId(100L);
        detail.setLogLevel("INFO");
        detail.setLogContent("开始执行");
        detail.setSortNo(1);
        List<SysJobLogDetail> details = Arrays.asList(detail);
        when(jobLogService.selectJobLogDetailListByJobLogId(100L)).thenReturn(details);
        setLoginPrincipal("monitor:job:query");
        setRequestAttributes();

        AjaxResult result = controller.detailList(100L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get(AjaxResult.DATA_TAG)).isEqualTo(details);
    }

    @Test
    void detailListShouldDenyWithoutQueryPermission()
    {
        ISysJobLogService jobLogService = mock(ISysJobLogService.class);
        SysJobLogController controller = createSecuredController(jobLogService);
        setLoginPrincipal("monitor:job:list");
        setRequestAttributes();

        assertThatThrownBy(() -> controller.detailList(100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    /**
     * 创建带方法权限拦截的 Controller。
     *
     * @param jobLogService 调度日志服务
     * @return Controller 代理
     */
    private SysJobLogController createSecuredController(ISysJobLogService jobLogService)
    {
        SysJobLogController controller = new SysJobLogController();
        ReflectionTestUtils.setField(controller, "jobLogService", jobLogService);

        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("ss", new PermissionService());
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setApplicationContext(applicationContext);
        PreAuthorizeAuthorizationManager manager = new PreAuthorizeAuthorizationManager();
        manager.setExpressionHandler(expressionHandler);

        ProxyFactory proxyFactory = new ProxyFactory(controller);
        proxyFactory.addAdvice(AuthorizationManagerBeforeMethodInterceptor.preAuthorize(manager));
        return (SysJobLogController) proxyFactory.getProxy();
    }

    /**
     * 设置测试登录主体权限。
     *
     * @param permissions 权限字符串
     */
    private void setLoginPrincipal(String... permissions)
    {
        LoginPrincipal principal = LoginPrincipal.builder()
                .userId(2L)
                .username("test")
                .permissions(new HashSet<>(Arrays.asList(permissions)))
                .roleKeys(Collections.emptySet())
                .build();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 绑定测试请求上下文。
     */
    private void setRequestAttributes()
    {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }
}
