package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysDictItem;
import com.manzhushaka.db.system.entity.SysDictType;
import com.manzhushaka.db.system.mapper.SysDictItemMapper;
import com.manzhushaka.db.system.mapper.SysDictTypeMapper;
import com.manzhushaka.system.dto.dict.DictItemForm;
import com.manzhushaka.system.dto.dict.DictTypeForm;
import com.manzhushaka.system.dto.dict.DictTypeQuery;
import com.manzhushaka.system.service.DictService;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.dict.DictItemVO;
import com.manzhushaka.system.vo.dict.DictTypeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 实现 DictServiceImpl 业务服务。
 */
@Service
public class DictServiceImpl implements DictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;

    /**
     * 创建 DictServiceImpl 实例。
     *
     * @param dictTypeMapper dictTypeMapper 参数
     * @param dictItemMapper dictItemMapper 参数
     */
    public DictServiceImpl(SysDictTypeMapper dictTypeMapper, SysDictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    /**
     * 查询 page Types 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @Override
    public PageResult<DictTypeVO> pageTypes(DictTypeQuery query) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
            .like(StringUtils.hasText(query.getDictName()), SysDictType::getDictName, query.getDictName())
            .like(StringUtils.hasText(query.getDictCode()), SysDictType::getDictCode, query.getDictCode())
            .eq(query.getStatus() != null, SysDictType::getStatus, query.getStatus())
            .orderByDesc(SysDictType::getId);
        Page<SysDictType> page = dictTypeMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, type -> {
            DictTypeVO vo = toDictTypeVO(type);
            vo.setItems(listItemsByTypeId(type.getId()));
            return vo;
        });
    }

    /**
     * 返回 typeById。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @Override
    public DictTypeVO getTypeById(Long id) {
        SysDictType type = getTypeOrThrow(id);
        DictTypeVO vo = toDictTypeVO(type);
        vo.setItems(listItemsByTypeId(id));
        return vo;
    }

    /**
     * 查询 list Items By Type Id 结果。
     *
     * @param dictTypeId dictTypeId 标识
     * @return 查询结果
     */
    @Override
    public List<DictItemVO> listItemsByTypeId(Long dictTypeId) {
        List<SysDictItem> items = dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
            .eq(SysDictItem::getDictTypeId, dictTypeId)
            .orderByAsc(SysDictItem::getSort, SysDictItem::getId));
        return items.stream().map(this::toDictItemVO).toList();
    }

    /**
     * 查询 list Items By Type Code 结果。
     *
     * @param dictCode dictCode 参数
     * @return 查询结果
     */
    @Override
    public List<DictItemVO> listItemsByTypeCode(String dictCode) {
        SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
            .eq(SysDictType::getDictCode, dictCode)
            .last("limit 1"));
        if (type == null) {
            return List.of();
        }
        return listItemsByTypeId(type.getId());
    }

    /**
     * 执行 type Options 逻辑。
     *
     * @return 处理结果
     */
    @Override
    public List<LabelValueOption> typeOptions() {
        List<SysDictType> types = dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
            .eq(SysDictType::getStatus, 1)
            .orderByAsc(SysDictType::getId));
        return types.stream().map(type -> new LabelValueOption(type.getDictName(), String.valueOf(type.getId()))).toList();
    }

    /**
     * 创建 create Type 数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @Override
    @Transactional
    public Long createType(DictTypeForm form) {
        SysDictType entity = new SysDictType();
        entity.setDictName(form.getDictName());
        entity.setDictCode(form.getDictCode());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        dictTypeMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 更新 update Type 数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    @Override
    @Transactional
    public void updateType(Long id, DictTypeForm form) {
        SysDictType entity = getTypeOrThrow(id);
        entity.setDictName(form.getDictName());
        entity.setDictCode(form.getDictCode());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        dictTypeMapper.updateById(entity);
    }

    /**
     * 清理 delete Type 数据。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional
    public void deleteType(Long id) {
        if (dictTypeMapper.deleteById(id) == 0) {
            throw new BizException(404, "字典类型不存在");
        }
        dictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictTypeId, id));
    }

    /**
     * 创建 create Item 数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @Override
    @Transactional
    public Long createItem(DictItemForm form) {
        getTypeOrThrow(form.getDictTypeId());
        SysDictItem entity = new SysDictItem();
        applyItemForm(entity, form);
        dictItemMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 更新 update Item 数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    @Override
    @Transactional
    public void updateItem(Long id, DictItemForm form) {
        getTypeOrThrow(form.getDictTypeId());
        SysDictItem entity = getItemOrThrow(id);
        applyItemForm(entity, form);
        dictItemMapper.updateById(entity);
    }

    /**
     * 清理 delete Item 数据。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (dictItemMapper.deleteById(id) == 0) {
            throw new BizException(404, "字典项不存在");
        }
    }

    /**
     * 更新 apply Item Form 数据。
     *
     * @param entity 实体对象
     * @param form 表单参数
     */
    private void applyItemForm(SysDictItem entity, DictItemForm form) {
        entity.setDictTypeId(form.getDictTypeId());
        entity.setItemLabel(form.getItemLabel());
        entity.setItemValue(form.getItemValue());
        entity.setSort(form.getSort() == null ? 0 : form.getSort());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    /**
     * 返回 typeOrThrow。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    private SysDictType getTypeOrThrow(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new BizException(404, "字典类型不存在");
        }
        return type;
    }

    /**
     * 返回 itemOrThrow。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    private SysDictItem getItemOrThrow(Long id) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw new BizException(404, "字典项不存在");
        }
        return item;
    }

    /**
     * 构建 to Dict Type VO 结果。
     *
     * @param type type 参数
     * @return 处理结果
     */
    private DictTypeVO toDictTypeVO(SysDictType type) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(type.getId());
        vo.setDictName(type.getDictName());
        vo.setDictCode(type.getDictCode());
        vo.setStatus(type.getStatus());
        vo.setCreateTime(type.getCreateTime());
        return vo;
    }

    /**
     * 构建 to Dict Item VO 结果。
     *
     * @param item item 参数
     * @return 处理结果
     */
    private DictItemVO toDictItemVO(SysDictItem item) {
        DictItemVO vo = new DictItemVO();
        vo.setId(item.getId());
        vo.setDictTypeId(item.getDictTypeId());
        vo.setItemLabel(item.getItemLabel());
        vo.setItemValue(item.getItemValue());
        vo.setSort(item.getSort());
        vo.setStatus(item.getStatus());
        return vo;
    }
}
