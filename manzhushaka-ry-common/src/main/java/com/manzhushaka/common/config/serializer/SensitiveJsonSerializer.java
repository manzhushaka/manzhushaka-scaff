package com.manzhushaka.common.config.serializer;

import java.util.Objects;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;
import com.manzhushaka.common.annotation.Sensitive;
import com.manzhushaka.common.enums.DesensitizedType;

/**
 * 数据脱敏序列化过滤
 * <p>
 * 注意：该序列化器位于 common 模块，不能直接依赖 framework 的 LoginPrincipal。
 * 通过 {@code SecurityContextHolder} 直接获取并强硬转运行时类型。
 *
 * @author manzhushaka
 */
public class SensitiveJsonSerializer extends StdSerializer<String>
{
    private final DesensitizedType desensitizedType;

    public SensitiveJsonSerializer()
    {
        super(String.class);
        this.desensitizedType = null;
    }

    public SensitiveJsonSerializer(DesensitizedType desensitizedType)
    {
        super(String.class);
        this.desensitizedType = desensitizedType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException
    {
        if (desensitizedType != null && desensitization())
        {
            gen.writeString(desensitizedType.desensitizer().apply(value));
        }
        else
        {
            gen.writeString(value);
        }
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) throws DatabindException
    {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass()))
        {
            return new SensitiveJsonSerializer(annotation.desensitizedType());
        }
        return ctxt.findValueSerializer(property.getType());
    }

    /**
     * 是否需要脱敏处理
     * <p>
     * 使用 {@code SecurityContextHolder} 直接读取 Authentication，
     * 并通过全限定名强制转型为 {@code LoginPrincipal}，避免 common 模块对 framework 的编译期依赖。
     */
    private boolean desensitization()
    {
        try
        {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() != null
                    && auth.getPrincipal().getClass().getName()
                            .equals("com.manzhushaka.framework.security.model.LoginPrincipal"))
            {
                // 使用反射调用 isAdmin() 方法，避免编译期依赖
                Object principal = auth.getPrincipal();
                java.lang.reflect.Method isAdminMethod = principal.getClass().getMethod("isAdmin");
                boolean isAdmin = (boolean) isAdminMethod.invoke(principal);
                return !isAdmin;
            }
        }
        catch (Exception e)
        {
            // 非认证状态下默认脱敏
        }
        return true;
    }
}
