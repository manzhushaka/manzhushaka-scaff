package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 提供 SysMenuMapper 持久化访问能力。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * 查询 select Menus By User Id 结果。
     *
     * @param userId 用户 ID
     * @return 查询结果
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
