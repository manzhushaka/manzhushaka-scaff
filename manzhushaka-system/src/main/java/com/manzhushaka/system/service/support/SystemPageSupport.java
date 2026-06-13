package com.manzhushaka.system.service.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.system.dto.PageQuery;

/**
 * 定义 SystemPageSupport。
 */
public final class SystemPageSupport {

    /**
     * 创建 SystemPageSupport 实例。
     */
    private SystemPageSupport() {
    }

    /**
     * 构建分页参数。
     *
     * @param query 查询条件
     * @return 处理结果
     */
    public static <T> Page<T> buildPage(PageQuery query) {
        return new Page<>(query.getPageNum(), query.getPageSize());
    }
}
