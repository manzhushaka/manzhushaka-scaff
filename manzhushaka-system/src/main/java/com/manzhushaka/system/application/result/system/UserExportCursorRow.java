package com.manzhushaka.system.application.result.system;

import java.util.Date;

/**
 * 带稳定游标的用户导出行。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public class UserExportCursorRow extends UserExcelRow
{
    private Date createTime;

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    @Override
    public String toString()
    {
        return "UserExportCursorRow[userId=" + getUserId() + ", userName=" + getUserName()
                + ", createTime=" + createTime + "]";
    }
}
