package com.manzhushaka.web.vo.pii;

import com.manzhushaka.biz.pii.application.result.QrcodeResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QrcodeVO {
    private Long id;
    private Long merchantId;
    private String qrcodeCode;
    private String qrcodeUrl;
    private String qrcodeImageUrl;
    private String name;
    private Integer status;
    private LocalDateTime expireTime;
    private String remark;
    private LocalDateTime createTime;
    private List<QrcodeTaxItemVO> taxItems = new ArrayList<>();

    public static QrcodeVO from(QrcodeResult result) {
        QrcodeVO vo = new QrcodeVO();
        vo.setId(result.getId());
        vo.setMerchantId(result.getMerchantId());
        vo.setQrcodeCode(result.getQrcodeCode());
        vo.setQrcodeUrl(result.getQrcodeUrl());
        vo.setQrcodeImageUrl(result.getQrcodeImageUrl());
        vo.setName(result.getName());
        vo.setStatus(result.getStatus());
        vo.setExpireTime(result.getExpireTime());
        vo.setRemark(result.getRemark());
        vo.setCreateTime(result.getCreateTime());
        vo.setTaxItems(result.getTaxItems().stream().map(QrcodeTaxItemVO::from).collect(Collectors.toList()));
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getQrcodeCode() { return qrcodeCode; }
    public void setQrcodeCode(String qrcodeCode) { this.qrcodeCode = qrcodeCode; }
    public String getQrcodeUrl() { return qrcodeUrl; }
    public void setQrcodeUrl(String qrcodeUrl) { this.qrcodeUrl = qrcodeUrl; }
    public String getQrcodeImageUrl() { return qrcodeImageUrl; }
    public void setQrcodeImageUrl(String qrcodeImageUrl) { this.qrcodeImageUrl = qrcodeImageUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public List<QrcodeTaxItemVO> getTaxItems() { return taxItems; }
    public void setTaxItems(List<QrcodeTaxItemVO> taxItems) { this.taxItems = taxItems; }
}
