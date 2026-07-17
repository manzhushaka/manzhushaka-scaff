package com.manzhushaka.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.mapper.SysUserMapper;
import com.manzhushaka.system.service.ISysConfigService;
import com.manzhushaka.system.service.ISysDeptService;

/**
 * 系统用户服务测试。
 *
 * @author manzhushaka
 * @date 2026-07-02
 */
class SysUserServiceImplTest
{
    /**
     * 清理测试注入的敏感字段加密器。
     */
    @AfterEach
    void tearDown()
    {
        SensitiveFieldCryptoHolder.clear();
    }

    /**
     * 导入新用户时，配置中心的初始密码也不能是弱密码。
     */
    @Test
    void importUserShouldRejectWeakInitialPassword()
    {
        SysUserServiceImpl service = new SysUserServiceImpl();
        SysUserMapper userMapper = mock(SysUserMapper.class);
        ISysConfigService configService = mock(ISysConfigService.class);
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "configService", configService);
        ReflectionTestUtils.setField(service, "deptService", mock(ISysDeptService.class));
        ReflectionTestUtils.setField(service, "validator", Validation.buildDefaultValidatorFactory().getValidator());
        when(userMapper.selectUserByUserName("zhangsan")).thenReturn(null);
        when(configService.selectConfigByKey("sys.user.initPassword")).thenReturn("123456");
        SysUser user = new SysUser();
        user.setUserName("zhangsan");
        user.setNickName("张三");
        user.setDeptId(103L);

        assertThatThrownBy(() -> service.importUser(List.of(user), false, "admin"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("密码长度必须介于8到20个字符之间");
        verify(userMapper, never()).insertUser(any(SysUser.class));
    }

    /**
     * 导入新用户时应在入库前填充手机号和邮箱检索摘要。
     */
    @Test
    void importUserShouldPopulateSensitiveFieldHashes()
    {
        SysUserServiceImpl service = new SysUserServiceImpl();
        SysUserMapper userMapper = mock(SysUserMapper.class);
        ISysConfigService configService = mock(ISysConfigService.class);
        ISysDeptService deptService = mock(ISysDeptService.class);
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "configService", configService);
        ReflectionTestUtils.setField(service, "deptService", deptService);
        ReflectionTestUtils.setField(service, "validator", Validation.buildDefaultValidatorFactory().getValidator());
        SensitiveFieldCryptoHolder.setEncryptor(encryptor);
        when(userMapper.selectUserByUserName("zhangsan")).thenReturn(null);
        when(configService.selectConfigByKey("sys.user.initPassword")).thenReturn("Strong@123");
        when(encryptor.hash(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));
        SysUser user = createImportedUser(103L);

        service.importUser(List.of(user), false, "admin");

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insertUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmailHash()).isEqualTo("hash:zhangsan@example.com");
        assertThat(userCaptor.getValue().getPhonenumberHash()).isEqualTo("hash:13800138000");
    }

    /**
     * 更新导入用户时应校验新部门并刷新敏感字段检索摘要。
     */
    @Test
    void importUserUpdateShouldUseImportedDepartmentAndPopulateHashes()
    {
        SysUserServiceImpl service = spy(new SysUserServiceImpl());
        SysUserMapper userMapper = mock(SysUserMapper.class);
        ISysDeptService deptService = mock(ISysDeptService.class);
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        ReflectionTestUtils.setField(service, "userMapper", userMapper);
        ReflectionTestUtils.setField(service, "deptService", deptService);
        ReflectionTestUtils.setField(service, "validator", Validation.buildDefaultValidatorFactory().getValidator());
        SensitiveFieldCryptoHolder.setEncryptor(encryptor);
        SysUser existingUser = new SysUser();
        existingUser.setUserId(8L);
        existingUser.setDeptId(100L);
        when(userMapper.selectUserByUserName("zhangsan")).thenReturn(existingUser);
        when(encryptor.hash(anyString())).thenAnswer(invocation -> "hash:" + invocation.getArgument(0));
        doNothing().when(service).checkUserDataScope(8L);
        SysUser importedUser = createImportedUser(200L);

        service.importUser(List.of(importedUser), true, "admin");

        verify(deptService).checkDeptDataScope(200L);
        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).updateUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getDeptId()).isEqualTo(200L);
        assertThat(userCaptor.getValue().getEmailHash()).isEqualTo("hash:zhangsan@example.com");
        assertThat(userCaptor.getValue().getPhonenumberHash()).isEqualTo("hash:13800138000");
    }

    /**
     * 创建满足导入校验规则的用户。
     *
     * @param deptId 部门ID
     * @return 导入用户
     */
    private SysUser createImportedUser(Long deptId)
    {
        SysUser user = new SysUser();
        user.setUserName("zhangsan");
        user.setNickName("张三");
        user.setDeptId(deptId);
        user.setEmail("zhangsan@example.com");
        user.setPhonenumber("13800138000");
        return user;
    }
}
