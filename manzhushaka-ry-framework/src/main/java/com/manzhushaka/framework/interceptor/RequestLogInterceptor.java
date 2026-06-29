package com.manzhushaka.framework.interceptor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.manzhushaka.common.enums.BusinessStatus;
import com.manzhushaka.common.utils.ExceptionUtil;
import com.manzhushaka.common.utils.ServletUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.ip.IpUtils;
import com.manzhushaka.framework.manager.AsyncManager;
import com.manzhushaka.framework.manager.factory.AsyncFactory;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.command.RequestLogRecord;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Component
public class RequestLogInterceptor implements HandlerInterceptor
{
    private static final ThreadLocal<Long> START_TIME_THREAD_LOCAL = new NamedThreadLocal<Long>("Request Log Cost Time");

    private static final int PARAM_MAX_LENGTH = 2000;

    private static final int TEXT_MAX_LENGTH = 2000;

    private static final String MASK_VALUE = "******";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        START_TIME_THREAD_LOCAL.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        try
        {
            String requestUri = request.getRequestURI();
            if (shouldExclude(requestUri))
            {
                return;
            }
            Long startTime = START_TIME_THREAD_LOCAL.get();
            long costTime = startTime == null ? 0L : System.currentTimeMillis() - startTime;
            LoginPrincipal principal = SecurityContextHelper.getPrincipalQuietly();
            String userName = principal == null ? null : principal.getUsername();
            Integer status = ex == null && response.getStatus() < HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                    ? BusinessStatus.SUCCESS.ordinal() : BusinessStatus.FAIL.ordinal();

            RequestLogRecord record = RequestLogRecord.builder()
                    .requestUri(StringUtils.substring(requestUri, 0, 255))
                    .requestMethod(request.getMethod())
                    .controllerMethod(resolveControllerMethod(handler))
                    .queryString(StringUtils.substring(maskSensitiveQueryString(request.getQueryString()), 0, 1000))
                    .requestParams(resolveRequestParams(request))
                    .ipaddr(IpUtils.getIpAddr(request))
                    .userName(userName)
                    .statusCode(response.getStatus())
                    .status(status)
                    .errorMsg(resolveErrorMessage(ex))
                    .userAgent(StringUtils.substring(request.getHeader("User-Agent"), 0, 500))
                    .costTime(costTime)
                    .requestTime(new Date())
                    .build();
            AsyncManager.me().execute(AsyncFactory.recordRequest(record));
        }
        finally
        {
            START_TIME_THREAD_LOCAL.remove();
        }
    }

    /**
     * 判断请求路径是否不记录请求日志。
     *
     * @param requestUri 请求路径
     * @return true 表示排除
     */
    public boolean shouldExclude(String requestUri)
    {
        if (StringUtils.isEmpty(requestUri))
        {
            return true;
        }
        return requestUri.startsWith("/monitor/requestLog")
                || requestUri.startsWith("/monitor/runtimeLog")
                || requestUri.startsWith("/druid/")
                || requestUri.startsWith("/profile/")
                || requestUri.startsWith("/assets/")
                || requestUri.startsWith("/static/")
                || requestUri.startsWith("/favicon")
                || "/captchaImage".equals(requestUri)
                || requestUri.endsWith(".js")
                || requestUri.endsWith(".css")
                || requestUri.endsWith(".png")
                || requestUri.endsWith(".jpg")
                || requestUri.endsWith(".jpeg")
                || requestUri.endsWith(".svg")
                || requestUri.endsWith(".ico");
    }

    /**
     * 解析控制器方法名。
     *
     * @param handler 处理器
     * @return 控制器方法名
     */
    private String resolveControllerMethod(Object handler)
    {
        if (!(handler instanceof HandlerMethod handlerMethod))
        {
            return null;
        }
        String className = handlerMethod.getBeanType().getName();
        String methodName = handlerMethod.getMethod().getName();
        return StringUtils.substring(className + "." + methodName + "()", 0, 255);
    }

    /**
     * 解析请求参数摘要。
     *
     * @param request 请求
     * @return 请求参数摘要
     */
    private String resolveRequestParams(HttpServletRequest request)
    {
        Map<String, String> paramMap = ServletUtils.getParamMap(request);
        if (StringUtils.isEmpty(paramMap))
        {
            return null;
        }
        return StringUtils.substring(JSON.toJSONString(maskSensitiveParams(paramMap)), 0, PARAM_MAX_LENGTH);
    }

    /**
     * 对敏感请求参数值做掩码处理。
     *
     * @param paramMap 请求参数
     * @return 掩码后的请求参数
     */
    private Map<String, String> maskSensitiveParams(Map<String, String> paramMap)
    {
        Map<String, String> safeParamMap = new LinkedHashMap<String, String>(paramMap.size());
        for (Map.Entry<String, String> entry : paramMap.entrySet())
        {
            safeParamMap.put(entry.getKey(), isSensitiveParamName(entry.getKey()) ? MASK_VALUE : entry.getValue());
        }
        return safeParamMap;
    }

    /**
     * 对原始查询字符串中的敏感参数值做掩码处理。
     *
     * @param queryString 原始查询字符串
     * @return 掩码后的查询字符串
     */
    private String maskSensitiveQueryString(String queryString)
    {
        if (StringUtils.isEmpty(queryString))
        {
            return queryString;
        }
        String[] parts = queryString.split("&");
        StringBuilder builder = new StringBuilder(queryString.length());
        for (int i = 0; i < parts.length; i++)
        {
            if (i > 0)
            {
                builder.append('&');
            }
            String part = parts[i];
            int separatorIndex = part.indexOf('=');
            String paramName = separatorIndex < 0 ? part : part.substring(0, separatorIndex);
            if (isSensitiveParamName(paramName))
            {
                builder.append(paramName).append('=').append(MASK_VALUE);
            }
            else
            {
                builder.append(part);
            }
        }
        return builder.toString();
    }

    /**
     * 判断参数名是否为敏感字段。
     *
     * @param paramName 参数名
     * @return true 表示敏感
     */
    private boolean isSensitiveParamName(String paramName)
    {
        if (StringUtils.isEmpty(paramName))
        {
            return false;
        }
        String lowerParamName = paramName.toLowerCase(Locale.ROOT);
        return lowerParamName.contains("password")
                || lowerParamName.contains("token")
                || lowerParamName.contains("secret")
                || lowerParamName.contains("authorization");
    }

    /**
     * 解析异常摘要。
     *
     * @param ex 异常
     * @return 异常摘要
     */
    private String resolveErrorMessage(Exception ex)
    {
        if (ex == null)
        {
            return null;
        }
        return StringUtils.substring(ExceptionUtil.getExceptionMessage(ex), 0, TEXT_MAX_LENGTH);
    }
}
