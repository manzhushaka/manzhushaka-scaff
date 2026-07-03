package com.manzhushaka.biz.pii.infrastructure.gateway.wechat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.wechat.dto.OAuthExchangeResponse;
import com.manzhushaka.common.exception.ServiceException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "REAL")
public class UmsWechatOAuthGateway implements WechatOAuthGateway {

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

    @Override
    public OAuthExchangeResponse exchangeCodeForOpenId(OAuthExchangeRequest request) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(buildAccessTokenUrl(request));
            return client.execute(get, response -> {
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (response.getCode() != 200) {
                    throw new ServiceException("微信 OAuth HTTP " + response.getCode(), 10204);
                }
                return parseResponse(body);
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("微信 OAuth 调用异常: " + e.getMessage(), 10204).setDetailMessage(e.toString());
        }
    }

    String buildAccessTokenUrl(OAuthExchangeRequest request) {
        return ACCESS_TOKEN_URL
                + "?appid=" + encode(request.getAppId())
                + "&secret=" + encode(request.getAppSecret())
                + "&code=" + encode(request.getCode())
                + "&grant_type=authorization_code";
    }

    OAuthExchangeResponse parseResponse(String body) {
        JSONObject json = JSON.parseObject(body);
        OAuthExchangeResponse response = new OAuthExchangeResponse();
        response.setOpenid(json.getString("openid"));
        response.setUnionid(json.getString("unionid"));
        response.setAccessToken(json.getString("access_token"));
        response.setExpiresIn(json.getIntValue("expires_in"));
        if (json.containsKey("errcode")) {
            response.setErrCode(json.getString("errcode"));
            response.setErrMsg(json.getString("errmsg"));
        }
        return response;
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
