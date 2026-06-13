package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.role.RoleForm;
import com.manzhushaka.system.dto.role.RoleQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.role.RoleVO;

import java.util.List;

/**
 * 定义 RoleService 服务能力。
 */
public interface RoleService {
    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<RoleVO> page(RoleQuery query);

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
    RoleVO getById(Long id);

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long create(RoleForm form);

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void update(Long id, RoleForm form);

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    void delete(Long id);
}
