package com.manzhushaka.biz.pii.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付即开票业务配置。
 */
@Component
@ConfigurationProperties(prefix = "pii")
public class PiiProperties {

    /** REAL | MOCK */
    private String mode = "MOCK";

    private Pay pay = new Pay();

    private Invoice invoice = new Invoice();

    private Wechat wechat = new Wechat();

    private Bi bi = new Bi();

    private Order order = new Order();

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Pay getPay() {
        return pay;
    }

    public void setPay(Pay pay) {
        this.pay = pay;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public void setWechat(Wechat wechat) {
        this.wechat = wechat;
    }

    public Bi getBi() {
        return bi;
    }

    public void setBi(Bi bi) {
        this.bi = bi;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public static class Pay {
        private String notifyUrl;

        private String refundNotifyUrl;

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }

        public String getRefundNotifyUrl() {
            return refundNotifyUrl;
        }

        public void setRefundNotifyUrl(String refundNotifyUrl) {
            this.refundNotifyUrl = refundNotifyUrl;
        }
    }

    public static class Invoice {
        private String apiBaseUrl;

        private String notifyUrl;

        private int connectTimeoutMs = 5000;

        private int readTimeoutMs = 15000;

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }

    public static class Wechat {
        private String appId;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }
    }

    public static class Bi {
        private int cacheSeconds = 300;

        public int getCacheSeconds() {
            return cacheSeconds;
        }

        public void setCacheSeconds(int cacheSeconds) {
            this.cacheSeconds = cacheSeconds;
        }
    }

    public static class Order {
        private int expireMinutes = 30;

        public int getExpireMinutes() {
            return expireMinutes;
        }

        public void setExpireMinutes(int expireMinutes) {
            this.expireMinutes = expireMinutes;
        }
    }
}
