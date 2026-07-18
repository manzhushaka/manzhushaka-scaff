package com.manzhushaka.system.application.service.impl;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.constant.UserConstants;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.application.command.SaveDeptCommand;
import com.manzhushaka.system.application.query.DeptQuery;
import com.manzhushaka.system.application.result.shared.TreeNodeResult;
import com.manzhushaka.system.application.result.system.DeptResult;
import com.manzhushaka.system.application.result.system.SystemResultMapper;
import com.manzhushaka.system.application.service.SystemDeptAppService;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.service.ISysDeptService;

/**
 * 系统部门应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class SystemDeptAppServiceImpl implements SystemDeptAppService
{
    @Autowired
    private ISysDeptService deptService;

    @Override
    public List<DeptResult> listDeptResults(DeptQuery query)
    {
        return SystemResultMapper.toDeptResults(deptService.selectDeptList(toEntity(query)));
    }

    @Override
    public List<DeptResult> listDeptResultsExcluding(Long deptId)
    {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(dept -> dept.getDeptId().equals(deptId)
                || ArrayUtils.contains(StringUtils.split(dept.getAncestors(), ","), String.valueOf(deptId)));
        return SystemResultMapper.toDeptResults(depts);
    }

    @Override
    public List<TreeNodeResult> listDeptTree(DeptQuery query)
    {
        return deptService.selectDeptTreeList(toEntity(query));
    }

    @Override
    public DeptResult getDeptResult(Long deptId)
    {
        deptService.checkDeptDataScope(deptId);
        return SystemResultMapper.toDeptResult(deptService.selectDeptById(deptId));
    }

    @Override
    public List<Long> listCheckedDeptIds(Long roleId)
    {
        return deptService.selectDeptListByRoleId(roleId);
    }

    @Override
    @Transactional
    public int createDept(SaveDeptCommand command, String operatorUsername)
    {
        SysDept dept = toEntity(command);
        if (!deptService.checkDeptNameUnique(dept))
        {
            throw new ServiceException("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        dept.setCreateBy(operatorUsername);
        return deptService.insertDept(dept);
    }

    @Override
    @Transactional
    public int updateDept(SaveDeptCommand command, String operatorUsername)
    {
        SysDept dept = toEntity(command);
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept))
        {
            throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        if (dept.getParentId().equals(deptId))
        {
            throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus())
                && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            throw new ServiceException("该部门包含未停用的子部门！");
        }
        dept.setUpdateBy(operatorUsername);
        return deptService.updateDept(dept);
    }

    @Override
    public void updateDeptSort(String[] deptIds, String[] orderNums)
    {
        deptService.updateDeptSort(deptIds, orderNums);
    }

    @Override
    @Transactional
    public int deleteDept(Long deptId)
    {
        if (deptService.hasChildByDeptId(deptId))
        {
            throw new ServiceException("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId))
        {
            throw new ServiceException("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return deptService.deleteDeptById(deptId);
    }

    private SysDept toEntity(DeptQuery query)
    {
        SysDept dept = new SysDept();
        if (query != null)
        {
            dept.setDeptName(query.deptName());
            dept.setStatus(query.status());
            dept.setDeptType(query.deptType());
            dept.setRegionCode(query.regionCode());
            dept.setRegionLevel(query.regionLevel());
        }
        return dept;
    }

    private SysDept toEntity(SaveDeptCommand command)
    {
        SysDept dept = new SysDept();
        dept.setDeptId(command.deptId());
        dept.setParentId(command.parentId());
        dept.setDeptName(command.deptName());
        dept.setOrderNum(command.orderNum());
        dept.setLeader(command.leader());
        dept.setPhone(command.phone());
        dept.setEmail(command.email());
        dept.setStatus(command.status());
        dept.setDeptType(command.deptType());
        dept.setRegionCode(command.regionCode());
        dept.setRegionLevel(command.regionLevel());
        return dept;
    }
}
