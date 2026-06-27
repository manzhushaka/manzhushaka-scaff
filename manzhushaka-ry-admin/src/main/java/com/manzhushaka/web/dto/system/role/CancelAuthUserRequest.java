package com.manzhushaka.web.dto.system.role;

/**
 * 取消授权用户请求
 */
public class CancelAuthUserRequest
{
    private Long roleId;
    private Long userId;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
}