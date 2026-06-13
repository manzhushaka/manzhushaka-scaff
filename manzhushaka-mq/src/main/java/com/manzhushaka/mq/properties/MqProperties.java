package com.manzhushaka.mq.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定义 MqProperties。
 */
@ConfigurationProperties(prefix = "manzhushaka.mq")
public class MqProperties {
    private String group = "manzhushaka-group";
    private String consumer = "oplog-consumer";
    private int maxRetry = 3;
    private int processingTimeoutSeconds = 300;

    /**
     * 返回 group。
     *
     * @return 字段值
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置 group。
     *
     * @param group group 参数
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 返回 consumer。
     *
     * @return 字段值
     */
    public String getConsumer() {
        return consumer;
    }

    /**
     * 设置 consumer。
     *
     * @param consumer consumer 参数
     */
    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    /**
     * 返回 maxRetry。
     *
     * @return 字段值
     */
    public int getMaxRetry() {
        return maxRetry;
    }

    /**
     * 设置 maxRetry。
     *
     * @param maxRetry maxRetry 参数
     */
    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    /**
     * 返回 processingTimeoutSeconds。
     *
     * @return 字段值
     */
    public int getProcessingTimeoutSeconds() {
        return processingTimeoutSeconds;
    }

    /**
     * 设置 processingTimeoutSeconds。
     *
     * @param processingTimeoutSeconds processingTimeoutSeconds 参数
     */
    public void setProcessingTimeoutSeconds(int processingTimeoutSeconds) {
        this.processingTimeoutSeconds = processingTimeoutSeconds;
    }
}
