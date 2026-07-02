package com.manzhushaka.system.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
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
}
