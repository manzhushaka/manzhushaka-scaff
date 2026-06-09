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

@Service
public class DictServiceImpl implements DictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;

    public DictServiceImpl(SysDictTypeMapper dictTypeMapper, SysDictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

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

    @Override
    public DictTypeVO getTypeById(Long id) {
        SysDictType type = getTypeOrThrow(id);
        DictTypeVO vo = toDictTypeVO(type);
        vo.setItems(listItemsByTypeId(id));
        return vo;
    }

    @Override
    public List<DictItemVO> listItemsByTypeId(Long dictTypeId) {
        List<SysDictItem> items = dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
            .eq(SysDictItem::getDictTypeId, dictTypeId)
            .orderByAsc(SysDictItem::getSort, SysDictItem::getId));
        return items.stream().map(this::toDictItemVO).toList();
    }

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

    @Override
    public List<LabelValueOption> typeOptions() {
        List<SysDictType> types = dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
            .eq(SysDictType::getStatus, 1)
            .orderByAsc(SysDictType::getId));
        return types.stream().map(type -> new LabelValueOption(type.getDictName(), String.valueOf(type.getId()))).toList();
    }

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

    @Override
    @Transactional
    public void updateType(Long id, DictTypeForm form) {
        SysDictType entity = getTypeOrThrow(id);
        entity.setDictName(form.getDictName());
        entity.setDictCode(form.getDictCode());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        dictTypeMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void deleteType(Long id) {
        dictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictTypeId, id));
        if (dictTypeMapper.deleteById(id) == 0) {
            throw new BizException(404, "字典类型不存在");
        }
    }

    @Override
    @Transactional
    public Long createItem(DictItemForm form) {
        getTypeOrThrow(form.getDictTypeId());
        SysDictItem entity = new SysDictItem();
        applyItemForm(entity, form);
        dictItemMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void updateItem(Long id, DictItemForm form) {
        getTypeOrThrow(form.getDictTypeId());
        SysDictItem entity = getItemOrThrow(id);
        applyItemForm(entity, form);
        dictItemMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (dictItemMapper.deleteById(id) == 0) {
            throw new BizException(404, "字典项不存在");
        }
    }

    private void applyItemForm(SysDictItem entity, DictItemForm form) {
        entity.setDictTypeId(form.getDictTypeId());
        entity.setItemLabel(form.getItemLabel());
        entity.setItemValue(form.getItemValue());
        entity.setSort(form.getSort() == null ? 0 : form.getSort());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    private SysDictType getTypeOrThrow(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new BizException(404, "字典类型不存在");
        }
        return type;
    }

    private SysDictItem getItemOrThrow(Long id) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw new BizException(404, "字典项不存在");
        }
        return item;
    }

    private DictTypeVO toDictTypeVO(SysDictType type) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(type.getId());
        vo.setDictName(type.getDictName());
        vo.setDictCode(type.getDictCode());
        vo.setStatus(type.getStatus());
        vo.setCreateTime(type.getCreateTime());
        return vo;
    }

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
