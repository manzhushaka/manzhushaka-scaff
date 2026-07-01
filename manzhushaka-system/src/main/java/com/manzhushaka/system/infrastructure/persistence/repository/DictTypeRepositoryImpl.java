package com.manzhushaka.system.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.manzhushaka.system.domain.repository.DictTypeRepository;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictType;
import com.manzhushaka.system.mapper.SysDictTypeMapper;

/**
 * 字典类型仓储实现
 *
 * @author manzhushaka
 */
@Repository
public class DictTypeRepositoryImpl implements DictTypeRepository
{
    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Override
    public List<SysDictType> selectDictTypeList(SysDictType dictType)
    {
        return dictTypeMapper.selectDictTypeList(dictType);
    }

    @Override
    public List<SysDictType> selectDictTypeAll()
    {
        return dictTypeMapper.selectDictTypeAll();
    }

    @Override
    public SysDictType selectDictTypeById(Long dictId)
    {
        return dictTypeMapper.selectDictTypeById(dictId);
    }

    @Override
    public SysDictType selectDictTypeByType(String dictType)
    {
        return dictTypeMapper.selectDictTypeByType(dictType);
    }

    @Override
    public int deleteDictTypeById(Long dictId)
    {
        return dictTypeMapper.deleteDictTypeById(dictId);
    }

    @Override
    public int deleteDictTypeByIds(Long[] dictIds)
    {
        return dictTypeMapper.deleteDictTypeByIds(dictIds);
    }

    @Override
    public int insertDictType(SysDictType dictType)
    {
        return dictTypeMapper.insertDictType(dictType);
    }

    @Override
    public int updateDictType(SysDictType dictType)
    {
        return dictTypeMapper.updateDictType(dictType);
    }

    @Override
    public SysDictType checkDictTypeUnique(String dictType)
    {
        return dictTypeMapper.checkDictTypeUnique(dictType);
    }
}
