package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysDept;
import com.manzhushaka.db.system.entity.SysUser;
import com.manzhushaka.db.system.mapper.SysDeptMapper;
import com.manzhushaka.db.system.mapper.SysUserMapper;
import com.manzhushaka.system.dto.dept.DeptForm;
import com.manzhushaka.system.dto.dept.DeptQuery;
import com.manzhushaka.system.service.DeptService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.dept.DeptTreeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现 DeptServiceImpl 业务服务。
 */
@Service
public class DeptServiceImpl implements DeptService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    /**
     * 创建 DeptServiceImpl 实例。
     *
     * @param deptMapper deptMapper 参数
     * @param userMapper userMapper 参数
     */
    public DeptServiceImpl(SysDeptMapper deptMapper, SysUserMapper userMapper) {
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
    }

    /**
     * 执行 tree 逻辑。
     *
     * @param query 查询条件
     * @return 处理结果
     */
    @Override
    public List<DeptTreeVO> tree(DeptQuery query) {
        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
            .like(StringUtils.hasText(query.getDeptName()), SysDept::getDeptName, query.getDeptName())
            .eq(query.getStatus() != null, SysDept::getStatus, query.getStatus())
            .orderByAsc(SysDept::getSort, SysDept::getId));
        return buildTree(depts.stream().map(this::toDeptTreeVO).toList());
    }

    /**
     * 查询下拉选项。
     *
     * @return 查询结果
     */
    @Override
    public List<LabelValueOption> options() {
        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getStatus, 1)
            .orderByAsc(SysDept::getSort, SysDept::getId));
        return depts.stream().map(dept -> new LabelValueOption(dept.getDeptName(), String.valueOf(dept.getId()))).toList();
    }

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @Override
    public DeptTreeVO getById(Long id) {
        return toDeptTreeVO(getDeptOrThrow(id));
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @Override
    @Transactional
    public Long create(DeptForm form) {
        SysDept entity = new SysDept();
        applyForm(entity, form);
        entity.setAncestorPath(buildAncestorPath(form.getParentId()));
        deptMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    @Override
    @Transactional
    public void update(Long id, DeptForm form) {
        SysDept entity = getDeptOrThrow(id);
        applyForm(entity, form);
        entity.setAncestorPath(buildAncestorPath(form.getParentId()));
        deptMapper.updateById(entity);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        assertNoChildDept(id);
        assertNoActiveUser(id);
        if (deptMapper.deleteById(id) == 0) {
            throw new BizException(404, "部门不存在");
        }
    }

    /**
     * 获取部门，不存在时抛出异常。
     *
     * @param id 部门 ID
     * @return 部门实体
     */
    private SysDept getDeptOrThrow(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BizException(404, "部门不存在");
        }
        return dept;
    }

    /**
     * 将表单值写入部门实体。
     *
     * @param entity 部门实体
     * @param form 表单数据
     */
    private void applyForm(SysDept entity, DeptForm form) {
        entity.setParentId(form.getParentId() == null ? 0L : form.getParentId());
        entity.setDeptName(form.getDeptName());
        entity.setSort(form.getSort() == null ? 0 : form.getSort());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    /**
     * 根据父部门构建祖级路径。
     *
     * @param parentId 父部门 ID
     * @return 祖级路径
     */
    private String buildAncestorPath(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return ",";
        }
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null) {
            return ",";
        }
        String ancestorPath = parent.getAncestorPath();
        if (!StringUtils.hasText(ancestorPath)) {
            ancestorPath = ",";
        }
        if (!ancestorPath.endsWith(",")) {
            ancestorPath = ancestorPath + ",";
        }
        return ancestorPath + parent.getId() + ",";
    }

    /**
     * 断言指定部门下不存在子部门。
     *
     * @param id 部门 ID
     */
    private void assertNoChildDept(Long id) {
        Long childCount = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException(400, "请先删除子部门");
        }
    }

    /**
     * 断言指定部门下不存在未删除用户。
     *
     * @param id 部门 ID
     */
    private void assertNoActiveUser(Long id) {
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getDeptId, id)
            .eq(SysUser::getDeleted, 0));
        if (userCount != null && userCount > 0) {
            throw new BizException(400, "请先迁移或删除部门下的用户");
        }
    }

    /**
     * 将部门实体转换为树节点。
     *
     * @param dept 部门实体
     * @return 树节点
     */
    private DeptTreeVO toDeptTreeVO(SysDept dept) {
        DeptTreeVO vo = new DeptTreeVO();
        vo.setId(dept.getId());
        vo.setParentId(dept.getParentId());
        vo.setDeptName(dept.getDeptName());
        vo.setAncestorPath(dept.getAncestorPath());
        vo.setSort(dept.getSort());
        vo.setStatus(dept.getStatus());
        return vo;
    }

    /**
     * 将平铺部门列表组装为树形结构。
     *
     * @param flatList 平铺节点列表
     * @return 树形节点列表
     */
    private List<DeptTreeVO> buildTree(List<DeptTreeVO> flatList) {
        Map<Long, DeptTreeVO> nodeMap = new LinkedHashMap<>();
        for (DeptTreeVO node : flatList) {
            nodeMap.put(node.getId(), node);
        }
        List<DeptTreeVO> roots = new ArrayList<>();
        for (DeptTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0L) {
                roots.add(node);
                continue;
            }
            DeptTreeVO parent = nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.getChildren().add(node);
        }
        sortTree(roots);
        return roots;
    }

    /**
     * 递归按排序字段整理树节点顺序。
     *
     * @param nodes 树节点列表
     */
    private void sortTree(List<DeptTreeVO> nodes) {
        nodes.sort(Comparator.comparing(DeptTreeVO::getSort).thenComparing(DeptTreeVO::getId));
        for (DeptTreeVO node : nodes) {
            sortTree(node.getChildren());
        }
    }
}
