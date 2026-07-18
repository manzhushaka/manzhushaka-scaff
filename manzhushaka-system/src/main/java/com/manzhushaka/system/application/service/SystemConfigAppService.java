package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.application.command.SaveConfigCommand;
import com.manzhushaka.system.application.query.ConfigQuery;
import com.manzhushaka.system.application.result.system.ConfigResult;

/**
 * 系统参数配置应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface SystemConfigAppService
{
    /**
     * 查询参数配置列表。
     *
     * @param query 查询条件
     * @return 参数配置列表
     */
    List<ConfigResult> listConfigs(ConfigQuery query);

    /**
     * 查询参数配置详情。
     *
     * @param configId 参数配置 ID
     * @return 参数配置详情
     */
    ConfigResult getConfig(Long configId);

    /**
     * 根据键名查询参数值。
     *
     * @param configKey 参数键名
     * @return 参数值
     */
    String getConfigValue(String configKey);

    /**
     * 新增参数配置。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int createConfig(SaveConfigCommand command, String operatorUsername);

    /**
     * 修改参数配置。
     *
     * @param command 保存命令
     * @param operatorUsername 操作人账号
     * @return 影响行数
     */
    int updateConfig(SaveConfigCommand command, String operatorUsername);

    /**
     * 删除参数配置。
     *
     * @param configIds 参数配置 ID 数组
     */
    void deleteConfigs(Long[] configIds);

    /** 刷新参数配置缓存。 */
    void refreshCache();
}
