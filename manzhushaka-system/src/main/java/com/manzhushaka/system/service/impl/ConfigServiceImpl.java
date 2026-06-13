package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysConfig;
import com.manzhushaka.db.system.mapper.SysConfigMapper;
import com.manzhushaka.system.dto.config.ConfigForm;
import com.manzhushaka.system.dto.config.ConfigQuery;
import com.manzhushaka.system.service.ConfigService;
import com.manzhushaka.system.service.support.SystemMappingSupport;
import com.manzhushaka.system.service.support.SystemPageSupport;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.config.ConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 实现 ConfigServiceImpl 业务服务。
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigMapper configMapper;

    /**
     * 创建 ConfigServiceImpl 实例。
     *
     * @param configMapper configMapper 参数
     */
    public ConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    /**
     * 分页查询列表。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @Override
    public PageResult<ConfigVO> page(ConfigQuery query) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
            .like(StringUtils.hasText(query.getConfigName()), SysConfig::getConfigName, query.getConfigName())
            .like(StringUtils.hasText(query.getConfigKey()), SysConfig::getConfigKey, query.getConfigKey())
            .eq(query.getStatus() != null, SysConfig::getStatus, query.getStatus())
            .orderByDesc(SysConfig::getId);
        Page<SysConfig> page = configMapper.selectPage(SystemPageSupport.buildPage(query), wrapper);
        return SystemMappingSupport.toPageResult(page, this::toConfigVO);
    }

    /**
     * 根据 ID 查询详情。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @Override
    public ConfigVO getById(Long id) {
        return toConfigVO(getConfigOrThrow(id));
    }

    /**
     * 创建数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @Override
    @Transactional
    public Long create(ConfigForm form) {
        SysConfig entity = new SysConfig();
        applyForm(entity, form);
        configMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 更新数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     */
    @Override
    @Transactional
    public void update(Long id, ConfigForm form) {
        SysConfig entity = getConfigOrThrow(id);
        applyForm(entity, form);
        configMapper.updateById(entity);
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (configMapper.deleteById(id) == 0) {
            throw new BizException(404, "参数配置不存在");
        }
    }

    /**
     * 返回 configOrThrow。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    private SysConfig getConfigOrThrow(Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BizException(404, "参数配置不存在");
        }
        return config;
    }

    /**
     * 更新 apply Form 数据。
     *
     * @param entity 实体对象
     * @param form 表单参数
     */
    private void applyForm(SysConfig entity, ConfigForm form) {
        entity.setConfigName(form.getConfigName());
        entity.setConfigKey(form.getConfigKey());
        entity.setConfigValue(form.getConfigValue());
        entity.setStatus(form.getStatus() == null ? 1 : form.getStatus());
    }

    /**
     * 构建 to Config VO 结果。
     *
     * @param config config 参数
     * @return 处理结果
     */
    private ConfigVO toConfigVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        vo.setId(config.getId());
        vo.setConfigName(config.getConfigName());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setStatus(config.getStatus());
        vo.setCreateTime(config.getCreateTime());
        return vo;
    }
}
