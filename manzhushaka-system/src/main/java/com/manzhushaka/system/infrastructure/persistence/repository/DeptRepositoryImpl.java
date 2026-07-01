package com.manzhushaka.system.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.manzhushaka.system.domain.repository.DeptRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.mapper.SysDeptMapper;

/**
 * 部门仓储实现
 *
 * @author manzhushaka
 */
@Repository
public class DeptRepositoryImpl implements DeptRepository
{
    @Autowired
    private SysDeptMapper deptMapper;

    @Override
    public List<SysDept> selectDeptList(SysDept dept)
    {
        return deptMapper.selectDeptList(dept);
    }

    @Override
    public List<Long> selectDeptListByRoleId(Long roleId, boolean deptCheckStrictly)
    {
        return deptMapper.selectDeptListByRoleId(roleId, deptCheckStrictly);
    }

    @Override
    public SysDept selectDeptById(Long deptId)
    {
        return deptMapper.selectDeptById(deptId);
    }

    @Override
    public List<SysDept> selectChildrenDeptById(Long deptId)
    {
        return deptMapper.selectChildrenDeptById(deptId);
    }

    @Override
    public int selectNormalChildrenDeptById(Long deptId)
    {
        return deptMapper.selectNormalChildrenDeptById(deptId);
    }

    @Override
    public int hasChildByDeptId(Long deptId)
    {
        return deptMapper.hasChildByDeptId(deptId);
    }

    @Override
    public int checkDeptExistUser(Long deptId)
    {
        return deptMapper.checkDeptExistUser(deptId);
    }

    @Override
    public SysDept checkDeptNameUnique(String deptName, Long parentId)
    {
        return deptMapper.checkDeptNameUnique(deptName, parentId);
    }

    @Override
    public int insertDept(SysDept dept)
    {
        return deptMapper.insertDept(dept);
    }

    @Override
    public int updateDept(SysDept dept)
    {
        return deptMapper.updateDept(dept);
    }

    @Override
    public void updateDeptStatusNormal(Long[] deptIds)
    {
        deptMapper.updateDeptStatusNormal(deptIds);
    }

    @Override
    public int updateDeptChildren(List<SysDept> depts)
    {
        return deptMapper.updateDeptChildren(depts);
    }

    @Override
    public void updateDeptSort(SysDept dept)
    {
        deptMapper.updateDeptSort(dept);
    }

    @Override
    public int deleteDeptById(Long deptId)
    {
        return deptMapper.deleteDeptById(deptId);
    }

    @Override
    public void checkDeptDataScope(Long deptId)
    {
        // 数据权限校验由上层业务控制，此处不做实现
    }
}
