package com.manzhushaka.common.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.manzhushaka.common.annotation.SensitiveField;
import com.manzhushaka.common.enums.SensitiveType;

import java.io.IOException;
import java.util.Objects;

/**
 * 根据字段注解配置输出脱敏字符串的序列化器。
 */
public class SensitiveFieldSerializer extends JsonSerializer<Object> implements ContextualSerializer {
    private final SensitiveField sensitiveField;

    /**
     * 创建一个无上下文信息的序列化器。
     */
    public SensitiveFieldSerializer() {
        this(null);
    }

    /**
     * 创建一个绑定字段注解配置的序列化器。
     *
     * @param sensitiveField 字段脱敏注解
     */
    public SensitiveFieldSerializer(SensitiveField sensitiveField) {
        this.sensitiveField = sensitiveField;
    }

    /**
     * 按字段脱敏配置输出 JSON 字段值。
     *
     * @param value 待序列化字段值
     * @param gen JSON 生成器
     * @param serializers 序列化上下文
     * @throws IOException 写出 JSON 失败时抛出
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (sensitiveField == null) {
            gen.writeObject(value);
            return;
        }
        gen.writeString(mask(String.valueOf(value), sensitiveField));
    }

    /**
     * 根据字段注解生成当前字段专用的序列化器实例。
     *
     * @param prov 序列化上下文
     * @param property 当前字段属性
     * @return 当前字段对应的序列化器
     * @throws JsonMappingException 解析字段上下文失败时抛出
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return this;
        }
        SensitiveField annotation = property.getAnnotation(SensitiveField.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(SensitiveField.class);
        }
        if (annotation == null) {
            return prov.findValueSerializer(Objects.requireNonNull(property.getType()), property);
        }
        return new SensitiveFieldSerializer(annotation);
    }

    /**
     * 按注解策略处理字符串脱敏。
     *
     * @param value 原始字符串
     * @param annotation 字段脱敏注解
     * @return 脱敏后的字符串
     */
    private String mask(String value, SensitiveField annotation) {
        return switch (annotation.value()) {
            case FULL -> repeatMask(3, annotation.maskChar());
            case MOBILE -> maskMobile(value, annotation.maskChar());
            case CUSTOM -> maskCustom(value, annotation.prefixKeep(), annotation.suffixKeep(), annotation.maskChar());
        };
    }

    /**
     * 对手机号执行前 3 后 4 的掩码处理。
     *
     * @param value 原始手机号
     * @param maskChar 掩码字符
     * @return 脱敏后的手机号
     */
    private String maskMobile(String value, char maskChar) {
        if (value.length() < 7) {
            return maskCustom(value, 0, 0, maskChar);
        }
        return value.substring(0, 3) + repeatMask(4, maskChar) + value.substring(value.length() - 4);
    }

    /**
     * 按前后保留位数执行通用脱敏。
     *
     * @param value 原始字符串
     * @param prefixKeep 前缀保留位数
     * @param suffixKeep 后缀保留位数
     * @param maskChar 掩码字符
     * @return 脱敏后的字符串
     */
    private String maskCustom(String value, int prefixKeep, int suffixKeep, char maskChar) {
        int safePrefixKeep = Math.max(prefixKeep, 0);
        int safeSuffixKeep = Math.max(suffixKeep, 0);
        if (value.length() <= safePrefixKeep + safeSuffixKeep) {
            return repeatMask(value.length(), maskChar);
        }
        int maskLength = value.length() - safePrefixKeep - safeSuffixKeep;
        return value.substring(0, safePrefixKeep)
            + repeatMask(maskLength, maskChar)
            + value.substring(value.length() - safeSuffixKeep);
    }

    /**
     * 生成指定长度的掩码字符串。
     *
     * @param length 掩码长度
     * @param maskChar 掩码字符
     * @return 掩码字符串
     */
    private String repeatMask(int length, char maskChar) {
        return String.valueOf(maskChar).repeat(Math.max(length, 0));
    }
}
