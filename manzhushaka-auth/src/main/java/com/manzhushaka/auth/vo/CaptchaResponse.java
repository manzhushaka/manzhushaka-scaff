package com.manzhushaka.auth.vo;

/**
 * 承载 CaptchaResponse 响应数据。
 */
public class CaptchaResponse {
    private String key;
    private String imageBase64;

    /**
     * 返回 key。
     *
     * @return 字段值
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置 key。
     *
     * @param key 键名
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 返回 imageBase64。
     *
     * @return 字段值
     */
    public String getImageBase64() {
        return imageBase64;
    }

    /**
     * 设置 imageBase64。
     *
     * @param imageBase64 imageBase64 参数
     */
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
