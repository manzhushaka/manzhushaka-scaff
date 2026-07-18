package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 参数配置结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record ConfigResult(
        @Excel(name = "参数主键", cellType = ColumnType.NUMERIC) Long configId,
        @Excel(name = "参数名称") String configName,
        @Excel(name = "参数键名") String configKey,
        @Excel(name = "参数键值") String configValue,
        @Excel(name = "系统内置", readConverterExp = "Y=是,N=否") String configType,
        String createBy, Date createTime, String updateBy, Date updateTime, String remark)
{
    @Override
    public String toString()
    {
        return "ConfigResult[configId=" + configId + ", configName=" + configName
                + ", configKey=" + configKey + ", configType=" + configType + "]";
    }
}
