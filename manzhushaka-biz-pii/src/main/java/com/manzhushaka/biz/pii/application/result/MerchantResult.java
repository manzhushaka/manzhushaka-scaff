package com.manzhushaka.biz.pii.application.result;

import com.manzhushaka.biz.pii.domain.model.MerchantProfile;

import java.time.LocalDateTime;

public class MerchantResult {
    private Long id;
    private Long deptId;
    private Long parentDeptId;
    private String regionName;
    private String merchantName;
    private String umsMerchantId;
    private String umsTerminalId;
    private String invoiceMsgSrc;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MerchantResult from(MerchantProfile profile) {
        MerchantResult result = new MerchantResult();
        result.setId(profile.getId());
        result.setDeptId(profile.getDeptId());
        result.setMerchantName(profile.getMerchantName());
        result.setUmsMerchantId(profile.getUmsMerchantId());
        result.setUmsTerminalId(profile.getUmsTerminalId());
        result.setInvoiceMsgSrc(profile.getInvoiceMsgSrc());
        result.setStatus(profile.getStatus());
        result.setRemark(profile.getRemark());
        result.setCreateTime(profile.getCreateTime());
        result.setUpdateTime(profile.getUpdateTime());
        return result;
    }

    public MerchantResult withRegion(Long parentDeptId, String regionName) {
        this.parentDeptId = parentDeptId;
        this.regionName = regionName;
        return this;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getParentDeptId() { return parentDeptId; }
    public void setParentDeptId(Long parentDeptId) { this.parentDeptId = parentDeptId; }
    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getUmsMerchantId() { return umsMerchantId; }
    public void setUmsMerchantId(String umsMerchantId) { this.umsMerchantId = umsMerchantId; }
    public String getUmsTerminalId() { return umsTerminalId; }
    public void setUmsTerminalId(String umsTerminalId) { this.umsTerminalId = umsTerminalId; }
    public String getInvoiceMsgSrc() { return invoiceMsgSrc; }
    public void setInvoiceMsgSrc(String invoiceMsgSrc) { this.invoiceMsgSrc = invoiceMsgSrc; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
