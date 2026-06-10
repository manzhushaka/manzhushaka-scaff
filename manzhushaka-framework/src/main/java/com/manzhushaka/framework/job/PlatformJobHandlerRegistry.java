package com.manzhushaka.framework.job;

import com.manzhushaka.common.exception.BizException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformJobHandlerRegistry {
    private final Map<String, PlatformJobHandler> handlers = new LinkedHashMap<>();

    public PlatformJobHandlerRegistry(List<PlatformJobHandler> handlers) {
        for (PlatformJobHandler handler : handlers) {
            this.handlers.put(handler.handlerName(), handler);
        }
    }

    public PlatformJobHandler getRequired(String handlerName) {
        PlatformJobHandler handler = handlers.get(handlerName);
        if (handler == null) {
            throw new BizException(404, "未找到定时任务处理器");
        }
        return handler;
    }

    public List<PlatformJobHandler> list() {
        return handlers.values().stream().toList();
    }
}
