package com.manzhushaka.mq.service;

/**
 * 定义 MqMessageAdminService 服务能力。
 */
public interface MqMessageAdminService {
    /**
     * 处理 retry 流程。
     *
     * @param id 主键 ID
     */
    void retry(Long id);
}
