package com.manzhushaka.biz.pii.infrastructure.gateway.notify;

import com.alibaba.fastjson2.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyVerifierTest {

    @Test
    void signShouldIgnoreEmptyValuesAndSignThenSortByKey() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");
        params.put("msgType", "issue");
        params.put("amount", "10000");
        params.put("notifyUrl", "");
        params.put("remark", null);
        params.put("sign", "SHOULD_BE_IGNORED");

        String sign = NotifyVerifier.sign(params, "TESTKEY");

        assertThat(sign)
                .hasSize(64)
                .matches("[0-9A-F]{64}")
                .isEqualTo("3DBE4E5962857668009B8CDC7EF7665CB2F5A7004CAA02C77559E3DE582622A7");
    }

    @Test
    void verifyShouldAcceptCorrectSignAndRejectWrongSign() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");
        params.put("msgType", "issue");
        params.put("amount", "10000");
        params.put("sign", NotifyVerifier.sign(params, "K"));

        assertThat(NotifyVerifier.verify(params, "K")).isTrue();

        params.put("sign", "BAD");

        assertThat(NotifyVerifier.verify(params, "K")).isFalse();
    }

    @Test
    void verifyShouldReturnFalseWhenSignMissing() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");

        assertThat(NotifyVerifier.verify(params, "K")).isFalse();
    }

    @Test
    void signShouldBeStableForUnicodeAndSpecialCharacters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("buyerName", "海南椰子店&测试=一号");
        params.put("remark", "换行\n制表\t百分号% 空格");
        params.put("amount", "100.50");

        String sign = NotifyVerifier.sign(params, "密钥K&=");

        assertThat(sign).isEqualTo("B8BEE9043C0B9184D30336E3C578C28E8A5702FF58123FEC5556C4AA5AEEF54A");
    }

    @Test
    void signShouldKeepJsonArrayOrderWhenSigningGoodsDetail() {
        JSONArray goods = new JSONArray();
        goods.add(Map.of("name", "税目A", "amount", 100));
        goods.add(Map.of("name", "税目B", "amount", 200));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("goodsDetail", goods);
        params.put("msgId", "MSG001");

        String firstOrderSign = NotifyVerifier.sign(params, "K");

        JSONArray reversedGoods = new JSONArray();
        reversedGoods.add(Map.of("name", "税目B", "amount", 200));
        reversedGoods.add(Map.of("name", "税目A", "amount", 100));
        params.put("goodsDetail", reversedGoods);

        assertThat(firstOrderSign)
                .isEqualTo("2E59647410B61CC5EE656694C805FA12D37E3C248666CDC53542D805CF666A1E")
                .isNotEqualTo(NotifyVerifier.sign(params, "K"));
    }
}
