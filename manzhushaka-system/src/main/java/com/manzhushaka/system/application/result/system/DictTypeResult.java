package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 字典类型查询结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record DictTypeResult(
        @Excel(name = "字典主键", cellType = ColumnType.NUMERIC) Long dictId,
        @Excel(name = "字典名称") String dictName,
        @Excel(name = "字典类型") String dictType,
        @Excel(name = "状态", readConverterExp = "0=正常,1=停用") String status,
        String createBy, Date createTime, String updateBy, Date updateTime, String remark)
{
}
