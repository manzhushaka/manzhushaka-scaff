package com.manzhushaka.db.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.manzhushaka.db.meta.BaseEntity;

public class SysDictItem extends BaseEntity {
    private Long dictTypeId;
    @TableField("item_label")
    private String itemLabel;
    @TableField("item_value")
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
