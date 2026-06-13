package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.dict.DictItemForm;
import com.manzhushaka.system.dto.dict.DictTypeForm;
import com.manzhushaka.system.dto.dict.DictTypeQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.dict.DictItemVO;
import com.manzhushaka.system.vo.dict.DictTypeVO;

import java.util.List;

/**
 * 定义 DictService 服务能力。
 */
public interface DictService {
    /**
     * 查询 page Types 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<DictTypeVO> pageTypes(DictTypeQuery query);

    /**
     * 返回 typeById。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    DictTypeVO getTypeById(Long id);

    /**
     * 查询 list Items By Type Id 结果。
     *
     * @param dictTypeId dictTypeId 标识
     * @return 查询结果
     */
    List<DictItemVO> listItemsByTypeId(Long dictTypeId);

    /**
     * 查询 list Items By Type Code 结果。
     *
     * @param dictCode dictCode 参数
     * @return 查询结果
     */
    List<DictItemVO> listItemsByTypeCode(String dictCode);

    /**
     * 执行 type Options 逻辑。
     *
     * @return 处理结果
     */
    List<LabelValueOption> typeOptions();

    /**
     * 创建 create Type 数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long createType(DictTypeForm form);

    /**
     * 更新 update Type 数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void updateType(Long id, DictTypeForm form);

    /**
     * 清理 delete Type 数据。
     *
     * @param id 主键 ID
     */
    void deleteType(Long id);

    /**
     * 创建 create Item 数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    Long createItem(DictItemForm form);

    /**
     * 更新 update Item 数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    void updateItem(Long id, DictItemForm form);

    /**
     * 清理 delete Item 数据。
     *
     * @param id 主键 ID
     */
    void deleteItem(Long id);
}
