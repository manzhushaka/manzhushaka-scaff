package com.manzhushaka.system.vo.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * 承载 MenuRouteVO 响应数据。
 */
public class MenuRouteVO {
    private String path;
    private String name;
    private String component;
    /**
     * 执行 Route Meta VO 逻辑。
     *
     * @return 处理结果
     */
    private final RouteMetaVO meta = new RouteMetaVO();
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final List<MenuRouteVO> children = new ArrayList<>();

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
     * 返回 meta。
     *
     * @return 字段值
     */
    public RouteMetaVO getMeta() {
        return meta;
    }

    /**
     * 返回 children。
     *
     * @return 字段值
     */
    public List<MenuRouteVO> getChildren() {
        return children;
    }
}
