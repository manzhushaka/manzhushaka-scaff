package com.manzhushaka.web.dto.system.user;

/**
 * 修改当前用户密码请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class UpdateOwnPasswordRequest
{
    private String oldPassword;
    private String newPassword;

    public String getOldPassword()
    {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword)
    {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }

    @Override
    public String toString()
    {
        return "UpdateOwnPasswordRequest{}";
    }
}
