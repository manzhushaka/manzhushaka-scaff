package com.manzhushaka.db.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manzhushaka.db.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 提供 SysUserMapper 持久化访问能力。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 查询结果
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 查询用户角色编码。
     *
     * @param userId 用户 ID
     * @return 查询结果
     */
    List<String> selectRoleCodes(@Param("userId") Long userId);

    /**
     * 查询用户权限编码。
     *
     * @param userId 用户 ID
     * @return 查询结果
     */
    List<String> selectPermCodes(@Param("userId") Long userId);

    /**
     * 查询用户数据权限范围。
     *
     * @param userId 用户 ID
     * @return 查询结果
     */
    List<String> selectDataScopes(@Param("userId") Long userId);
}
