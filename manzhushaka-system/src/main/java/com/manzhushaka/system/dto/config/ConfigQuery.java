package com.manzhushaka.system.dto.config;

import com.manzhushaka.system.dto.PageQuery;

/**
 * 承载 ConfigQuery 请求参数。
 */
public class ConfigQuery extends PageQuery {
    private String configName;
    private String configKey;
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
