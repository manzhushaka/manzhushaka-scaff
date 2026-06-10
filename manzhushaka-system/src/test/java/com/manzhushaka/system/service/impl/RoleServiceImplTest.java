package com.manzhushaka.system.service.impl;

import com.manzhushaka.common.enums.DataScopeType;
import com.manzhushaka.db.system.entity.SysRole;
import com.manzhushaka.db.system.entity.SysRoleMenu;
import com.manzhushaka.db.system.mapper.SysRoleMapper;
import com.manzhushaka.db.system.mapper.SysRoleMenuMapper;
import com.manzhushaka.system.dto.role.RoleForm;
import com.manzhushaka.system.vo.role.RoleVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleServiceImplTest {

    @Test
    void shouldCreateRoleAndPersistAssignedMenus() {
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleMenuMapper roleMenuMapper = mock(SysRoleMenuMapper.class);
        RoleServiceImpl service = new RoleServiceImpl(roleMapper, roleMenuMapper);

        RoleForm form = buildRoleForm(List.of(101L, 102L));
        when(roleMapper.insert(any(SysRole.class))).thenAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(200L);
            return 1;
        });

        Long roleId = service.create(form);

        ArgumentCaptor<SysRoleMenu> roleMenuCaptor = ArgumentCaptor.forClass(SysRoleMenu.class);
        verify(roleMenuMapper, times(2)).insert(roleMenuCaptor.capture());
        assertEquals(200L, roleId);
        assertEquals(List.of(101L, 102L), roleMenuCaptor.getAllValues().stream().map(SysRoleMenu::getMenuId).toList());
        assertEquals(List.of(200L, 200L), roleMenuCaptor.getAllValues().stream().map(SysRoleMenu::getRoleId).toList());
    }

    @Test
    void shouldLoadRoleDetailWithAssignedMenuIds() {
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleMenuMapper roleMenuMapper = mock(SysRoleMenuMapper.class);
        RoleServiceImpl service = new RoleServiceImpl(roleMapper, roleMenuMapper);

        SysRole role = new SysRole();
        role.setId(88L);
        role.setRoleCode("AUDITOR");
        role.setRoleName("审计员");
        role.setDataScope(DataScopeType.SELF);
        role.setStatus(1);
        role.setDeleted(0);
        when(roleMapper.selectById(88L)).thenReturn(role);

        SysRoleMenu dashboard = new SysRoleMenu();
        dashboard.setRoleId(88L);
        dashboard.setMenuId(300L);
        SysRoleMenu reports = new SysRoleMenu();
        reports.setRoleId(88L);
        reports.setMenuId(301L);
        when(roleMenuMapper.selectList(any())).thenReturn(List.of(reports, dashboard));

        RoleVO detail = service.getById(88L);

        assertEquals(List.of(300L, 301L), detail.getMenuIds());
    }

    @Test
    void shouldReplaceRoleMenuAssignmentsWhenUpdatingRole() {
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleMenuMapper roleMenuMapper = mock(SysRoleMenuMapper.class);
        RoleServiceImpl service = new RoleServiceImpl(roleMapper, roleMenuMapper);

        SysRole role = new SysRole();
        role.setId(77L);
        role.setDeleted(0);
        when(roleMapper.selectById(77L)).thenReturn(role);

        RoleForm form = buildRoleForm(List.of(401L, 402L));

        service.update(77L, form);

        verify(roleMenuMapper).delete(any());
        ArgumentCaptor<SysRoleMenu> roleMenuCaptor = ArgumentCaptor.forClass(SysRoleMenu.class);
        verify(roleMenuMapper, times(2)).insert(roleMenuCaptor.capture());
        assertEquals(List.of(401L, 402L), roleMenuCaptor.getAllValues().stream().map(SysRoleMenu::getMenuId).toList());
    }

    private RoleForm buildRoleForm(List<Long> menuIds) {
        RoleForm form = new RoleForm();
        form.setRoleCode("OPS");
        form.setRoleName("运维角色");
        form.setDataScope(DataScopeType.ALL);
        form.setStatus(1);
        form.setMenuIds(menuIds);
        return form;
    }
}
