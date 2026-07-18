package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 登录日志结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record LoginLogResult(
        @Excel(name = "序号", cellType = ColumnType.NUMERIC) Long infoId,
        @Excel(name = "用户账号") String userName,
        @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败") String status,
        @Excel(name = "登录地址") String ipaddr,
        @Excel(name = "登录地点") String loginLocation,
        @Excel(name = "浏览器") String browser,
        @Excel(name = "操作系统") String os,
        @Excel(name = "提示消息") String msg,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date loginTime)
{
}
