package com.manzhushaka.biz.pii.infrastructure.gateway.wechat;

import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockWechatOAuthGateway implements WechatOAuthGateway {

    @Override
    public OAuthExchangeResponse exchangeCodeForOpenId(OAuthExchangeRequest request) {
        OAuthExchangeResponse response = new OAuthExchangeResponse();
        response.setOpenid("MOCK_OPENID_" + request.getCode());
        response.setAccessToken("MOCK_AT");
        response.setExpiresIn(7200);
        return response;
    }
}
