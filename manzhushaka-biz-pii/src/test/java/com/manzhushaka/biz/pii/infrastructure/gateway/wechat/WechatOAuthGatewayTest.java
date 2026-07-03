package com.manzhushaka.biz.pii.infrastructure.gateway.wechat;

import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WechatOAuthGatewayTest {

    @Test
    void mockGatewayShouldReturnOpenIdFromCode() {
        MockWechatOAuthGateway gateway = new MockWechatOAuthGateway();

        OAuthExchangeResponse response = gateway.exchangeCodeForOpenId(request("CODE001"));

        assertThat(response.getOpenid()).isEqualTo("MOCK_OPENID_CODE001");
        assertThat(response.getAccessToken()).isEqualTo("MOCK_AT");
        assertThat(response.getExpiresIn()).isEqualTo(7200);
    }

    @Test
    void umsGatewayShouldBuildOAuthAccessTokenUrl() {
        UmsWechatOAuthGateway gateway = new UmsWechatOAuthGateway();

        String url = gateway.buildAccessTokenUrl(request("C O/D+E"));

        assertThat(url).startsWith("https://api.weixin.qq.com/sns/oauth2/access_token?");
        assertThat(url).contains("appid=APP");
        assertThat(url).contains("secret=SECRET");
        assertThat(url).contains("code=C+O%2FD%2BE");
        assertThat(url).contains("grant_type=authorization_code");
    }

    @Test
    void umsGatewayShouldParseErrorJson() {
        UmsWechatOAuthGateway gateway = new UmsWechatOAuthGateway();

        OAuthExchangeResponse response = gateway.parseResponse("{\"errcode\":40029,\"errmsg\":\"invalid code\"}");

        assertThat(response.getErrCode()).isEqualTo("40029");
        assertThat(response.getErrMsg()).isEqualTo("invalid code");
        assertThat(response.getOpenid()).isNull();
    }

    private OAuthExchangeRequest request(String code) {
        OAuthExchangeRequest request = new OAuthExchangeRequest();
        request.setAppId("APP");
        request.setAppSecret("SECRET");
        request.setCode(code);
        return request;
    }
}
