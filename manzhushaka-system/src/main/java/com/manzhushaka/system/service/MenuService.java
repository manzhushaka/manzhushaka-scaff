package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.menu.MenuForm;
import com.manzhushaka.system.dto.menu.MenuQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.MenuTreeNode;
import com.manzhushaka.system.vo.menu.MenuRouteVO;
import com.manzhushaka.system.vo.menu.MenuVO;

import java.util.List;

/**
 * 定义 MenuService 服务能力。
 */
public interface MenuService {
    /**
     * 查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    List<MenuVO> list(MenuQuery query);

    /**
     * 查询树形数据。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    List<MenuTreeNode> tree(MenuQuery query);

    /**
     * 查询 routes By User Id 结果。
     *
     * @param userId 用户 ID
     * @return 查询结果
     */
    List<MenuRouteVO> routesByUserId(Long userId);

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
    MenuVO getById(Long id);

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long create(MenuForm form);

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void update(Long id, MenuForm form);

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    void delete(Long id);
}
