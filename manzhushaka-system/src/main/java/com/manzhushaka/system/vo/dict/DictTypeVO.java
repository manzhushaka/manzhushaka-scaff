package com.manzhushaka.system.vo.dict;

import java.time.LocalDateTime;
import java.util.List;

public class DictTypeVO {
    private Long id;
    private String dictName;
    private String dictCode;
    private Integer status;
    private LocalDateTime createTime;
    private List<DictItemVO> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<DictItemVO> getItems() {
        return items;
    }

    public void setItems(List<DictItemVO> items) {
        this.items = items;
    }
}
