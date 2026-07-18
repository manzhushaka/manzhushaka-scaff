package com.manzhushaka.web.dto.system;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 部门保存请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class DeptSaveRequest
{
    private Long deptId;
    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过30个字符")
    private String deptName;

    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    private String leader;

    @Size(max = 11, message = "联系电话长度不能超过11个字符")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    private String status;
    private String deptType;
    private String regionCode;
    private Integer regionLevel;

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getLeader()
    {
        return leader;
    }

    public void setLeader(String leader)
    {
        this.leader = leader;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDeptType()
    {
        return deptType;
    }

    public void setDeptType(String deptType)
    {
        this.deptType = deptType;
    }

    public String getRegionCode()
    {
        return regionCode;
    }

    public void setRegionCode(String regionCode)
    {
        this.regionCode = regionCode;
    }

    public Integer getRegionLevel()
    {
        return regionLevel;
    }

    public void setRegionLevel(Integer regionLevel)
    {
        this.regionLevel = regionLevel;
    }

    @Override
    public String toString()
    {
        return "DeptSaveRequest{deptId=" + deptId + ", deptName='" + deptName + "'}";
    }
}
