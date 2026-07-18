package com.manzhushaka.system.application.result.system;

import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 角色导出行。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record RoleExcelRow(
        @Excel(name = "角色序号", cellType = ColumnType.NUMERIC) Long roleId,
        @Excel(name = "角色名称") String roleName,
        @Excel(name = "角色权限") String roleKey,
        @Excel(name = "角色排序") Integer roleSort,
        @Excel(name = "数据范围",
                readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,"
                        + "4=本部门及以下数据权限,5=仅本人数据权限")
        String dataScope,
        @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用") String status)
{
}
