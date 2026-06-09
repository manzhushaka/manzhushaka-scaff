package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysRole;
import com.manzhushaka.db.system.mapper.SysRoleMapper;
import com.manzhushaka.system.dto.role.RoleForm;
import com.manzhushaka.system.dto.role.RoleQuery;
import com.manzhushaka.system.service.RoleService;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.role.RoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;

    public RoleServiceImpl(SysRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public PageResult<RoleVO> page(RoleQuery query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
            .like(StringUtils.hasText(query.getRoleCode()), SysRole::getRoleCode, query.getRoleCode())
            .like(StringUtils.hasText(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
            .eq(query.getStatus() != null, SysRole::getStatus, query.getStatus())
            .eq(SysRole::getDeleted, 0)
            .orderByDesc(SysRole::getId);
        Page<SysRole> page = roleMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toRoleVO);
    }

    @Override
    public List<LabelValueOption> options() {
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getDeleted, 0)
            .eq(SysRole::getStatus, 1)
            .orderByAsc(SysRole::getId));
        return SystemMappingSupport.mapList(roles, role -> new LabelValueOption(role.getRoleName(), String.valueOf(role.getId())));
    }

    @Override
    public RoleVO getById(Long id) {
        return toRoleVO(getRoleOrThrow(id));
    }

    @Override
    @Transactional
    public Long create(RoleForm form) {
        SysRole entity = new SysRole();
        applyForm(entity, form);
        entity.setDeleted(0);
        roleMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(Long id, RoleForm form) {
        SysRole entity = getRoleOrThrow(id);
        applyForm(entity, form);
        roleMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysRole entity = getRoleOrThrow(id);
        entity.setDeleted(1);
        roleMapper.updateById(entity);
    }

    private SysRole getRoleOrThrow(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || Integer.valueOf(1).equals(role.getDeleted())) {
            throw new BizException(404, "角色不存在");
        }
        return role;
    }

    private void applyForm(SysRole entity, RoleForm form) {
        entity.setRoleCode(form.getRoleCode());
        entity.setRoleName(form.getRoleName());
        entity.setDataScope(form.getDataScope());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }
}
