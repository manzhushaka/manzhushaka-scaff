package com.manzhushaka.system.application.service.impl;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.application.command.SaveDictDataCommand;
import com.manzhushaka.system.application.command.SaveDictTypeCommand;
import com.manzhushaka.system.application.query.DictDataQuery;
import com.manzhushaka.system.application.query.DictTypeQuery;
import com.manzhushaka.system.application.result.system.DictDataResult;
import com.manzhushaka.system.application.result.system.DictTypeResult;
import com.manzhushaka.system.application.service.SystemDictAppService;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictData;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictType;
import com.manzhushaka.system.service.ISysDictDataService;
import com.manzhushaka.system.service.ISysDictTypeService;

/**
 * 系统字典应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class SystemDictAppServiceImpl implements SystemDictAppService
{
    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private ISysDictDataService dictDataService;

    @Override
    public List<DictTypeResult> listDictTypes(DictTypeQuery query)
    {
        return dictTypeService.selectDictTypeList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public DictTypeResult getDictType(Long dictId)
    {
        return toResult(dictTypeService.selectDictTypeById(dictId));
    }

    @Override
    public List<DictTypeResult> listAllDictTypes()
    {
        return dictTypeService.selectDictTypeAll().stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional
    public int createDictType(SaveDictTypeCommand command, String operatorUsername)
    {
        SysDictType dict = toEntity(command);
        if (!dictTypeService.checkDictTypeUnique(dict))
        {
            throw new ServiceException("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dict.setCreateBy(operatorUsername);
        return dictTypeService.insertDictType(dict);
    }

    @Override
    @Transactional
    public int updateDictType(SaveDictTypeCommand command, String operatorUsername)
    {
        SysDictType dict = toEntity(command);
        if (!dictTypeService.checkDictTypeUnique(dict))
        {
            throw new ServiceException("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dict.setUpdateBy(operatorUsername);
        return dictTypeService.updateDictType(dict);
    }

    @Override
    public void deleteDictTypes(Long[] dictIds)
    {
        dictTypeService.deleteDictTypeByIds(dictIds);
    }

    @Override
    public void refreshCache()
    {
        dictTypeService.resetDictCache();
    }

    @Override
    public List<DictDataResult> listDictData(DictDataQuery query)
    {
        return dictDataService.selectDictDataList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public List<DictDataResult> listDictDataByType(String dictType)
    {
        List<SysDictData> data = dictTypeService.selectDictDataByType(dictType);
        return data == null ? Collections.emptyList() : data.stream().map(this::toResult).toList();
    }

    @Override
    public DictDataResult getDictData(Long dictCode)
    {
        return toResult(dictDataService.selectDictDataById(dictCode));
    }

    @Override
    public int createDictData(SaveDictDataCommand command, String operatorUsername)
    {
        SysDictData dict = toEntity(command);
        dict.setCreateBy(operatorUsername);
        return dictDataService.insertDictData(dict);
    }

    @Override
    public int updateDictData(SaveDictDataCommand command, String operatorUsername)
    {
        SysDictData dict = toEntity(command);
        dict.setUpdateBy(operatorUsername);
        return dictDataService.updateDictData(dict);
    }

    @Override
    public void deleteDictData(Long[] dictCodes)
    {
        dictDataService.deleteDictDataByIds(dictCodes);
    }

    private SysDictType toEntity(DictTypeQuery query)
    {
        SysDictType dict = new SysDictType();
        if (query != null)
        {
            dict.setDictName(query.dictName());
            dict.setDictType(query.dictType());
            dict.setStatus(query.status());
            if (query.beginTime() != null)
            {
                dict.getParams().put("beginTime", query.beginTime());
            }
            if (query.endTime() != null)
            {
                dict.getParams().put("endTime", query.endTime());
            }
        }
        return dict;
    }

    private SysDictType toEntity(SaveDictTypeCommand command)
    {
        SysDictType dict = new SysDictType();
        dict.setDictId(command.dictId());
        dict.setDictName(command.dictName());
        dict.setDictType(command.dictType());
        dict.setStatus(command.status());
        dict.setRemark(command.remark());
        return dict;
    }

    private SysDictData toEntity(DictDataQuery query)
    {
        SysDictData dict = new SysDictData();
        if (query != null)
        {
            dict.setDictLabel(query.dictLabel());
            dict.setDictType(query.dictType());
            dict.setStatus(query.status());
        }
        return dict;
    }

    private SysDictData toEntity(SaveDictDataCommand command)
    {
        SysDictData dict = new SysDictData();
        dict.setDictCode(command.dictCode());
        dict.setDictSort(command.dictSort());
        dict.setDictLabel(command.dictLabel());
        dict.setDictValue(command.dictValue());
        dict.setDictType(command.dictType());
        dict.setCssClass(command.cssClass());
        dict.setListClass(command.listClass());
        dict.setIsDefault(command.isDefault());
        dict.setStatus(command.status());
        dict.setRemark(command.remark());
        return dict;
    }

    private DictTypeResult toResult(SysDictType dict)
    {
        if (dict == null)
        {
            return null;
        }
        return new DictTypeResult(dict.getDictId(), dict.getDictName(), dict.getDictType(),
                dict.getStatus(), dict.getCreateBy(), dict.getCreateTime(), dict.getUpdateBy(),
                dict.getUpdateTime(), dict.getRemark());
    }

    private DictDataResult toResult(SysDictData dict)
    {
        if (dict == null)
        {
            return null;
        }
        return new DictDataResult(dict.getDictCode(), dict.getDictSort(), dict.getDictLabel(),
                dict.getDictValue(), dict.getDictType(), dict.getCssClass(), dict.getListClass(),
                dict.getIsDefault(), dict.getStatus(), dict.getCreateBy(), dict.getCreateTime(),
                dict.getUpdateBy(), dict.getUpdateTime(), dict.getRemark());
    }
}
