package com.manzhushaka.db.meta;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

/**
 * 定义 BaseEntity。
 */
public class BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

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
     * 返回 createBy。
     *
     * @return 字段值
     */
    public String getCreateBy() {
        return createBy;
    }

    /**
     * 设置 createBy。
     *
     * @param createBy createBy 参数
     */
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
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
     * 返回 updateBy。
     *
     * @return 字段值
     */
    public String getUpdateBy() {
        return updateBy;
    }

    /**
     * 设置 updateBy。
     *
     * @param updateBy updateBy 参数
     */
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    /**
     * 返回 updateTime。
     *
     * @return 字段值
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置 updateTime。
     *
     * @param updateTime updateTime 参数
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
