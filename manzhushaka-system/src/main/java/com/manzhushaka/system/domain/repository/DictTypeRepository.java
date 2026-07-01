package com.manzhushaka.system.domain.repository;

import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictType;

/**
 * 字典类型仓储接口
 *
 * @author manzhushaka
 */
public interface DictTypeRepository
{
    /**
     * 根据条件分页查询字典类型
     */
    List<SysDictType> selectDictTypeList(SysDictType dictType);

    /**
     * 根据所有字典类型
     */
    List<SysDictType> selectDictTypeAll();

    /**
     * 根据字典类型ID查询信息
     */
    SysDictType selectDictTypeById(Long dictId);

    /**
     * 根据字典类型查询信息
     */
    SysDictType selectDictTypeByType(String dictType);

    /**
     * 通过字典ID删除字典信息
     */
    int deleteDictTypeById(Long dictId);

    /**
     * 批量删除字典类型信息
     */
    int deleteDictTypeByIds(Long[] dictIds);

    /**
     * 新增字典类型信息
     */
    int insertDictType(SysDictType dictType);

    /**
     * 修改字典类型信息
     */
    int updateDictType(SysDictType dictType);

    /**
     * 校验字典类型称是否唯一
     */
    SysDictType checkDictTypeUnique(String dictType);
}