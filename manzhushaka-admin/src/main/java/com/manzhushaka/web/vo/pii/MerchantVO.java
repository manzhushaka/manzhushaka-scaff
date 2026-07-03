package com.manzhushaka.web.vo.pii;

import com.manzhushaka.biz.pii.application.result.MerchantResult;

import java.time.LocalDateTime;

public class MerchantVO {
    private Long id;
    private Long deptId;
    private String merchantName;
    private String umsMerchantId;
    private String umsTerminalId;
    private String invoiceMsgSrc;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static MerchantVO from(MerchantResult result) {
        MerchantVO vo = new MerchantVO();
        vo.setId(result.getId());
        vo.setDeptId(result.getDeptId());
        vo.setMerchantName(result.getMerchantName());
        vo.setUmsMerchantId(result.getUmsMerchantId());
        vo.setUmsTerminalId(result.getUmsTerminalId());
        vo.setInvoiceMsgSrc(result.getInvoiceMsgSrc());
        vo.setStatus(result.getStatus());
        vo.setRemark(result.getRemark());
        vo.setCreateTime(result.getCreateTime());
        vo.setUpdateTime(result.getUpdateTime());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
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
