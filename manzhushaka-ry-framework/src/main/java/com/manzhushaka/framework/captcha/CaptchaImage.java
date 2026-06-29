package com.manzhushaka.framework.captcha;

/**
 * 图片验证码生成结果。
 *
 * @param code 验证码文本
 * @param imageBase64 图片 Base64
 * @author manzhushaka
 * @date 2026-06-29
 */
public record CaptchaImage(String code, String imageBase64)
{
}
