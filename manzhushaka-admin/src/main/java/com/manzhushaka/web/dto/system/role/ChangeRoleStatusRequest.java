package com.manzhushaka.web.dto.system.role;

/**
 * 修改角色状态请求
 */
public class ChangeRoleStatusRequest
{
    private Long roleId;
    private String status;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
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