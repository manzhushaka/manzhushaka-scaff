package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提供 SysJobLogMapper 持久化访问能力。
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {
}
