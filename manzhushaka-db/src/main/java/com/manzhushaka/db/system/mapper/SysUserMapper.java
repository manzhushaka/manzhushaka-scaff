package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    SysUser selectByUsername(@Param("username") String username);

    List<String> selectRoleCodes(@Param("userId") Long userId);

    List<String> selectPermCodes(@Param("userId") Long userId);

    List<String> selectDataScopes(@Param("userId") Long userId);
}
