package com.manzhushaka.web.dto.system.user;

import com.manzhushaka.web.dto.common.DateRangeRequest;

/**
 * 用户列表请求
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class UserListRequest extends DateRangeRequest
{
    private Integer pageNum;
    private Integer pageSize;
    private String userName;
    private String phonenumber;
    private String status;
    private Long deptId;

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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

}
