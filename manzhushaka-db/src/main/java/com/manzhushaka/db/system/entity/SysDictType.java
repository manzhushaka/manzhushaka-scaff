package com.manzhushaka.db.system.entity;

import com.manzhushaka.db.meta.BaseEntity;

public class SysDictType extends BaseEntity {
    private String dictName;
    private String dictCode;
    private Integer status;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
