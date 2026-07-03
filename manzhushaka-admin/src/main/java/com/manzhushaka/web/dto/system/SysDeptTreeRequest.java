package com.manzhushaka.web.dto.system;

import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;

public class SysDeptTreeRequest {
    private String deptName;
    private String status;
    private String deptType;
    private String regionCode;
    private Integer regionLevel;

    public SysDept toDept() {
        SysDept dept = new SysDept();
        dept.setDeptName(deptName);
        dept.setStatus(status);
        dept.setDeptType(deptType);
        dept.setRegionCode(regionCode);
        dept.setRegionLevel(regionLevel);
        return dept;
    }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDeptType() { return deptType; }
    public void setDeptType(String deptType) { this.deptType = deptType; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public Integer getRegionLevel() { return regionLevel; }
    public void setRegionLevel(Integer regionLevel) { this.regionLevel = regionLevel; }
}
