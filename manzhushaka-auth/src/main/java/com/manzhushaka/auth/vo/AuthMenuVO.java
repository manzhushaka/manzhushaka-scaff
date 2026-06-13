package com.manzhushaka.auth.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 承载 AuthMenuVO 响应数据。
 */
public class AuthMenuVO {
    private Long id;
    private String name;
    private String type;
    private String path;
    private String component;
    private String title;
    private String icon;
    private Boolean hidden;
    private String redirect;
    private String permission;
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final List<AuthMenuVO> children = new ArrayList<>();

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 返回 name。
     *
     * @return 字段值
     */
    public String getName() {
        return name;
    }

    /**
     * 设置 name。
     *
     * @param name name 参数
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 返回 type。
     *
     * @return 字段值
     */
    public String getType() {
        return type;
    }

    /**
     * 设置 type。
     *
     * @param type type 参数
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 返回 path。
     *
     * @return 字段值
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置 path。
     *
     * @param path path 参数
     */
    public void setPath(String path) {
        this.path = path;
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
     * 返回 title。
     *
     * @return 字段值
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置 title。
     *
     * @param title title 参数
     */
    public void setTitle(String title) {
        this.title = title;
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
     * 返回 hidden。
     *
     * @return 字段值
     */
    public Boolean getHidden() {
        return hidden;
    }

    /**
     * 设置 hidden。
     *
     * @param hidden hidden 参数
     */
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * 返回 redirect。
     *
     * @return 字段值
     */
    public String getRedirect() {
        return redirect;
    }

    /**
     * 设置 redirect。
     *
     * @param redirect redirect 参数
     */
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    /**
     * 返回 permission。
     *
     * @return 字段值
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 设置 permission。
     *
     * @param permission permission 参数
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * 返回 children。
     *
     * @return 字段值
     */
    public List<AuthMenuVO> getChildren() {
        return children;
    }
}
