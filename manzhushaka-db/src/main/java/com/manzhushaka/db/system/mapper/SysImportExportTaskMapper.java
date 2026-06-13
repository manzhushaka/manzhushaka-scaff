package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提供 SysImportExportTaskMapper 持久化访问能力。
 */
@Mapper
public interface SysImportExportTaskMapper extends BaseMapper<SysImportExportTask> {
}
