package com.manzhushaka.common.annotation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.enums.SensitiveType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensitiveFieldSerializationTest {

    /**
     * 验证带脱敏注解的字段在序列化为 JSON 时会按策略输出。
     *
     * @throws Exception 序列化或解析 JSON 失败时抛出
     */
    @Test
    void shouldMaskAnnotatedFieldsDuringJsonSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SensitivePayload payload = new SensitivePayload();
        payload.username = "admin";
        payload.password = "secret";
        payload.mobile = "13812345678";
        payload.bankCard = "6222021234567890123";

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsBytes(payload));

        assertEquals("admin", jsonNode.get("username").asText());
        assertEquals("***", jsonNode.get("password").asText());
        assertEquals("138****5678", jsonNode.get("mobile").asText());
        assertEquals("6222************123", jsonNode.get("bankCard").asText());
    }

    private static class SensitivePayload {
        private String username;

        @SensitiveField(SensitiveType.FULL)
        private String password;

        @SensitiveField(SensitiveType.MOBILE)
        private String mobile;

        @SensitiveField(value = SensitiveType.CUSTOM, prefixKeep = 4, suffixKeep = 3)
        private String bankCard;

        /**
         * 获取用户名。
         *
         * @return 用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 设置用户名。
         *
         * @param username 用户名
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * 获取密码。
         *
         * @return 密码
         */
        public String getPassword() {
            return password;
        }

        /**
         * 设置密码。
         *
         * @param password 密码
         */
        public void setPassword(String password) {
            this.password = password;
        }

        /**
         * 获取手机号。
         *
         * @return 手机号
         */
        public String getMobile() {
            return mobile;
        }

        /**
         * 设置手机号。
         *
         * @param mobile 手机号
         */
        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        /**
         * 获取银行卡号。
         *
         * @return 银行卡号
         */
        public String getBankCard() {
            return bankCard;
        }

        /**
         * 设置银行卡号。
         *
         * @param bankCard 银行卡号
         */
        public void setBankCard(String bankCard) {
            this.bankCard = bankCard;
        }
    }
}
