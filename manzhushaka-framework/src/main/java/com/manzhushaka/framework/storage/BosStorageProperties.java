package com.manzhushaka.framework.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定义 BosStorageProperties。
 */
@Component
@ConfigurationProperties(prefix = "storage.bos")
public class BosStorageProperties {
    private String endpoint;
    private String bucket;
    private String accessKeyId;
    private String secretAccessKey;
    private String basePath = "import-export";
    private int downloadExpireSeconds = 900;

    /**
     * 返回 endpoint。
     *
     * @return 字段值
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * 设置 endpoint。
     *
     * @param endpoint endpoint 参数
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 返回 bucket。
     *
     * @return 字段值
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * 设置 bucket。
     *
     * @param bucket bucket 参数
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * 返回 accessKeyId。
     *
     * @return 字段值
     */
    public String getAccessKeyId() {
        return accessKeyId;
    }

    /**
     * 设置 accessKeyId。
     *
     * @param accessKeyId accessKeyId 标识
     */
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /**
     * 返回 secretAccessKey。
     *
     * @return 字段值
     */
    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    /**
     * 设置 secretAccessKey。
     *
     * @param secretAccessKey secretAccessKey 参数
     */
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    /**
     * 返回 basePath。
     *
     * @return 字段值
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * 设置 basePath。
     *
     * @param basePath basePath 参数
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    /**
     * 返回 downloadExpireSeconds。
     *
     * @return 字段值
     */
    public int getDownloadExpireSeconds() {
        return downloadExpireSeconds;
    }

    /**
     * 设置 downloadExpireSeconds。
     *
     * @param downloadExpireSeconds downloadExpireSeconds 参数
     */
    public void setDownloadExpireSeconds(int downloadExpireSeconds) {
        this.downloadExpireSeconds = downloadExpireSeconds;
    }
}
