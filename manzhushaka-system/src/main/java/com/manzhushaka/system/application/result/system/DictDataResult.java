package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 字典数据查询结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record DictDataResult(
        @Excel(name = "字典编码", cellType = ColumnType.NUMERIC) Long dictCode,
        @Excel(name = "字典排序", cellType = ColumnType.NUMERIC) Long dictSort,
        @Excel(name = "字典标签") String dictLabel,
        @Excel(name = "字典键值") String dictValue,
        @Excel(name = "字典类型") String dictType,
        String cssClass, String listClass,
        @Excel(name = "是否默认", readConverterExp = "Y=是,N=否") String isDefault,
        @Excel(name = "状态", readConverterExp = "0=正常,1=停用") String status,
        String createBy, Date createTime, String updateBy, Date updateTime, String remark)
{
    public boolean getDefault()
    {
        return "Y".equals(isDefault);
    }
}
