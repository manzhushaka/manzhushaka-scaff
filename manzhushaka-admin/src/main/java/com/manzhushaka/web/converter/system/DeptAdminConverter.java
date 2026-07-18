package com.manzhushaka.web.converter.system;

import com.manzhushaka.system.application.command.SaveDeptCommand;
import com.manzhushaka.system.application.query.DeptQuery;
import com.manzhushaka.web.dto.system.DeptSaveRequest;
import com.manzhushaka.web.dto.system.SysDeptTreeRequest;

/**
 * 部门管理转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class DeptAdminConverter
{
    private DeptAdminConverter()
    {
    }

    public static DeptQuery toQuery(SysDeptTreeRequest request)
    {
        if (request == null)
        {
            return new DeptQuery(null, null, null, null, null);
        }
        return new DeptQuery(request.getDeptName(), request.getStatus(), request.getDeptType(),
                request.getRegionCode(), request.getRegionLevel());
    }

    public static SaveDeptCommand toCommand(DeptSaveRequest request)
    {
        return new SaveDeptCommand(request.getDeptId(), request.getParentId(), request.getDeptName(),
                request.getOrderNum(), request.getLeader(), request.getPhone(), request.getEmail(),
                request.getStatus(), request.getDeptType(), request.getRegionCode(),
                request.getRegionLevel());
    }
}
