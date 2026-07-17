package com.manzhushaka.system.application.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.system.application.command.DataScopeCommand;
import com.manzhushaka.system.service.ISysRoleService;
import com.manzhushaka.system.service.ISysUserService;

/**
 * 系统角色应用服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-17
 */
class SystemRoleAppServiceImplTest
{
    /**
     * 数据权限修改必须委托完整角色服务维护角色部门关联。
     */
    @Test
    void updateDataScopeShouldDelegateToCompleteRoleService()
    {
        SystemRoleAppServiceImpl service = buildService();
        ISysRoleService roleService = (ISysRoleService) ReflectionTestUtils.getField(service, "roleService");
        DataScopeCommand command = new DataScopeCommand(2L, "2", new Long[] { 100L, 101L });

        service.updateDataScope(command, "operator");

        verify(roleService).authDataScope(org.mockito.ArgumentMatchers.argThat(role ->
                role.getRoleId().equals(2L)
                        && "operator".equals(role.getUpdateBy())
                        && role.getDeptIds().length == 2));
    }

    /**
     * 批量授权用户必须委托完整角色服务写入关联表。
     */
    @Test
    void selectAuthUserAllShouldDelegateToCompleteRoleService()
    {
        SystemRoleAppServiceImpl service = buildService();
        ISysRoleService roleService = (ISysRoleService) ReflectionTestUtils.getField(service, "roleService");
        Long[] userIds = { 2L, 3L };

        service.selectAuthUserAll(2L, userIds);

        verify(roleService).checkRoleDataScope(2L);
        verify(roleService).insertAuthUsers(2L, userIds);
    }

    /**
     * 构建测试服务。
     *
     * @return 系统角色应用服务
     */
    private SystemRoleAppServiceImpl buildService()
    {
        SystemRoleAppServiceImpl service = new SystemRoleAppServiceImpl();
        ReflectionTestUtils.setField(service, "roleService", mock(ISysRoleService.class));
        ReflectionTestUtils.setField(service, "userService", mock(ISysUserService.class));
        return service;
    }
}
