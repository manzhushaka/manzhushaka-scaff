package com.manzhushaka.framework.security.context;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import com.manzhushaka.common.core.text.Convert;

/**
 * 权限信息
 * 
 * @author manzhushaka
 */
public class PermissionContextHolder
{
    private static final String PERMISSION_CONTEXT_ATTRIBUTES = "PERMISSION_CONTEXT";
    private static final ThreadLocal<String> LOCAL_CONTEXT = new ThreadLocal<>();

    public static void setContext(String permission)
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null)
        {
            attributes.setAttribute(PERMISSION_CONTEXT_ATTRIBUTES, permission, RequestAttributes.SCOPE_REQUEST);
        }
        LOCAL_CONTEXT.set(permission);
    }

    public static String getContext()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null)
        {
            String permission = Convert.toStr(attributes.getAttribute(PERMISSION_CONTEXT_ATTRIBUTES,
                    RequestAttributes.SCOPE_REQUEST));
            if (permission != null)
            {
                return permission;
            }
        }
        return LOCAL_CONTEXT.get();
    }

    /** 清理当前线程权限上下文。 */
    public static void clearContext()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null)
        {
            attributes.removeAttribute(PERMISSION_CONTEXT_ATTRIBUTES, RequestAttributes.SCOPE_REQUEST);
        }
        LOCAL_CONTEXT.remove();
    }
}
