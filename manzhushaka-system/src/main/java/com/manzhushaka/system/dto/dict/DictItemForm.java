package com.manzhushaka.system.dto.dict;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DictItemForm {
    @NotNull(message = "字典类型不能为空")
    private Long dictTypeId;
    @NotBlank(message = "字典标签不能为空")
    private String itemLabel;
    @NotBlank(message = "字典值不能为空")
    private String itemValue;
    private Integer sort;
    private Integer status;

    public Long getDictTypeId() {
        return dictTypeId;
    }

    public void setDictTypeId(Long dictTypeId) {
        this.dictTypeId = dictTypeId;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
