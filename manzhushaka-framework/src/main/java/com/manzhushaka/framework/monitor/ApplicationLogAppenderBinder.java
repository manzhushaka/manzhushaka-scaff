package com.manzhushaka.framework.monitor;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ApplicationLogAppenderBinder {
    private static final String APPENDER_NAME = "manzhushakaApplicationLogBuffer";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final ApplicationLogBuffer logBuffer;

    /**
     * 创建应用日志缓冲绑定器。
     *
     * @param logBuffer 应用日志缓冲区
     */
    public ApplicationLogAppenderBinder(ApplicationLogBuffer logBuffer) {
        this.logBuffer = logBuffer;
    }

    /**
     * 在容器初始化后把缓冲 appender 绑定到 root logger。
     */
    @PostConstruct
    public void bind() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (rootLogger.getAppender(APPENDER_NAME) != null) {
            return;
        }
        LoggerContext context = rootLogger.getLoggerContext();
        BufferingAppender appender = new BufferingAppender(logBuffer);
        appender.setName(APPENDER_NAME);
        appender.setContext(context);
        appender.start();
        rootLogger.addAppender(appender);
    }

    private static final class BufferingAppender extends AppenderBase<ILoggingEvent> {
        private final ApplicationLogBuffer logBuffer;

        /**
         * 创建日志缓冲 appender。
         *
         * @param logBuffer 应用日志缓冲区
         */
        private BufferingAppender(ApplicationLogBuffer logBuffer) {
            this.logBuffer = logBuffer;
        }

        /**
         * 处理一条 logback 日志事件。
         *
         * @param eventObject logback 日志事件
         */
        @Override
        protected void append(ILoggingEvent eventObject) {
            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(eventObject.getTimeStamp()), ZoneId.systemDefault());
            logBuffer.append(time, formatLine(eventObject, time));
        }

        /**
         * 格式化日志行为监控页文本。
         *
         * @param eventObject 日志事件
         * @param time 日志时间
         * @return 格式化日志
         */
        private String formatLine(ILoggingEvent eventObject, LocalDateTime time) {
            StringBuilder builder = new StringBuilder()
                .append(DATE_TIME_FORMATTER.format(time))
                .append(" [")
                .append(eventObject.getThreadName())
                .append("] ")
                .append(eventObject.getLevel())
                .append(" ")
                .append(eventObject.getLoggerName())
                .append(" - ")
                .append(eventObject.getFormattedMessage());
            if (eventObject.getThrowableProxy() != null) {
                builder.append(System.lineSeparator()).append(ThrowableProxyUtil.asString(eventObject.getThrowableProxy()));
            }
            return builder.toString();
        }
    }
}
