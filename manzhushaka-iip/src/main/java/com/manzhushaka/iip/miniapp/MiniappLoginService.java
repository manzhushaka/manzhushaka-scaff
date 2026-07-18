package com.manzhushaka.iip.miniapp;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.http.HttpUtils;

/**
 * 小程序平台登录适配 服务层（wechat/alipay/unionpay + 开发模拟降级）
 * 
 * 平台密钥从配置 iip.miniapp.{platform}.appid/secret 读取，默认空。
 * 任一平台 appid/secret 未配置时，该平台登录走开发模拟 mock 模式（仅限开发调试，生产必须配置真实密钥）。
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class MiniappLoginService
{
    /** 平台：微信 */
    public static final String PLATFORM_WECHAT = "wechat";

    /** 平台：支付宝 */
    public static final String PLATFORM_ALIPAY = "alipay";

    /** 平台：云闪付 */
    public static final String PLATFORM_UNIONPAY = "unionpay";

    /** 平台白名单 */
    private static final Set<String> SUPPORTED_PLATFORMS = Set.of(PLATFORM_WECHAT, PLATFORM_ALIPAY, PLATFORM_UNIONPAY);

    /** 微信 jscode2session 接口地址 */
    private static final String WECHAT_JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Value("${iip.miniapp.wechat.appid:}")
    private String wechatAppid;

    @Value("${iip.miniapp.wechat.secret:}")
    private String wechatSecret;

    @Value("${iip.miniapp.alipay.appid:}")
    private String alipayAppid;

    @Value("${iip.miniapp.alipay.secret:}")
    private String alipaySecret;

    @Value("${iip.miniapp.unionpay.appid:}")
    private String unionpayAppid;

    @Value("${iip.miniapp.unionpay.secret:}")
    private String unionpaySecret;

    /**
     * 按平台用登录code换取平台会话（openid 等）
     * 
     * @param platform 平台（wechat/alipay/unionpay，大小写不敏感）
     * @param code 小程序登录code
     * @return 平台会话（含规范化后的平台标识与openid）
     * @throws ServiceException 平台不在白名单、code为空或平台方调用失败时抛出
     */
    public MiniappSession login(String platform, String code)
    {
        if (StringUtils.isBlank(code))
        {
            throw new ServiceException("登录code不能为空");
        }
        String normalized = StringUtils.isBlank(platform) ? "" : platform.trim().toLowerCase();
        if (!SUPPORTED_PLATFORMS.contains(normalized))
        {
            throw new ServiceException("不支持的平台：" + platform);
        }
        switch (normalized)
        {
            case PLATFORM_WECHAT:
                return isConfigured(wechatAppid, wechatSecret) ? doWechatLogin(normalized, code) : mockLogin(normalized, code);
            case PLATFORM_ALIPAY:
                return isConfigured(alipayAppid, alipaySecret) ? doAlipayLogin(normalized, code) : mockLogin(normalized, code);
            case PLATFORM_UNIONPAY:
                return isConfigured(unionpayAppid, unionpaySecret) ? doUnionpayLogin(normalized, code) : mockLogin(normalized, code);
            default:
                throw new ServiceException("不支持的平台：" + platform);
        }
    }

    /**
     * 微信真实登录：调用 jscode2session 用 code 换 openid/session_key
     * 
     * @param platform 规范化后的平台标识
     * @param code 小程序登录code
     * @return 平台会话
     * @throws ServiceException 网络失败、响应解析失败或微信返回错误码时抛出
     */
    private MiniappSession doWechatLogin(String platform, String code)
    {
        String param = "appid=" + wechatAppid + "&secret=" + wechatSecret + "&js_code=" + code
                + "&grant_type=authorization_code";
        String response = HttpUtils.sendGet(WECHAT_JSCODE2SESSION_URL, param);
        if (StringUtils.isBlank(response))
        {
            throw new ServiceException("微信登录服务暂不可用，请稍后重试");
        }
        JSONObject json;
        try
        {
            json = JSON.parseObject(response);
        }
        catch (Exception e)
        {
            throw new ServiceException("微信登录响应解析失败");
        }
        Integer errcode = json.getInteger("errcode");
        if (errcode != null && errcode != 0)
        {
            throw new ServiceException("微信登录失败：" + json.getString("errmsg"));
        }
        String openid = json.getString("openid");
        if (StringUtils.isBlank(openid))
        {
            throw new ServiceException("微信登录失败：未获取到openid");
        }
        return new MiniappSession(platform, openid, json.getString("session_key"), json.getString("unionid"));
    }

    /**
     * 支付宝真实登录（结构预留）
     * 
     * 生产环境需接入支付宝官方 SDK（alipay-sdk-java），用 auth_code 调用 alipay.system.oauth.token
     * 换取 user_id，并对返回内容做验签；当前工程未集成官方 SDK，配置密钥后调用将提示暂未开放。
     * 
     * @param platform 规范化后的平台标识
     * @param code 小程序登录code
     * @return 平台会话
     */
    private MiniappSession doAlipayLogin(String platform, String code)
    {
        throw new ServiceException("支付宝登录接入中，暂未开放");
    }

    /**
     * 云闪付真实登录（结构预留）
     * 
     * 生产环境需接入云闪付（银联）官方 SDK 完成 code 换取用户标识与验签；
     * 当前工程未集成官方 SDK，配置密钥后调用将提示暂未开放。
     * 
     * @param platform 规范化后的平台标识
     * @param code 小程序登录code
     * @return 平台会话
     */
    private MiniappSession doUnionpayLogin(String platform, String code)
    {
        throw new ServiceException("云闪付登录接入中，暂未开放");
    }

    /**
     * 开发模拟登录：以 mock_{platform}_{code} 作为 openid
     * 
     * 仅限开发调试使用（平台 appid/secret 未配置时触发）；生产环境必须配置真实平台密钥，禁止依赖 mock。
     * 
     * @param platform 规范化后的平台标识
     * @param code 小程序登录code
     * @return 模拟平台会话
     */
    private MiniappSession mockLogin(String platform, String code)
    {
        return new MiniappSession(platform, "mock_" + platform + "_" + code, null, null);
    }

    /**
     * 判断平台密钥是否已配置
     * 
     * @param appid 平台appid
     * @param secret 平台secret
     * @return true 表示均已配置
     */
    private boolean isConfigured(String appid, String secret)
    {
        return StringUtils.isNotBlank(appid) && StringUtils.isNotBlank(secret);
    }

    /**
     * 小程序平台会话
     * 
     * @param platform 规范化后的平台标识（wechat/alipay/unionpay）
     * @param openid 平台用户标识
     * @param sessionKey 平台会话密钥（mock 模式为null）
     * @param unionid 平台联合标识（可能为null）
     */
    public record MiniappSession(String platform, String openid, String sessionKey, String unionid)
    {
    }
}
