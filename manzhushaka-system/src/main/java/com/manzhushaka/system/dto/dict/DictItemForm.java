package com.manzhushaka.system.dto.dict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 承载 DictItemForm 请求参数。
 */
public class DictItemForm {
    @NotNull(message = "字典类型不能为空")
    private Long dictTypeId;
    @NotBlank(message = "字典标签不能为空")
    private String itemLabel;
    @NotBlank(message = "字典值不能为空")
    private String itemValue;
    private Integer sort;
    private Integer status;

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
