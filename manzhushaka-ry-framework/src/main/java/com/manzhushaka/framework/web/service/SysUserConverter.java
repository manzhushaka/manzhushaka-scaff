package com.manzhushaka.framework.web.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;

/**
 * 用户对象类型转换器
 * <p>
 * 用于在 common 版本 SysUser 和 system 版本 SysUser 之间进行转换。
 * 两个版本的类字段完全一致，仅包名不同：
 * <ul>
 *   <li>common: com.manzhushaka.common.core.domain.entity.SysUser</li>
 *   <li>system: com.manzhushaka.system.infrastructure.persistence.entity.SysUser</li>
 * </ul>
 *
 * @author manzhushaka
 */
public class SysUserConverter
{

    /**
     * 将 system 版本 SysUser 转换为 common 版本 SysUser。
     * <p>
     * 注意：关联对象 SysRole、SysDept 也会一并转换，以保证类型安全。
     *
     * @param source system 版本 SysUser
     * @return common 版本 SysUser，或 null（如果 source 为 null）
     */
    public static com.manzhushaka.common.core.domain.entity.SysUser toCommon(
            com.manzhushaka.system.infrastructure.persistence.entity.SysUser source)
    {
        if (source == null)
        {
            return null;
        }
        com.manzhushaka.common.core.domain.entity.SysUser target =
                new com.manzhushaka.common.core.domain.entity.SysUser();
        // 复制基本字段（同名同类型字段自动复制）
        BeanUtils.copyProperties(source, target);
        // 手动复制 SysDept（泛型擦除后类型不同）
        if (source.getDept() != null)
        {
            com.manzhushaka.common.core.domain.entity.SysDept dept =
                    new com.manzhushaka.common.core.domain.entity.SysDept();
            BeanUtils.copyProperties(source.getDept(), dept);
            target.setDept(dept);
        }
        // 手动复制 SysRole 列表
        if (source.getRoles() != null)
        {
            List<com.manzhushaka.common.core.domain.entity.SysRole> roleList = new ArrayList<>();
            for (com.manzhushaka.system.infrastructure.persistence.entity.SysRole role : source.getRoles())
            {
                com.manzhushaka.common.core.domain.entity.SysRole targetRole =
                        new com.manzhushaka.common.core.domain.entity.SysRole();
                BeanUtils.copyProperties(role, targetRole);
                roleList.add(targetRole);
            }
            target.setRoles(roleList);
        }
        return target;
    }

    /**
     * 将 common 版本 SysUser 转换为 system 版本 SysUser
     *
     * @param source common 版本 SysUser
     * @return system 版本 SysUser，或 null（如果 source 为 null）
     */
    public static com.manzhushaka.system.infrastructure.persistence.entity.SysUser toSystem(
            com.manzhushaka.common.core.domain.entity.SysUser source)
    {
        if (source == null)
        {
            return null;
        }
        com.manzhushaka.system.infrastructure.persistence.entity.SysUser target =
                new com.manzhushaka.system.infrastructure.persistence.entity.SysUser();
        BeanUtils.copyProperties(source, target);
        // 手动复制 SysDept
        if (source.getDept() != null)
        {
            com.manzhushaka.system.infrastructure.persistence.entity.SysDept dept =
                    new com.manzhushaka.system.infrastructure.persistence.entity.SysDept();
            BeanUtils.copyProperties(source.getDept(), dept);
            target.setDept(dept);
        }
        // 手动复制 SysRole 列表
        if (source.getRoles() != null)
        {
            List<com.manzhushaka.system.infrastructure.persistence.entity.SysRole> roleList = new ArrayList<>();
            for (com.manzhushaka.common.core.domain.entity.SysRole role : source.getRoles())
            {
                com.manzhushaka.system.infrastructure.persistence.entity.SysRole targetRole =
                        new com.manzhushaka.system.infrastructure.persistence.entity.SysRole();
                BeanUtils.copyProperties(role, targetRole);
                roleList.add(targetRole);
            }
            target.setRoles(roleList);
        }
        return target;
    }
}