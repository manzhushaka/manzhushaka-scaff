package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysDept;
import com.manzhushaka.db.system.mapper.SysDeptMapper;
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

@Service
public class DeptServiceImpl implements DeptService {

    private final SysDeptMapper deptMapper;

    public DeptServiceImpl(SysDeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    @Override
    public List<DeptTreeVO> tree(DeptQuery query) {
        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
            .like(StringUtils.hasText(query.getDeptName()), SysDept::getDeptName, query.getDeptName())
            .eq(query.getStatus() != null, SysDept::getStatus, query.getStatus())
            .orderByAsc(SysDept::getSort, SysDept::getId));
        return buildTree(depts.stream().map(this::toDeptTreeVO).toList());
    }

    @Override
    public List<LabelValueOption> options() {
        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getStatus, 1)
            .orderByAsc(SysDept::getSort, SysDept::getId));
        return depts.stream().map(dept -> new LabelValueOption(dept.getDeptName(), String.valueOf(dept.getId()))).toList();
    }

    @Override
    public DeptTreeVO getById(Long id) {
        return toDeptTreeVO(getDeptOrThrow(id));
    }

    @Override
    @Transactional
    public Long create(DeptForm form) {
        SysDept entity = new SysDept();
        applyForm(entity, form);
        entity.setAncestorPath(buildAncestorPath(form.getParentId()));
        deptMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void update(Long id, DeptForm form) {
        SysDept entity = getDeptOrThrow(id);
        applyForm(entity, form);
        entity.setAncestorPath(buildAncestorPath(form.getParentId()));
        deptMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (deptMapper.deleteById(id) == 0) {
            throw new BizException(404, "部门不存在");
        }
    }

    private SysDept getDeptOrThrow(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BizException(404, "部门不存在");
        }
        return dept;
    }

    private void applyForm(SysDept entity, DeptForm form) {
        entity.setParentId(form.getParentId() == null ? 0L : form.getParentId());
        entity.setDeptName(form.getDeptName());
        entity.setSort(form.getSort() == null ? 0 : form.getSort());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

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

    private void sortTree(List<DeptTreeVO> nodes) {
        nodes.sort(Comparator.comparing(DeptTreeVO::getSort).thenComparing(DeptTreeVO::getId));
        for (DeptTreeVO node : nodes) {
            sortTree(node.getChildren());
        }
    }
}
