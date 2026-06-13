package com.manzhushaka.system.vo.dict;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 承载 DictTypeVO 响应数据。
 */
public class DictTypeVO {
    private Long id;
    private String dictName;
    private String dictCode;
    private Integer status;
    private LocalDateTime createTime;
    private List<DictItemVO> items;

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

    /**
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 返回 items。
     *
     * @return 字段值
     */
    public List<DictItemVO> getItems() {
        return items;
    }

    /**
     * 设置 items。
     *
     * @param items items 参数
     */
    public void setItems(List<DictItemVO> items) {
        this.items = items;
    }
}
