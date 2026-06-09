package com.manzhushaka.system.dto.config;

import jakarta.validation.constraints.NotBlank;

public class ConfigForm {
    @NotBlank(message = "参数名称不能为空")
    private String configName;
    @NotBlank(message = "参数键不能为空")
    private String configKey;
    private String configValue;
    private Integer status;

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
