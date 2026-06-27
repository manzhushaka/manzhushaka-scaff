package com.manzhushaka.web.vo.system.user;

import java.util.Date;
import java.util.List;
import com.manzhushaka.system.domain.SysPost;
import com.manzhushaka.common.core.domain.entity.SysRole;

/**
 * 用户详情视图对象
 */
public class UserDetailVO
{
    private Long userId;
    private Long deptId;
    private String userName;
    private String nickName;
    private String email;
    private String phonenumber;
    private String sex;
    private String avatar;
    private String status;
    private String deptName;
    private List<SysRole> roles;
    private List<Long> roleIds;
    private List<Long> postIds;
    private List<SysRole> allRoles;
    private List<Object> allPosts;
    private Date createTime;

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

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public List<SysRole> getRoles()
    {
        return roles;
    }

    public void setRoles(List<SysRole> roles)
    {
        this.roles = roles;
    }

    public List<Long> getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds)
    {
        this.roleIds = roleIds;
    }

    public List<Long> getPostIds()
    {
        return postIds;
    }

    public void setPostIds(List<Long> postIds)
    {
        this.postIds = postIds;
    }

    public List<SysRole> getAllRoles()
    {
        return allRoles;
    }

    public void setAllRoles(List<SysRole> allRoles)
    {
        this.allRoles = allRoles;
    }

    public List<Object> getAllPosts()
    {
        return allPosts;
    }

    public void setAllPosts(List<Object> allPosts)
    {
        this.allPosts = allPosts;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}