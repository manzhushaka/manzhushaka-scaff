package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 操作日志结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record OperLogResult(
        @Excel(name = "操作序号", cellType = ColumnType.NUMERIC) Long operId,
        @Excel(name = "操作模块") String title,
        @Excel(name = "业务类型",
                readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,"
                        + "6=导入,7=强退,8=生成代码,9=清空数据")
        Integer businessType,
        @Excel(name = "请求方法") String method,
        @Excel(name = "请求方式") String requestMethod,
        @Excel(name = "操作类别", readConverterExp = "0=其它,1=后台用户,2=手机端用户")
        Integer operatorType,
        @Excel(name = "操作人员") String operName,
        @Excel(name = "部门名称") String deptName,
        @Excel(name = "请求地址") String operUrl,
        @Excel(name = "操作地址") String operIp,
        @Excel(name = "操作地点") String operLocation,
        @Excel(name = "请求参数") String operParam,
        @Excel(name = "返回参数") String jsonResult,
        @Excel(name = "状态", readConverterExp = "0=正常,1=异常") Integer status,
        @Excel(name = "错误消息") String errorMsg,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date operTime,
        @Excel(name = "消耗时间", suffix = "毫秒") Long costTime)
{
    @Override
    public String toString()
    {
        return "OperLogResult[operId=" + operId + ", title=" + title + ", status=" + status + "]";
    }
}
