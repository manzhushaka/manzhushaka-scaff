package com.manzhushaka.system.domain.repository;

import java.util.List;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictData;

/**
 * 字典数据仓储接口
 *
 * @author manzhushaka
 */
public interface DictDataRepository
{
    /**
     * 根据条件分页查询字典数据
     */
    List<SysDictData> selectDictDataList(SysDictData dictData);

    /**
     * 根据字典类型查询字典数据
     */
    List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 根据字典类型和字典键值查询字典数据信息
     */
    String selectDictLabel(String dictType, String dictValue);

    /**
     * 根据字典数据ID查询信息
     */
    SysDictData selectDictDataById(Long dictCode);

    /**
     * 查询字典数据
     */
    int countDictDataByType(String dictType);

    /**
     * 通过字典ID删除字典数据信息
     */
    int deleteDictDataById(Long dictCode);

    /**
     * 批量删除字典数据信息
     */
    int deleteDictDataByIds(Long[] dictCodes);

    /**
     * 新增字典数据信息
     */
    int insertDictData(SysDictData dictData);

    /**
     * 修改字典数据信息
     */
    int updateDictData(SysDictData dictData);

    /**
     * 同步修改字典类型
     */
    int updateDictDataType(String oldDictType, String newDictType);
}