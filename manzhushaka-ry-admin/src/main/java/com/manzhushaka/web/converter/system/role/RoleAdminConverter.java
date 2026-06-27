package com.manzhushaka.web.converter.system.role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.manzhushaka.system.infrastructure.persistence.entity.SysRole;
import com.manzhushaka.system.application.command.CancelAuthUserCommand;
import com.manzhushaka.system.application.command.ChangeRoleStatusCommand;
import com.manzhushaka.system.application.command.CreateRoleCommand;
import com.manzhushaka.system.application.command.DataScopeCommand;
import com.manzhushaka.system.application.command.UpdateRoleCommand;
import com.manzhushaka.system.application.query.RoleListQuery;
import com.manzhushaka.web.dto.system.role.CancelAuthUserRequest;
import com.manzhushaka.web.dto.system.role.ChangeRoleStatusRequest;
import com.manzhushaka.web.dto.system.role.CreateRoleRequest;
import com.manzhushaka.web.dto.system.role.DataScopeRequest;
import com.manzhushaka.web.dto.system.role.RoleListRequest;
import com.manzhushaka.web.dto.system.role.UpdateRoleRequest;
import com.manzhushaka.web.vo.system.role.RoleDetailVO;
import com.manzhushaka.web.vo.system.role.RoleListVO;

/**
 * 角色管理转换器
 *
 * @author manzhushaka
 */
public class RoleAdminConverter
{
    /**
     * RoleListRequest -> RoleListQuery
     */
    public static RoleListQuery toRoleListQuery(RoleListRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new RoleListQuery(
                request.getPageNum(),
                request.getPageSize(),
                request.getRoleName(),
                request.getRoleKey(),
                request.getStatus(),
                request.getBeginTime(),
                request.getEndTime());
    }

    /**
     * CreateRoleRequest -> CreateRoleCommand
     */
    public static CreateRoleCommand toCreateRoleCommand(CreateRoleRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new CreateRoleCommand(
                request.getRoleId(),
                request.getRoleName(),
                request.getRoleKey(),
                request.getRoleSort(),
                request.getDataScope(),
                request.getStatus(),
                request.getMenuIds(),
                request.getDeptIds(),
                request.getRemark());
    }

    /**
     * UpdateRoleRequest -> UpdateRoleCommand
     */
    public static UpdateRoleCommand toUpdateRoleCommand(UpdateRoleRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new UpdateRoleCommand(
                request.getRoleId(),
                request.getRoleName(),
                request.getRoleKey(),
                request.getRoleSort(),
                request.getDataScope(),
                request.getStatus(),
                request.getMenuIds(),
                request.getDeptIds(),
                request.getRemark());
    }

    /**
     * DataScopeRequest -> DataScopeCommand
     */
    public static DataScopeCommand toDataScopeCommand(DataScopeRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new DataScopeCommand(request.getRoleId(), request.getDataScope(), request.getDeptIds());
    }

    /**
     * ChangeRoleStatusRequest -> ChangeRoleStatusCommand
     */
    public static ChangeRoleStatusCommand toChangeRoleStatusCommand(ChangeRoleStatusRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new ChangeRoleStatusCommand(request.getRoleId(), request.getStatus());
    }

    /**
     * CancelAuthUserRequest -> CancelAuthUserCommand
     */
    public static CancelAuthUserCommand toCancelAuthUserCommand(CancelAuthUserRequest request)
    {
        if (request == null)
        {
            return null;
        }
        return new CancelAuthUserCommand(request.getRoleId(), new Long[] { request.getUserId() });
    }

    /**
     * SysRole -> RoleListVO
     */
    public static RoleListVO toRoleListVO(SysRole role)
    {
        if (role == null)
        {
            return null;
        }
        RoleListVO vo = new RoleListVO();
        vo.setRoleId(role.getRoleId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleSort(role.getRoleSort());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setFlag(role.isFlag());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }

    /**
     * List<SysRole> -> List<RoleListVO>
     */
    public static List<RoleListVO> toRoleListVO(List<SysRole> roles)
    {
        if (roles == null)
        {
            return new ArrayList<>();
        }
        return roles.stream()
                .map(RoleAdminConverter::toRoleListVO)
                .collect(Collectors.toList());
    }

    /**
     * SysRole -> RoleDetailVO
     */
    public static RoleDetailVO toRoleDetailVO(SysRole role)
    {
        if (role == null)
        {
            return null;
        }
        RoleDetailVO vo = new RoleDetailVO();
        vo.setRoleId(role.getRoleId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleSort(role.getRoleSort());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setDelFlag(role.getDelFlag());
        vo.setRemark(role.getRemark());
        vo.setPermissions(role.getPermissions());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }
}