package com.manzhushaka.web.dto.system;

/**
 * 部门树查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class SysDeptTreeRequest
{
    private String deptName;
    private String status;
    private String deptType;
    private String regionCode;
    private Integer regionLevel;

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
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
        return "SysDeptTreeRequest{deptName='" + deptName + "', status='" + status + "'}";
    }
}
