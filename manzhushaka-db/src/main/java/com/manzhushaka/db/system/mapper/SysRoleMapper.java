package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提供 SysRoleMapper 持久化访问能力。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
