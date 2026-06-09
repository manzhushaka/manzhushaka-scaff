package com.manzhushaka.system.service.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.system.dto.PageQuery;

public final class SystemPageSupport {

    private SystemPageSupport() {
    }

    public static <T> Page<T> buildPage(PageQuery query) {
        return new Page<>(query.getPageNum(), query.getPageSize());
    }
}
