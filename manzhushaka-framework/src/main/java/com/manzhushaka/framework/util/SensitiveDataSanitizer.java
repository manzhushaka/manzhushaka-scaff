package com.manzhushaka.framework.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SensitiveDataSanitizer {
    private static final Set<String> FULL_MASK_KEYS = Set.of("password", "token", "authorization", "accessToken");
    private static final Set<String> MOBILE_KEYS = Set.of("mobile", "phone", "telephone");

    private SensitiveDataSanitizer() {
    }

    public static Object mask(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                result.put(key, maskByKey(key, entry.getValue()));
            }
            return result;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(SensitiveDataSanitizer::mask).toList();
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object[] masked = new Object[length];
            for (int index = 0; index < length; index++) {
                masked[index] = mask(Array.get(value, index));
            }
            return List.of(masked);
        }
        return value;
    }

    private static Object maskByKey(String key, Object rawValue) {
        if (FULL_MASK_KEYS.contains(key)) {
            return "***";
        }
        if (MOBILE_KEYS.contains(key) && rawValue instanceof String mobile && mobile.length() >= 7) {
            return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
        }
        return mask(rawValue);
    }
}
