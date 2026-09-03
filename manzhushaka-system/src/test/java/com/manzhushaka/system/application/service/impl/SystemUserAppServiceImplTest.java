package com.manzhushaka.system.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.system.application.command.CreateUserCommand;
import com.manzhushaka.system.application.command.ResetPwdCommand;
import com.manzhushaka.system.application.command.UpdateOwnPasswordCommand;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.system.application.result.system.UserExportCursorRow;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
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
    @AfterEach
    void tearDown()
    {
        SensitiveFieldCryptoHolder.clear();
    }

    /**
     * 新增用户时不能使用弱密码。
     */
    @Test
    void createUserShouldRejectWeakPassword()
    {
        SystemUserAppServiceImpl service = buildService();
        ISysUserService userService = (ISysUserService) ReflectionTestUtils.getField(service, "userService");
        CreateUserCommand command = new CreateUserCommand(
                null, "admin", "admin123", "管理员", null, null, "0", null, "0", 103L, new Long[] { 1L });

        assertThatThrownBy(() -> service.createUser(command, "operator"))
                .isInstanceOf(ServiceException.class)
                .hasMessage("密码不能包含用户名");
        verifyNoInteractions(userService);
    }

    /**
     * 管理员重置密码时不能使用弱密码。
     */
    @Test
    void resetPwdShouldRejectWeakPassword()
    {
        SystemUserAppServiceImpl service = buildService();
        ISysUserService userService = (ISysUserService) ReflectionTestUtils.getField(service, "userService");
        ResetPwdCommand command = new ResetPwdCommand(2L, "123456");

        assertThatThrownBy(() -> service.resetPwd(command))
                .isInstanceOf(ServiceException.class)
                .hasMessage("密码长度必须介于8到20个字符之间");
        verifyNoInteractions(userService);
    }

    /**
     * 管理员重置密码时不能包含目标用户名。
     */
    @Test
    void resetPwdShouldRejectPasswordContainsTargetUsername()
    {
        SystemUserAppServiceImpl service = buildService();
        ISysUserService userService = (ISysUserService) ReflectionTestUtils.getField(service, "userService");
        SysUser user = new SysUser();
        user.setUserName("zhangsan");
        when(userService.selectUserById(2L)).thenReturn(user);
        ResetPwdCommand command = new ResetPwdCommand(2L, "Zhangsan@7294");

        assertThatThrownBy(() -> service.resetPwd(command))
                .isInstanceOf(ServiceException.class)
                .hasMessage("密码不能包含用户名");
    }

    /** 当前用户修改密码时不能使用弱密码。 */
    @Test
    void updateOwnPasswordShouldRejectWeakPassword()
    {
        SystemUserAppServiceImpl service = buildService();
        ISysUserService userService = (ISysUserService) ReflectionTestUtils.getField(service, "userService");
        SysUser user = new SysUser();
        user.setPassword(PasswordUtils.encrypt("Old@7294"));
        when(userService.selectUserById(2L)).thenReturn(user);

        assertThatThrownBy(() -> service.updateOwnPassword(new UpdateOwnPasswordCommand(
                2L, "zhangsan", "Old@7294", "123456")))
                .isInstanceOf(ServiceException.class)
                .hasMessage("密码长度必须介于8到20个字符之间");
        verify(userService, never()).resetUserPwd(anyLong(), anyString());
    }

    /**
     * 删除用户必须委托完整业务服务处理权限和关联关系。
     */
    @Test
    void deleteUserShouldDelegateToCompleteUserService()
    {
        SystemUserAppServiceImpl service = buildService();
        ISysUserService userService = (ISysUserService) ReflectionTestUtils.getField(service, "userService");
        Long[] userIds = { 2L, 3L };

        service.deleteUser(userIds);

        verify(userService).deleteUserByIds(userIds);
    }

    @Test
    void listUserExportRowsShouldHashPhoneAndPreserveCursor()
    {
        SensitiveFieldCryptoHolder.setEncryptor(new StubEncryptor());
        SystemUserAppServiceImpl service = buildService();
        ISysUserService userService = (ISysUserService) ReflectionTestUtils.getField(service, "userService");
        Date cursorTime = new Date(1_700_000_000_000L);
        SysDept dept = new SysDept();
        dept.setDeptName("研发部");
        dept.setLeader("负责人");
        SysUser user = new SysUser();
        user.setUserId(12L);
        user.setUserName("operator");
        user.setCreateTime(cursorTime);
        user.setDept(dept);
        when(userService.selectUserExportBatch(any(), any(), anyLong(), anyInt()))
                .thenReturn(List.of(user));
        UserListQuery query = new UserListQuery(null, null, null, "13800138000",
                "0", null, null, null);

        List<UserExportCursorRow> rows = service.listUserExportRows(query, cursorTime, 11L, 500);

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userService).selectUserExportBatch(userCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(cursorTime), org.mockito.ArgumentMatchers.eq(11L),
                org.mockito.ArgumentMatchers.eq(500));
        assertThat(userCaptor.getValue().getPhonenumberHash()).isEqualTo("hash:13800138000");
        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.getUserId()).isEqualTo(12L);
            assertThat(row.getCreateTime()).isEqualTo(cursorTime);
            assertThat(row.getDeptName()).isEqualTo("研发部");
        });
    }

    /**
     * 构建测试服务。
     *
     * @return 系统用户应用服务
     */
    private SystemUserAppServiceImpl buildService()
    {
        SystemUserAppServiceImpl service = new SystemUserAppServiceImpl();
        ReflectionTestUtils.setField(service, "userService", mock(ISysUserService.class));
        ReflectionTestUtils.setField(service, "roleService", mock(ISysRoleService.class));
        ReflectionTestUtils.setField(service, "deptService", mock(ISysDeptService.class));
        return service;
    }

    private static class StubEncryptor implements SensitiveFieldEncryptor
    {
        @Override
        public String encrypt(String plaintext)
        {
            return plaintext;
        }

        @Override
        public String decrypt(String ciphertext)
        {
            return ciphertext;
        }

        @Override
        public String hash(String plaintext)
        {
            return "hash:" + plaintext;
        }

        @Override
        public boolean isCiphertext(String value)
        {
            return false;
        }
    }
}
