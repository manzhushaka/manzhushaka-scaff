package com.manzhushaka.task.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Spring 管理的业务任务处理器注册表。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Component
public class TaskHandlerRegistry
{
    private final Map<String, TaskHandler> handlers = new LinkedHashMap<>();

    public TaskHandlerRegistry(List<TaskHandler> handlerList)
    {
        for (TaskHandler handler : handlerList)
        {
            if (handlers.put(handler.handlerType(), handler) != null)
            {
                throw new IllegalStateException("重复的异步任务处理器: " + handler.handlerType());
            }
        }
    }

    /** 获取处理器。 */
    public TaskHandler get(String handlerType)
    {
        TaskHandler handler = handlers.get(handlerType);
        if (handler == null)
        {
            throw new IllegalArgumentException("未注册的异步任务处理器: " + handlerType);
        }
        return handler;
    }
}
