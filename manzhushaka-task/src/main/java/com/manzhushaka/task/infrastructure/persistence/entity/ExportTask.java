package com.manzhushaka.task.infrastructure.persistence.entity;

/**
 * 异步导出任务实体。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public class ExportTask extends AbstractTaskEntity
{
    private String querySnapshot;

    public String getQuerySnapshot() { return querySnapshot; }
    public void setQuerySnapshot(String querySnapshot) { this.querySnapshot = querySnapshot; }
}
