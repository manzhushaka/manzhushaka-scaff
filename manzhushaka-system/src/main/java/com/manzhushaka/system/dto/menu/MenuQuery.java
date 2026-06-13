package com.manzhushaka.system.dto.menu;

/**
 * 承载 MenuQuery 请求参数。
 */
public class MenuQuery {
    private String menuName;
    private String menuType;
    private Integer status;

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
