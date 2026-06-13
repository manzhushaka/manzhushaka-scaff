package com.manzhushaka.system.service.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.system.vo.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * 定义 SystemMappingSupport。
 */
public final class SystemMappingSupport {

    /**
     * 创建 SystemMappingSupport 实例。
     */
    private SystemMappingSupport() {
    }

    /**
     * 转换分页结果。
     *
     * @param page page 参数
     * @param mapper 映射函数
     * @return 处理结果
     */
    public static <T, R> PageResult<R> toPageResult(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(page.getTotal(), page.getRecords().stream().map(mapper).toList());
    }

    /**
     * 转换列表数据。
     *
     * @param source source 参数
     * @param mapper 映射函数
     * @return 处理结果
     */
    public static <T, R> List<R> mapList(List<T> source, Function<T, R> mapper) {
        return source.stream().map(mapper).toList();
    }
}
