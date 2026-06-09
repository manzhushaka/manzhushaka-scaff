package com.manzhushaka.system.vo.menu;

import java.util.ArrayList;
import java.util.List;

public class MenuRouteVO {
    private String path;
    private String name;
    private String component;
    private final RouteMetaVO meta = new RouteMetaVO();
    private final List<MenuRouteVO> children = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public RouteMetaVO getMeta() {
        return meta;
    }

    public List<MenuRouteVO> getChildren() {
        return children;
    }
}
