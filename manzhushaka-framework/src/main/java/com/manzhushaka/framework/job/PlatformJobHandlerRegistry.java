package com.manzhushaka.framework.job;

import com.manzhushaka.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 定义 PlatformJobHandlerRegistry。
 */
@Component
public class PlatformJobHandlerRegistry {
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final Map<String, PlatformJobHandler> handlers = new LinkedHashMap<>();

    /**
     * 创建 PlatformJobHandlerRegistry 实例。
     *
     * @param handlers handlers 参数
     */
    public PlatformJobHandlerRegistry(List<PlatformJobHandler> handlers) {
        for (PlatformJobHandler handler : handlers) {
            this.handlers.put(handler.handlerName(), handler);
        }
    }

    /**
     * 返回 required。
     *
     * @param handlerName handlerName 参数
     * @return 字段值
     */
    public PlatformJobHandler getRequired(String handlerName) {
        PlatformJobHandler handler = handlers.get(handlerName);
        if (handler == null) {
            throw new BizException(404, "未找到定时任务处理器");
        }
        return handler;
    }

    /**
     * 查询列表。
     *
     * @return 查询结果
     */
    public List<PlatformJobHandler> list() {
        return handlers.values().stream().toList();
    }
}
