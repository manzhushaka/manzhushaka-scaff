package com.manzhushaka.web.vo.system.user;

import java.io.Serializable;

/**
 * 当前登录用户视图对象。
 *
 * 仅承载前端展示所需字段，认证密码、角色和权限等内部信息通过独立字段返回。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class AuthUserProfileVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;

    /** 部门 ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 登录账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 用户头像 */
    private String avatar;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    @Override
    public String toString()
    {
        return "AuthUserProfileVO{" +
                "userId=" + userId +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", userName='" + userName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
