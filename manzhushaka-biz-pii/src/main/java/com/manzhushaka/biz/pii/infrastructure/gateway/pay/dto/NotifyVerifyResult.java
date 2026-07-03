package com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto;

import java.util.Map;

public class NotifyVerifyResult {
    private boolean valid;
    private NotifyPayload payload;
    private Map<String, String> params;
    private String errorMsg;

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public NotifyPayload getPayload() { return payload; }
    public void setPayload(NotifyPayload payload) { this.payload = payload; }
    public Map<String, String> getParams() { return params; }
    public void setParams(Map<String, String> params) { this.params = params; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
