package com.manzhushaka.web.dto.pii;

public class BiDeptAggregateRequest extends BiDashboardRequest {
    private String level;
    private Long parentDeptId;

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Long getParentDeptId() { return parentDeptId; }
    public void setParentDeptId(Long parentDeptId) { this.parentDeptId = parentDeptId; }
}
