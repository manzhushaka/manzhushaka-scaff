package com.manzhushaka.system.dto.dict;

import com.manzhushaka.system.dto.PageQuery;

public class DictTypeQuery extends PageQuery {
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
