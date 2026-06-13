package com.manzhushaka.system.dto.config;

import jakarta.validation.constraints.NotBlank;

/**
 * 承载 ConfigForm 请求参数。
 */
public class ConfigForm {
    @NotBlank(message = "参数名称不能为空")
    private String configName;
    @NotBlank(message = "参数键不能为空")
    private String configKey;
    private String configValue;
    private Integer status;

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
}
