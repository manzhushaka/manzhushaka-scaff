package com.manzhushaka.web.dto.system;

/**
 * 菜单查询请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class MenuQueryRequest
{
    private String menuName;
    private String visible;
    private String status;

    public String getMenuName()
    {
        return menuName;
    }

    public void setMenuName(String menuName)
    {
        this.menuName = menuName;
    }

    public String getVisible()
    {
        return visible;
    }

    public void setVisible(String visible)
    {
        this.visible = visible;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
