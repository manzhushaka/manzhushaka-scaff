package com.manzhushaka.biz.pii.infrastructure.gateway.notify;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 银联发票平台 / 银联公众号支付通用签名工具。
 */
public final class NotifyVerifier {

    private NotifyVerifier() {
    }

    /**
     * 生成签名：自动排除 sign / 空值，按 key 的 ASCII 字典序排序后追加密钥。
     *
     * @param params 待签名参数
     * @param key 签名密钥
     * @return SHA-256 大写十六进制签名
     */
    public static String sign(Map<String, ?> params, String key) {
        String plainText = params.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> !"sign".equalsIgnoreCase(entry.getKey()))
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> !entry.getValue().toString().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        return sha256Hex(plainText + "&key=" + key).toUpperCase();
    }

    /**
     * 验签：传入的 sign 字段不参与计算。
     *
     * @param params 通知参数
     * @param key 签名密钥
     * @return 签名一致时返回 true
     */
    public static boolean verify(Map<String, ?> params, String key) {
        Object provided = params.get("sign");
        if (provided == null) {
            return false;
        }
        String expected = sign(params, key);
        return constantTimeEquals(expected, provided.toString().toUpperCase());
    }

    private static String sha256Hex(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }
}
