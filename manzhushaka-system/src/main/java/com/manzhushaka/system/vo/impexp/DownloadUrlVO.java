package com.manzhushaka.system.vo.impexp;

/**
 * 承载 DownloadUrlVO 响应数据。
 */
public class DownloadUrlVO {
    private final String url;

    /**
     * 创建 DownloadUrlVO 实例。
     *
     * @param url url 参数
     */
    public DownloadUrlVO(String url) {
        this.url = url;
    }

    /**
     * 返回 url。
     *
     * @return 字段值
     */
    public String getUrl() {
        return url;
    }
}
