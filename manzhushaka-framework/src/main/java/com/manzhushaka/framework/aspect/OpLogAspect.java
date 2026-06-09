package com.manzhushaka.framework.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.annotation.OpLog;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.context.LoginUserContext;
import com.manzhushaka.common.model.OpLogRecord;
import com.manzhushaka.common.spi.OpLogPublisher;
import com.manzhushaka.framework.util.SensitiveDataSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
public class OpLogAspect {
    private final ObjectMapper objectMapper;
    private final ObjectProvider<OpLogPublisher> publisherProvider;

    public OpLogAspect(ObjectMapper objectMapper, ObjectProvider<OpLogPublisher> publisherProvider) {
        this.objectMapper = objectMapper;
        this.publisherProvider = publisherProvider;
    }

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint joinPoint, OpLog opLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable exception) {
            throwable = exception;
            throw exception;
        } finally {
            OpLogPublisher publisher = publisherProvider.getIfAvailable();
            if (publisher != null) {
                publisher.publish(buildRecord(joinPoint, opLog, result, throwable, System.currentTimeMillis() - startTime));
            }
        }
    }

    private OpLogRecord buildRecord(ProceedingJoinPoint joinPoint, OpLog opLog, Object result, Throwable throwable, long costMs) {
        HttpServletRequest request = currentRequest();
        LoginUser loginUser = LoginUserContext.get();

        OpLogRecord record = new OpLogRecord();
        record.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        record.setModule(opLog.module());
        record.setAction(opLog.action());
        record.setBusinessType(opLog.businessType().name());
        record.setRequestUri(request == null ? null : request.getRequestURI());
        record.setRequestMethod(request == null ? null : request.getMethod());
        record.setOperatorId(loginUser == null ? null : loginUser.getUserId());
        record.setOperatorName(loginUser == null ? null : loginUser.getUsername());
        record.setCostMs(costMs);
        record.setSuccess(throwable == null);
        record.setErrorMsg(throwable == null ? null : summarizeThrowable(throwable));
        record.setRequestSnapshot(opLog.recordRequest() ? toJsonSafe(SensitiveDataSanitizer.mask(joinPoint.getArgs())) : null);
        record.setResponseSnapshot(opLog.recordResponse() ? toJsonSafe(SensitiveDataSanitizer.mask(result)) : null);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String summarizeThrowable(Throwable throwable) {
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }

    private String toJsonSafe(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return String.valueOf(value);
        }
    }
}
