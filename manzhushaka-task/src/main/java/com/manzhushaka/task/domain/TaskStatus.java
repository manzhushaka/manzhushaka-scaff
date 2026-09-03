package com.manzhushaka.task.domain;

/**
 * 异步任务状态。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
public enum TaskStatus
{
    PENDING,
    RUNNING,
    CANCEL_REQUESTED,
    CANCELLED,
    SUCCESS,
    PARTIAL_SUCCESS,
    FAILED
}
