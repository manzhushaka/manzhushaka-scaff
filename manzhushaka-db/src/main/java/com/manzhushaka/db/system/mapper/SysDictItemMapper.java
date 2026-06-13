package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提供 SysDictItemMapper 持久化访问能力。
 */
@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {
}
