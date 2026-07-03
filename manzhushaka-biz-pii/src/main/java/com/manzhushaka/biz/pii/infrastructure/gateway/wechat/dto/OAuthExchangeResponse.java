package com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto;

public class OAuthExchangeResponse {
    private String openid;
    private String unionid;
    private String accessToken;
    private int expiresIn;
    private String errCode;
    private String errMsg;

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public int getExpiresIn() { return expiresIn; }
    public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }
    public String getErrCode() { return errCode; }
    public void setErrCode(String errCode) { this.errCode = errCode; }
    public String getErrMsg() { return errMsg; }
    public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
}
