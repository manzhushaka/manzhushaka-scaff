package com.manzhushaka.biz.pii.infrastructure.gateway.wechat;

import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeResponse;

public interface WechatOAuthGateway {
    OAuthExchangeResponse exchangeCodeForOpenId(OAuthExchangeRequest request);
}
