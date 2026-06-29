package com.manzhushaka.web.controller.common;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.utils.uuid.IdUtils;
import com.manzhushaka.framework.captcha.CaptchaImage;
import com.manzhushaka.framework.captcha.CaptchaService;
import com.manzhushaka.system.service.ISysConfigService;

/**
 * 验证码操作处理
 * 
 * @author manzhushaka
 */
@RestController
public class CaptchaController
{
    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysConfigService configService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成验证码
     *
     * @return 验证码信息
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode()
    {
        AjaxResult ajax = AjaxResult.success();
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled)
        {
            return ajax;
        }

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        CaptchaImage captchaImage = captchaService.createCharCaptcha();
        redisCache.setCacheObject(verifyKey, captchaImage.code(), Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);

        ajax.put("uuid", uuid);
        ajax.put("img", captchaImage.imageBase64());
        return ajax;
    }
}
