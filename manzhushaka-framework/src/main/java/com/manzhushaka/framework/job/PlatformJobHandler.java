package com.manzhushaka.framework.job;

public interface PlatformJobHandler {

    String handlerName();

    String handlerLabel();

    default void execute(PlatformJobExecutionContext context) throws Exception {
    }
}
