package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.user.UserForm;
import com.manzhushaka.system.dto.user.UserQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.user.UserVO;

/**
 * 定义 UserService 服务能力。
 */
public interface UserService {
    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<UserVO> page(UserQuery query);

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    UserVO getById(Long id);

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long create(UserForm form);

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void update(Long id, UserForm form);

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    void delete(Long id);
}
