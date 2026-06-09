package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.config.ConfigForm;
import com.manzhushaka.system.dto.config.ConfigQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.config.ConfigVO;

public interface ConfigService {
    PageResult<ConfigVO> page(ConfigQuery query);

    ConfigVO getById(Long id);

    Long create(ConfigForm form);

    void update(Long id, ConfigForm form);

    void delete(Long id);
}
