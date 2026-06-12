package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.enums.DataScopeType;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysDept;
import com.manzhushaka.db.system.entity.SysUser;
import com.manzhushaka.db.system.mapper.SysDeptMapper;
import com.manzhushaka.db.system.mapper.SysUserMapper;
import com.manzhushaka.system.dto.user.UserForm;
import com.manzhushaka.system.dto.user.UserQuery;
import com.manzhushaka.system.service.UserService;
import com.manzhushaka.system.service.support.SystemAccessSupport;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.user.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SystemAccessSupport accessSupport;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(
        SysUserMapper userMapper,
        SysDeptMapper deptMapper,
        SystemAccessSupport accessSupport
    ) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
        this.accessSupport = accessSupport;
    }

    @Override
    public PageResult<UserVO> page(UserQuery query) {
        accessSupport.assertDeptAccessible(query.getDeptId());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
            .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
            .like(StringUtils.hasText(query.getNickname()), SysUser::getNickname, query.getNickname())
            .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
            .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
            .eq(SysUser::getDeleted, 0)
            .orderByDesc(SysUser::getId);
        applyDataScope(wrapper);
        Page<SysUser> page = userMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        Map<Long, String> deptNameMap = loadDeptNameMap(page.getRecords());
        return SystemMappingSupport.toPageResult(page, user -> toUserVO(user, deptNameMap));
    }

    @Override
    public UserVO getById(Long id) {
        SysUser user = getUserOrThrow(id);
        ensureUserAccessible(user);
        return toUserVO(user, loadDeptNameMap(List.of(user)));
    }

    @Override
    @Transactional
    public Long create(UserForm form) {
        accessSupport.assertDeptAccessible(form.getDeptId());
        SysUser entity = new SysUser();
        applyForm(entity, form);
        entity.setDeleted(0);
        userMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(Long id, UserForm form) {
        SysUser entity = getUserOrThrow(id);
        ensureUserAccessible(entity);
        accessSupport.assertDeptAccessible(form.getDeptId());
        applyForm(entity, form);
        userMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser entity = getUserOrThrow(id);
        ensureUserAccessible(entity);
        entity.setDeleted(1);
        userMapper.updateById(entity);
    }

    private SysUser getUserOrThrow(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private void applyForm(SysUser entity, UserForm form) {
        entity.setUsername(form.getUsername());
        if (StringUtils.hasText(form.getPassword())) {
            entity.setPassword(encodePassword(form.getPassword()));
        }
        entity.setNickname(form.getNickname());
        entity.setDeptId(form.getDeptId());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    /**
     * 对系统用户密码进行 BCrypt 哈希后再入库。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 哈希后的密码
     */
    private String encodePassword(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new BizException(400, "密码不能为空");
        }
        return passwordEncoder.encode(rawPassword.trim());
    }

    /**
     * 按当前登录用户的数据权限收敛查询条件。
     *
     * @param wrapper 用户查询条件
     */
    private void applyDataScope(LambdaQueryWrapper<SysUser> wrapper) {
        DataScopeType scopeType = accessSupport.currentScopeType();
        if (scopeType == DataScopeType.ALL) {
            return;
        }
        if (scopeType == DataScopeType.SELF) {
            wrapper.eq(SysUser::getId, accessSupport.currentUserId());
            return;
        }
        List<Long> accessibleDeptIds = accessSupport.resolveAccessibleDeptIds();
        if (accessibleDeptIds.isEmpty()) {
            wrapper.eq(SysUser::getId, -1L);
            return;
        }
        wrapper.in(SysUser::getDeptId, accessibleDeptIds);
    }

    /**
     * 断言当前用户可访问目标用户。
     *
     * @param user 目标用户
     */
    private void ensureUserAccessible(SysUser user) {
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        DataScopeType scopeType = accessSupport.currentScopeType();
        if (scopeType == DataScopeType.ALL) {
            return;
        }
        if (scopeType == DataScopeType.SELF) {
            if (!user.getId().equals(accessSupport.currentUserId())) {
                throw new BizException(403, "无权访问该用户");
            }
            return;
        }
        accessSupport.assertDeptAccessible(user.getDeptId());
    }

    private Map<Long, String> loadDeptNameMap(List<SysUser> users) {
        List<Long> deptIds = users.stream().map(SysUser::getDeptId).filter(id -> id != null && id > 0).distinct().toList();
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        List<SysDept> depts = deptMapper.selectBatchIds(deptIds);
        Map<Long, String> deptNameMap = new HashMap<>();
        for (SysDept dept : depts) {
            deptNameMap.put(dept.getId(), dept.getDeptName());
        }
        return deptNameMap;
    }

    private UserVO toUserVO(SysUser user, Map<Long, String> deptNameMap) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setDeptId(user.getDeptId());
        vo.setDeptName(deptNameMap.get(user.getDeptId()));
        vo.setStatus(user.getStatus());
        vo.setRoleCodes(userMapper.selectRoleCodes(user.getId()));
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
