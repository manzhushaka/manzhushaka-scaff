package com.manzhushaka.system.vo.config;

import java.time.LocalDateTime;

/**
 * 承载 ConfigVO 响应数据。
 */
public class ConfigVO {
    private Long id;
    private String configName;
    private String configKey;
    private String configValue;
    private Integer status;
    private LocalDateTime createTime;

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 返回 configName。
     *
     * @return 字段值
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * 设置 configName。
     *
     * @param configName configName 参数
     */
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    /**
     * 返回 configKey。
     *
     * @return 字段值
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * 设置 configKey。
     *
     * @param configKey configKey 参数
     */
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    /**
     * 返回 configValue。
     *
     * @return 字段值
     */
    public String getConfigValue() {
        return configValue;
    }

    /**
     * 设置 configValue。
     *
     * @param configValue configValue 参数
     */
    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    /**
     * 返回 status。
     *
     * @return 字段值
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status status 参数
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
