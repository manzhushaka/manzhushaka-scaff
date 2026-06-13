package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

/**
 * 映射 SysMenu 数据库实体。
 */
public class SysMenu extends BaseEntity {
    private Long parentId;
    private String menuType;
    private String menuName;
    private String routePath;
    private String routeName;
    private String component;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer keepAlive;
    private String perms;
    private Integer status;

    /**
     * 返回 parentId。
     *
     * @return 字段值
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置 parentId。
     *
     * @param parentId parentId 标识
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 返回 menuType。
     *
     * @return 字段值
     */
    public String getMenuType() {
        return menuType;
    }

    /**
     * 设置 menuType。
     *
     * @param menuType menuType 参数
     */
    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    /**
     * 返回 menuName。
     *
     * @return 字段值
     */
    public String getMenuName() {
        return menuName;
    }

    /**
     * 设置 menuName。
     *
     * @param menuName menuName 参数
     */
    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    /**
     * 返回 routePath。
     *
     * @return 字段值
     */
    public String getRoutePath() {
        return routePath;
    }

    /**
     * 设置 routePath。
     *
     * @param routePath routePath 参数
     */
    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    /**
     * 返回 routeName。
     *
     * @return 字段值
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     * 设置 routeName。
     *
     * @param routeName routeName 参数
     */
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    /**
     * 返回 component。
     *
     * @return 字段值
     */
    public String getComponent() {
        return component;
    }

    /**
     * 设置 component。
     *
     * @param component component 参数
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * 返回 icon。
     *
     * @return 字段值
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置 icon。
     *
     * @param icon icon 参数
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 返回 sort。
     *
     * @return 字段值
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置 sort。
     *
     * @param sort sort 参数
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 返回 visible。
     *
     * @return 字段值
     */
    public Integer getVisible() {
        return visible;
    }

    /**
     * 设置 visible。
     *
     * @param visible visible 参数
     */
    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    /**
     * 返回 keepAlive。
     *
     * @return 字段值
     */
    public Integer getKeepAlive() {
        return keepAlive;
    }

    /**
     * 设置 keepAlive。
     *
     * @param keepAlive keepAlive 参数
     */
    public void setKeepAlive(Integer keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * 返回 perms。
     *
     * @return 字段值
     */
    public String getPerms() {
        return perms;
    }

    /**
     * 设置 perms。
     *
     * @param perms perms 参数
     */
    public void setPerms(String perms) {
        this.perms = perms;
    }

    /**
     * 返回 status。
     *
     * @return 字段值
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status status 参数
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
