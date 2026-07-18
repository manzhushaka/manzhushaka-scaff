package com.manzhushaka.web.dto.system.role;

/**
 * 角色用户列表查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class RoleUserListRequest
{
    private Long roleId;
    private String userName;
    private String phonenumber;

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }
}
