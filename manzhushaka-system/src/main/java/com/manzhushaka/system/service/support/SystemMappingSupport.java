package com.manzhushaka.system.service.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.system.vo.PageResult;

import java.util.List;
import java.util.function.Function;

public final class SystemMappingSupport {

    private SystemMappingSupport() {
    }

    public static <T, R> PageResult<R> toPageResult(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(page.getTotal(), page.getRecords().stream().map(mapper).toList());
    }

    public static <T, R> List<R> mapList(List<T> source, Function<T, R> mapper) {
        return source.stream().map(mapper).toList();
    }
}
