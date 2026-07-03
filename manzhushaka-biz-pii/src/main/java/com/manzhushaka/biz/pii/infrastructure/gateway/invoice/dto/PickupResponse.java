package com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto;

public class PickupResponse {
    private String resultCode;
    private String resultMsg;
    private String pdf;
    private String pdfUrl;
    private String ofdUrl;
    private String xmlUrl;

    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
    public String getPdf() { return pdf; }
    public void setPdf(String pdf) { this.pdf = pdf; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getOfdUrl() { return ofdUrl; }
    public void setOfdUrl(String ofdUrl) { this.ofdUrl = ofdUrl; }
    public String getXmlUrl() { return xmlUrl; }
    public void setXmlUrl(String xmlUrl) { this.xmlUrl = xmlUrl; }
}
