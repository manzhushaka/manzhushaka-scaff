package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.role.RoleForm;
import com.manzhushaka.system.dto.role.RoleQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.role.RoleVO;

import java.util.List;

public interface RoleService {
    PageResult<RoleVO> page(RoleQuery query);

    List<LabelValueOption> options();

    RoleVO getById(Long id);

    Long create(RoleForm form);

    void update(Long id, RoleForm form);

    void delete(Long id);
}
