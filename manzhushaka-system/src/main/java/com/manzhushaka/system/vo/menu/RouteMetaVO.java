package com.manzhushaka.system.vo.menu;

/**
 * 承载 RouteMetaVO 响应数据。
 */
public class RouteMetaVO {
    private String title;
    private String icon;
    private boolean hidden;
    private boolean keepAlive;

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
     * 判断是否 hidden。
     *
     * @return 字段值
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * 设置 hidden。
     *
     * @param hidden hidden 参数
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * 判断是否 keepAlive。
     *
     * @return 字段值
     */
    public boolean isKeepAlive() {
        return keepAlive;
    }

    /**
     * 设置 keepAlive。
     *
     * @param keepAlive keepAlive 参数
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
}
