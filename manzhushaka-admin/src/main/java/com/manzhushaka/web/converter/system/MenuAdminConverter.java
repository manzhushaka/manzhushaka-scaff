package com.manzhushaka.web.converter.system;

import com.manzhushaka.system.application.command.SaveMenuCommand;
import com.manzhushaka.system.application.query.MenuQuery;
import com.manzhushaka.web.dto.system.MenuQueryRequest;
import com.manzhushaka.web.dto.system.MenuSaveRequest;

/**
 * 菜单管理转换器。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public final class MenuAdminConverter
{
    private MenuAdminConverter()
    {
    }

    public static MenuQuery toQuery(MenuQueryRequest request)
    {
        if (request == null)
        {
            return new MenuQuery(null, null, null);
        }
        return new MenuQuery(request.getMenuName(), request.getVisible(), request.getStatus());
    }

    public static SaveMenuCommand toCommand(MenuSaveRequest request)
    {
        return new SaveMenuCommand(request.getMenuId(), request.getMenuName(), request.getParentId(),
                request.getOrderNum(), request.getPath(), request.getComponent(), request.getQuery(),
                request.getRouteName(), request.getIsFrame(), request.getIsCache(),
                request.getMenuType(), request.getVisible(), request.getStatus(), request.getPerms(),
                request.getIcon());
    }
}
