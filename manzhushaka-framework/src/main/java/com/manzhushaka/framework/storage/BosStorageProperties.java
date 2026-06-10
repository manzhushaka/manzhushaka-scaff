package com.manzhushaka.framework.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage.bos")
public class BosStorageProperties {
    private String endpoint;
    private String bucket;
    private String accessKeyId;
    private String secretAccessKey;
    private String basePath = "import-export";
    private int downloadExpireSeconds = 900;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public int getDownloadExpireSeconds() {
        return downloadExpireSeconds;
    }

    public void setDownloadExpireSeconds(int downloadExpireSeconds) {
        this.downloadExpireSeconds = downloadExpireSeconds;
    }
}
