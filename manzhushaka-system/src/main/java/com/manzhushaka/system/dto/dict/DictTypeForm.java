package com.manzhushaka.system.dto.dict;

import jakarta.validation.constraints.NotBlank;

/**
 * 承载 DictTypeForm 请求参数。
 */
public class DictTypeForm {
    @NotBlank(message = "字典名称不能为空")
    private String dictName;
    @NotBlank(message = "字典编码不能为空")
    private String dictCode;
    private Integer status;

    /**
     * 返回 dictName。
     *
     * @return 字段值
     */
    public String getDictName() {
        return dictName;
    }

    /**
     * 设置 dictName。
     *
     * @param dictName dictName 参数
     */
    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    /**
     * 返回 dictCode。
     *
     * @return 字段值
     */
    public String getDictCode() {
        return dictCode;
    }

    /**
     * 设置 dictCode。
     *
     * @param dictCode dictCode 参数
     */
    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
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
