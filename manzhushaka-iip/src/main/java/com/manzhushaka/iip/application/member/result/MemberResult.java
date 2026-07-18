package com.manzhushaka.iip.application.member.result;

import java.util.Date;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 小程序用户结果（后台列表/详情/导出）。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MemberResult(
        @Excel(name = "用户ID", cellType = ColumnType.NUMERIC) Long memberId,
        @Excel(name = "昵称") String nickname,
        @Excel(name = "手机号") String phone,
        @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知") String gender,
        @Excel(name = "状态", readConverterExp = "0=正常,1=停用") String status,
        @Excel(name = "最近登录时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss") Date lastLoginTime,
        @Excel(name = "最近登录IP") String lastLoginIp,
        @Excel(name = "注册时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss") Date createTime,
        String remark)
{
    @Override
    public String toString()
    {
        return "MemberResult[memberId=" + memberId + ", nickname=" + nickname + ", status=" + status + "]";
    }
}
