package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.dept.DeptForm;
import com.manzhushaka.system.dto.dept.DeptQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.dept.DeptTreeVO;

import java.util.List;

public interface DeptService {
    List<DeptTreeVO> tree(DeptQuery query);

    List<LabelValueOption> options();

    DeptTreeVO getById(Long id);

    Long create(DeptForm form);

    void update(Long id, DeptForm form);

    void delete(Long id);
}
