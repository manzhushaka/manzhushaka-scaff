package com.manzhushaka.system.application.result.system;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.infrastructure.persistence.entity.SysMenu;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.domain.vo.MetaVo;
import com.manzhushaka.system.domain.vo.RouterVo;

/**
 * 系统查询结果转换器。
 *
 * Entity 到 Application Result 的转换集中在 system 模块内部，避免 Web 层依赖持久化模型。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class SystemResultMapper
{
    private SystemResultMapper()
    {
    }

    /**
     * 转换用户列表。
     *
     * @param users 用户实体列表
     * @return 用户结果列表
     */
    public static List<UserResult> toUserResults(List<SysUser> users)
    {
        if (users == null || users.isEmpty())
        {
            return Collections.emptyList();
        }
        return users.stream().map(SystemResultMapper::toUserResult).collect(Collectors.toList());
    }

    /**
     * 转换用户实体。
     *
     * @param user 用户实体
     * @return 用户结果
     */
    public static UserResult toUserResult(SysUser user)
    {
        if (user == null)
        {
            return null;
        }
        List<RoleResult> roles = user.getRoles() == null ? Collections.emptyList()
                : user.getRoles().stream().map(SystemResultMapper::toRoleResult).collect(Collectors.toList());
        Long[] roleIds = user.getRoleIds();
        if (roleIds == null && user.getRoles() != null)
        {
            roleIds = user.getRoles().stream().map(SysRole::getRoleId).toArray(Long[]::new);
        }
        return new UserResult(user.getUserId(), user.getDeptId(), user.getUserName(),
                user.getNickName(), user.getEmail(), user.getPhonenumber(), user.getSex(),
                user.getAvatar(), user.getStatus(), user.getDelFlag(), user.getLoginIp(),
                user.getLoginDate(), toDeptResult(user.getDept()), roles, roleIds,
                user.getRoleId(), user.getCreateBy(), user.getCreateTime(), user.getUpdateBy(),
                user.getUpdateTime(), user.getRemark());
    }

    /**
     * 转换角色列表。
     *
     * @param roles 角色实体列表
     * @return 角色结果列表
     */
    public static List<RoleResult> toRoleResults(List<SysRole> roles)
    {
        if (roles == null || roles.isEmpty())
        {
            return Collections.emptyList();
        }
        return roles.stream().map(SystemResultMapper::toRoleResult).collect(Collectors.toList());
    }

    /**
     * 转换角色实体。
     *
     * @param role 角色实体
     * @return 角色结果
     */
    public static RoleResult toRoleResult(SysRole role)
    {
        if (role == null)
        {
            return null;
        }
        return new RoleResult(role.getRoleId(), role.getRoleName(), role.getRoleKey(),
                role.getRoleSort(), role.getDataScope(), role.isMenuCheckStrictly(),
                role.isDeptCheckStrictly(), role.getStatus(), role.getDelFlag(), role.isFlag(),
                role.getMenuIds(), role.getDeptIds(), role.getPermissions(), role.getCreateBy(),
                role.getCreateTime(), role.getUpdateBy(), role.getUpdateTime(), role.getRemark());
    }

    /**
     * 转换部门实体。
     *
     * @param dept 部门实体
     * @return 部门结果
     */
    public static DeptResult toDeptResult(SysDept dept)
    {
        if (dept == null)
        {
            return null;
        }
        List<DeptResult> children = dept.getChildren() == null ? Collections.emptyList()
                : dept.getChildren().stream().map(SystemResultMapper::toDeptResult).collect(Collectors.toList());
        return new DeptResult(dept.getDeptId(), dept.getParentId(), dept.getAncestors(),
                dept.getDeptName(), dept.getOrderNum(), dept.getLeader(), dept.getPhone(),
                dept.getEmail(), dept.getStatus(), dept.getDelFlag(), dept.getDeptType(),
                dept.getRegionCode(), dept.getRegionLevel(), dept.getParentName(),
                dept.getCreateBy(), dept.getCreateTime(), dept.getUpdateBy(), dept.getUpdateTime(),
                dept.getRemark(), children);
    }

    /**
     * 转换部门列表。
     *
     * @param depts 部门实体列表
     * @return 部门结果列表
     */
    public static List<DeptResult> toDeptResults(List<SysDept> depts)
    {
        if (depts == null || depts.isEmpty())
        {
            return Collections.emptyList();
        }
        return depts.stream().map(SystemResultMapper::toDeptResult).collect(Collectors.toList());
    }

    /**
     * 转换菜单实体。
     *
     * @param menu 菜单实体
     * @return 菜单结果
     */
    public static MenuResult toMenuResult(SysMenu menu)
    {
        if (menu == null)
        {
            return null;
        }
        List<MenuResult> children = menu.getChildren() == null ? Collections.emptyList()
                : menu.getChildren().stream().map(SystemResultMapper::toMenuResult).collect(Collectors.toList());
        return new MenuResult(menu.getMenuId(), menu.getMenuName(), menu.getParentName(),
                menu.getParentId(), menu.getOrderNum(), menu.getPath(), menu.getComponent(),
                menu.getQuery(), menu.getRouteName(), menu.getIsFrame(), menu.getIsCache(),
                menu.getMenuType(), menu.getVisible(), menu.getStatus(), menu.getPerms(),
                menu.getIcon(), menu.getCreateBy(), menu.getCreateTime(), menu.getUpdateBy(),
                menu.getUpdateTime(), menu.getRemark(), children);
    }

    /**
     * 转换菜单列表。
     *
     * @param menus 菜单实体列表
     * @return 菜单结果列表
     */
    public static List<MenuResult> toMenuResults(List<SysMenu> menus)
    {
        if (menus == null || menus.isEmpty())
        {
            return Collections.emptyList();
        }
        return menus.stream().map(SystemResultMapper::toMenuResult).collect(Collectors.toList());
    }

    /**
     * 转换用户导入导出行。
     *
     * @param users 用户实体列表
     * @return 用户行列表
     */
    public static List<UserExcelRow> toUserExcelRows(List<SysUser> users)
    {
        if (users == null || users.isEmpty())
        {
            return Collections.emptyList();
        }
        return users.stream().map(user -> new UserExcelRow(user.getUserId(), user.getDeptId(),
                user.getUserName(), user.getNickName(), user.getEmail(), user.getPhonenumber(),
                user.getSex(), user.getStatus(), user.getDept() == null ? null : user.getDept().getDeptName(),
                user.getDept() == null ? null : user.getDept().getLeader(), user.getLoginIp(),
                user.getLoginDate())).collect(Collectors.toList());
    }

    /**
     * 转换角色导出行。
     *
     * @param roles 角色实体列表
     * @return 角色行列表
     */
    public static List<RoleExcelRow> toRoleExcelRows(List<SysRole> roles)
    {
        if (roles == null || roles.isEmpty())
        {
            return Collections.emptyList();
        }
        return roles.stream().map(role -> new RoleExcelRow(role.getRoleId(), role.getRoleName(),
                role.getRoleKey(), role.getRoleSort(), role.getDataScope(), role.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 转换路由列表。
     *
     * @param routers 路由对象列表
     * @return 路由结果列表
     */
    public static List<RouterResult> toRouterResults(List<RouterVo> routers)
    {
        if (routers == null || routers.isEmpty())
        {
            return Collections.emptyList();
        }
        return routers.stream().map(SystemResultMapper::toRouterResult).collect(Collectors.toList());
    }

    private static RouterResult toRouterResult(RouterVo router)
    {
        MetaVo meta = router.getMeta();
        MetaResult metaResult = meta == null ? null
                : new MetaResult(meta.getTitle(), meta.getIcon(), meta.isNoCache(), meta.getLink());
        return new RouterResult(router.getName(), router.getPath(), router.getHidden(),
                router.getRedirect(), router.getComponent(), router.getQuery(),
                router.getAlwaysShow(), metaResult, toRouterResults(router.getChildren()));
    }
}
