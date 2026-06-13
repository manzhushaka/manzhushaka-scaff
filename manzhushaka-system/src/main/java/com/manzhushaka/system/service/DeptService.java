package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.dept.DeptForm;
import com.manzhushaka.system.dto.dept.DeptQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.dept.DeptTreeVO;

import java.util.List;

/**
 * 定义 DeptService 服务能力。
 */
public interface DeptService {
    /**
     * 查询树形数据。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    List<DeptTreeVO> tree(DeptQuery query);

    /**
     * 查询下拉选项。
     *
     * @return 查询结果
     */
    List<LabelValueOption> options();

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    DeptTreeVO getById(Long id);

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long create(DeptForm form);

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void update(Long id, DeptForm form);

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    void delete(Long id);
}
