package com.manzhushaka.system.vo.config;

/**
 * 承载 PlatformConfigVO 响应数据。
 */
public class PlatformConfigVO {
    private String platformName;
    private String platformSubtitle;
    private String logoUrl;

    /**
     * 返回 platformName。
     *
     * @return 字段值
     */
    public String getPlatformName() {
        return platformName;
    }

    /**
     * 设置 platformName。
     *
     * @param platformName platformName 参数
     */
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    /**
     * 返回 platformSubtitle。
     *
     * @return 字段值
     */
    public String getPlatformSubtitle() {
        return platformSubtitle;
    }

    /**
     * 设置 platformSubtitle。
     *
     * @param platformSubtitle platformSubtitle 参数
     */
    public void setPlatformSubtitle(String platformSubtitle) {
        this.platformSubtitle = platformSubtitle;
    }

    /**
     * 返回 logoUrl。
     *
     * @return 字段值
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * 设置 logoUrl。
     *
     * @param logoUrl logoUrl 参数
     */
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
