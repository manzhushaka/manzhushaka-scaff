package com.manzhushaka.system.vo;

/**
 * 承载 LabelValueOption 响应数据。
 */
public class LabelValueOption {
    private final String label;
    private final String value;

    /**
     * 创建 LabelValueOption 实例。
     *
     * @param label label 参数
     * @param value 字段值
     */
    public LabelValueOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    /**
     * 返回 label。
     *
     * @return 字段值
     */
    public String getLabel() {
        return label;
    }

    /**
     * 返回 value。
     *
     * @return 字段值
     */
    public String getValue() {
        return value;
    }
}
