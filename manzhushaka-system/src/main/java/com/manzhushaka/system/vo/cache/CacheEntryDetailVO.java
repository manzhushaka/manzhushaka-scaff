package com.manzhushaka.system.vo.cache;

/**
 * 承载 CacheEntryDetailVO 响应数据。
 */
public class CacheEntryDetailVO extends CacheEntryVO {
    private Object value;

    /**
     * 返回 value。
     *
     * @return 字段值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置 value。
     *
     * @param value 字段值
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
