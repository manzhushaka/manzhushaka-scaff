package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.application.command.SaveDictDataCommand;
import com.manzhushaka.system.application.command.SaveDictTypeCommand;
import com.manzhushaka.system.application.query.DictDataQuery;
import com.manzhushaka.system.application.query.DictTypeQuery;
import com.manzhushaka.system.application.result.system.DictDataResult;
import com.manzhushaka.system.application.result.system.DictTypeResult;

/**
 * 系统字典应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface SystemDictAppService
{
    /**
     * 查询字典类型。
     *
     * @param query 查询条件
     * @return 字典类型列表
     */
    List<DictTypeResult> listDictTypes(DictTypeQuery query);

    /**
     * 查询字典类型详情。
     *
     * @param dictId 字典类型 ID
     * @return 字典类型详情
     */
    DictTypeResult getDictType(Long dictId);

    /**
     * 查询全部字典类型。
     *
     * @return 字典类型列表
     */
    List<DictTypeResult> listAllDictTypes();

    /**
     * 新增字典类型。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createDictType(SaveDictTypeCommand command, String operatorUsername);

    /**
     * 修改字典类型。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateDictType(SaveDictTypeCommand command, String operatorUsername);

    /**
     * 删除字典类型。
     *
     * @param dictIds 字典类型 ID 数组
     */
    void deleteDictTypes(Long[] dictIds);

    /** 刷新字典缓存。 */
    void refreshCache();

    /**
     * 查询字典数据。
     *
     * @param query 查询条件
     * @return 字典数据列表
     */
    List<DictDataResult> listDictData(DictDataQuery query);

    /**
     * 按类型查询字典数据。
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<DictDataResult> listDictDataByType(String dictType);

    /**
     * 查询字典数据详情。
     *
     * @param dictCode 字典数据 ID
     * @return 字典数据详情
     */
    DictDataResult getDictData(Long dictCode);

    /**
     * 新增字典数据。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createDictData(SaveDictDataCommand command, String operatorUsername);

    /**
     * 修改字典数据。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateDictData(SaveDictDataCommand command, String operatorUsername);

    /**
     * 删除字典数据。
     *
     * @param dictCodes 字典数据 ID 数组
     */
    void deleteDictData(Long[] dictCodes);
}
