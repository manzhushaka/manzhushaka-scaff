package com.manzhushaka.web.dto.system.role;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 角色列表请求
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class RoleListRequest extends DateRangeRequest
{
    private Integer pageNum;
    private Integer pageSize;
    private String roleName;
    private String roleKey;
    private String status;

    public Integer getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(Integer pageNum)
    {
        this.pageNum = pageNum;
    }

    public Integer getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(Integer pageSize)
    {
        this.pageSize = pageSize;
    }

    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    public String getRoleKey()
    {
        return roleKey;
    }

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
