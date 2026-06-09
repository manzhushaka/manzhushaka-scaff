package com.manzhushaka.system.dto.dict;

import jakarta.validation.constraints.NotBlank;

public class DictTypeForm {
    @NotBlank(message = "字典名称不能为空")
    private String dictName;
    @NotBlank(message = "字典编码不能为空")
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
