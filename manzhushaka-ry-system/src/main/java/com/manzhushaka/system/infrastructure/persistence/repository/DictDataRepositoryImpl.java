package com.manzhushaka.system.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.manzhushaka.system.domain.repository.DictDataRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictData;
import com.manzhushaka.system.infrastructure.persistence.mapper.SysDictDataMapper;

/**
 * 字典数据仓储实现
 *
 * @author manzhushaka
 */
@Repository
public class DictDataRepositoryImpl implements DictDataRepository
{
    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Override
    public List<SysDictData> selectDictDataList(SysDictData dictData)
    {
        return dictDataMapper.selectDictDataList(dictData);
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType)
    {
        return dictDataMapper.selectDictDataByType(dictType);
    }

    @Override
    public String selectDictLabel(String dictType, String dictValue)
    {
        return dictDataMapper.selectDictLabel(dictType, dictValue);
    }

    @Override
    public SysDictData selectDictDataById(Long dictCode)
    {
        return dictDataMapper.selectDictDataById(dictCode);
    }

    @Override
    public int countDictDataByType(String dictType)
    {
        return dictDataMapper.countDictDataByType(dictType);
    }

    @Override
    public int deleteDictDataById(Long dictCode)
    {
        return dictDataMapper.deleteDictDataById(dictCode);
    }

    @Override
    public int deleteDictDataByIds(Long[] dictCodes)
    {
        return dictDataMapper.deleteDictDataByIds(dictCodes);
    }

    @Override
    public int insertDictData(SysDictData dictData)
    {
        return dictDataMapper.insertDictData(dictData);
    }

    @Override
    public int updateDictData(SysDictData dictData)
    {
        return dictDataMapper.updateDictData(dictData);
    }

    @Override
    public int updateDictDataType(String oldDictType, String newDictType)
    {
        return dictDataMapper.updateDictDataType(oldDictType, newDictType);
    }
}