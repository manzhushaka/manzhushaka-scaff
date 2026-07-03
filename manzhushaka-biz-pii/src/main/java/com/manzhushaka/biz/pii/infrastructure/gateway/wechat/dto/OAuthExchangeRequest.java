package com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto;

public class OAuthExchangeRequest {
    private String appId;
    private String appSecret;
    private String code;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
