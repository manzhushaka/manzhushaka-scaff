package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysOpLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提供 SysOpLogMapper 持久化访问能力。
 */
@Mapper
public interface SysOpLogMapper extends BaseMapper<SysOpLog> {
}
