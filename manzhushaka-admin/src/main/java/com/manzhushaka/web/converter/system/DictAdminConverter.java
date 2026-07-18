package com.manzhushaka.web.converter.system;

import com.manzhushaka.system.application.command.SaveDictDataCommand;
import com.manzhushaka.system.application.command.SaveDictTypeCommand;
import com.manzhushaka.system.application.query.DictDataQuery;
import com.manzhushaka.system.application.query.DictTypeQuery;
import com.manzhushaka.web.dto.system.DictDataRequest;
import com.manzhushaka.web.dto.system.DictTypeRequest;

/**
 * 字典管理转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class DictAdminConverter
{
    private DictAdminConverter()
    {
    }

    public static DictTypeQuery toQuery(DictTypeRequest request)
    {
        return new DictTypeQuery(request.getDictName(), request.getDictType(), request.getStatus(),
                request.getBeginTime(), request.getEndTime());
    }

    public static SaveDictTypeCommand toCommand(DictTypeRequest request)
    {
        return new SaveDictTypeCommand(request.getDictId(), request.getDictName(), request.getDictType(),
                request.getStatus(), request.getRemark());
    }

    public static DictDataQuery toQuery(DictDataRequest request)
    {
        return new DictDataQuery(request.getDictLabel(), request.getDictType(), request.getStatus());
    }

    public static SaveDictDataCommand toCommand(DictDataRequest request)
    {
        return new SaveDictDataCommand(request.getDictCode(), request.getDictSort(), request.getDictLabel(),
                request.getDictValue(), request.getDictType(), request.getCssClass(), request.getListClass(),
                request.getIsDefault(), request.getStatus(), request.getRemark());
    }
}
