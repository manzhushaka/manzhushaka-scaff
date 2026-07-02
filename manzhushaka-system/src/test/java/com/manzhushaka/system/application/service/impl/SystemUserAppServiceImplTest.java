package com.manzhushaka.system.application.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.domain.repository.UserRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysDeptService;
import com.manzhushaka.system.service.ISysRoleService;
import com.manzhushaka.system.service.ISysUserService;

/**
 * 系统用户应用服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-02
 */
class SystemUserAppServiceImplTest
{
    /**
     * 新增用户时不能使用弱密码。
     */
    @Test
    void createUserShouldRejectWeakPassword()
    {
        SystemUserAppServiceImpl service = buildService();
        UserRepository userRepository = (UserRepository) ReflectionTestUtils.getField(service, "userRepository");
        CreateUserCommand command = new CreateUserCommand(
                null, "admin", "admin123", "管理员", null, null, "0", null, "0", 103L, new Long[] { 1L });

        assertThatThrownBy(() -> service.createUser(command))
                .isInstanceOf(ServiceException.class)
                .hasMessage("密码不能包含用户名");
        verifyNoInteractions(userRepository);
    }

    /**
     * 管理员重置密码时不能使用弱密码。
     */
    @Test
    void resetPwdShouldRejectWeakPassword()
    {
        SystemUserAppServiceImpl service = buildService();
        UserRepository userRepository = (UserRepository) ReflectionTestUtils.getField(service, "userRepository");
        ResetPwdCommand command = new ResetPwdCommand(2L, "123456");

        assertThatThrownBy(() -> service.resetPwd(command))
                .isInstanceOf(ServiceException.class)
                .hasMessage("密码长度必须介于8到20个字符之间");
        verifyNoInteractions(userRepository);
    }

    /**
     * 管理员重置密码时不能包含目标用户名。
     */
    @Test
    void resetPwdShouldRejectPasswordContainsTargetUsername()
    {
        SystemUserAppServiceImpl service = buildService();
        UserRepository userRepository = (UserRepository) ReflectionTestUtils.getField(service, "userRepository");
        SysUser user = new SysUser();
        user.setUserName("zhangsan");
        when(userRepository.selectUserById(2L)).thenReturn(user);
        ResetPwdCommand command = new ResetPwdCommand(2L, "Zhangsan@7294");

        assertThatThrownBy(() -> service.resetPwd(command))
                .isInstanceOf(ServiceException.class)
                .hasMessage("密码不能包含用户名");
    }

    /**
     * 构建测试服务。
     *
     * @return 系统用户应用服务
     */
    private SystemUserAppServiceImpl buildService()
    {
        SystemUserAppServiceImpl service = new SystemUserAppServiceImpl();
        ReflectionTestUtils.setField(service, "userRepository", mock(UserRepository.class));
        ReflectionTestUtils.setField(service, "userService", mock(ISysUserService.class));
        ReflectionTestUtils.setField(service, "roleService", mock(ISysRoleService.class));
        ReflectionTestUtils.setField(service, "deptService", mock(ISysDeptService.class));
        return service;
    }
}
