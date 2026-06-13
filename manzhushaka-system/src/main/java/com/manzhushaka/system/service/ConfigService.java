package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.config.ConfigForm;
import com.manzhushaka.system.dto.config.ConfigQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.config.ConfigVO;

/**
 * 定义 ConfigService 服务能力。
 */
public interface ConfigService {
    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<ConfigVO> page(ConfigQuery query);

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    ConfigVO getById(Long id);

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long create(ConfigForm form);

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void update(Long id, ConfigForm form);

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    void delete(Long id);
}
