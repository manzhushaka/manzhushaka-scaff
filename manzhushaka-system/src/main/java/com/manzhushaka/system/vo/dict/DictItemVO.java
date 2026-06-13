package com.manzhushaka.system.vo.dict;

/**
 * 承载 DictItemVO 响应数据。
 */
public class DictItemVO {
    private Long id;
    private Long dictTypeId;
    private String itemLabel;
    private String itemValue;
    private Integer sort;
    private Integer status;

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
     * 返回 dictTypeId。
     *
     * @return 字段值
     */
    public Long getDictTypeId() {
        return dictTypeId;
    }

    /**
     * 设置 dictTypeId。
     *
     * @param dictTypeId dictTypeId 标识
     */
    public void setDictTypeId(Long dictTypeId) {
        this.dictTypeId = dictTypeId;
    }

    /**
     * 返回 itemLabel。
     *
     * @return 字段值
     */
    public String getItemLabel() {
        return itemLabel;
    }

    /**
     * 设置 itemLabel。
     *
     * @param itemLabel itemLabel 参数
     */
    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    /**
     * 返回 itemValue。
     *
     * @return 字段值
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * 设置 itemValue。
     *
     * @param itemValue itemValue 参数
     */
    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
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
