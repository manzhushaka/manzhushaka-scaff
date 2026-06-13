package com.manzhushaka.system.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.context.LoginUserContext;
import com.manzhushaka.common.enums.DataScopeType;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysDept;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysDeptMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供系统管理模块的通用访问控制能力。
 */
@Component
public class SystemAccessSupport {

    private final SysDeptMapper deptMapper;

    /**
     * 创建 SystemAccessSupport 实例。
     *
     * @param deptMapper deptMapper 参数
     */
    public SystemAccessSupport(SysDeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    /**
     * 获取当前登录用户。
     *
     * @return 当前登录用户
     */
    public LoginUser requireCurrentUser() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            throw new BizException(401, "未登录");
        }
        return loginUser;
    }

    /**
     * 判断当前用户是否具备全部数据权限。
     *
     * @return true 表示具备全部数据权限
     */
    public boolean hasAllDataScope() {
        return resolveScopeType(requireCurrentUser()) == DataScopeType.ALL;
    }

    /**
     * 获取当前用户数据权限级别。
     *
     * @return 当前用户的最大数据权限级别
     */
    public DataScopeType currentScopeType() {
        return resolveScopeType(requireCurrentUser());
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前登录用户 ID
     */
    public Long currentUserId() {
        return requireCurrentUser().getUserId();
    }

    /**
     * 解析当前用户可访问的部门 ID 集合。
     *
     * @return 可访问部门 ID 集合
     */
    public List<Long> resolveAccessibleDeptIds() {
        LoginUser loginUser = requireCurrentUser();
        DataScopeType scopeType = resolveScopeType(loginUser);
        Long deptId = loginUser.getDeptId();
        if (scopeType == DataScopeType.ALL || deptId == null || deptId <= 0) {
            return List.of();
        }
        if (scopeType == DataScopeType.SELF || scopeType == DataScopeType.DEPT) {
            return List.of(deptId);
        }
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(deptId);
        deptIds.addAll(deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .like(SysDept::getAncestorPath, "," + deptId + ",")
                .orderByAsc(SysDept::getId))
            .stream()
            .map(SysDept::getId)
            .filter(id -> id != null && id > 0)
            .toList());
        return deptIds.stream().distinct().toList();
    }

    /**
     * 断言当前用户可以访问指定部门。
     *
     * @param deptId 目标部门 ID
     */
    public void assertDeptAccessible(Long deptId) {
        if (deptId == null || deptId <= 0 || hasAllDataScope()) {
            return;
        }
        List<Long> accessibleDeptIds = resolveAccessibleDeptIds();
        if (!accessibleDeptIds.contains(deptId)) {
            throw new BizException(403, "无权访问该部门数据");
        }
    }

    /**
     * 断言当前用户可以访问指定导入导出任务。
     *
     * @param task 任务实体
     */
    public void assertTaskAccessible(SysImportExportTask task) {
        if (task == null) {
            throw new BizException(404, "任务不存在");
        }
        if (hasAllDataScope()) {
            return;
        }
        String currentUsername = requireCurrentUser().getUsername();
        if (!StringUtils.hasText(currentUsername) || !currentUsername.equals(task.getCreateBy())) {
            throw new BizException(403, "无权访问该任务");
        }
    }

    /**
     * 构建 resolve Scope Type 结果。
     *
     * @param loginUser loginUser 参数
     * @return 处理结果
     */
    private DataScopeType resolveScopeType(LoginUser loginUser) {
        return loginUser.getDataScopes()
            .stream()
            .reduce(DataScopeType.SELF, DataScopeType::max);
    }
}
