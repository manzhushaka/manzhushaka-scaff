package com.manzhushaka.task.infrastructure.persistence.entity;

/**
 * 异步导入任务实体。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public class ImportTask extends AbstractTaskEntity
{
    private Boolean updateSupport;
    private String optionsSnapshot;

    public Boolean getUpdateSupport() { return updateSupport; }
    public void setUpdateSupport(Boolean updateSupport) { this.updateSupport = updateSupport; }
    public String getOptionsSnapshot() { return optionsSnapshot; }
    public void setOptionsSnapshot(String optionsSnapshot) { this.optionsSnapshot = optionsSnapshot; }
}
