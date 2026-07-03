package com.manzhushaka.biz.pii.application.result;

import java.util.ArrayList;
import java.util.List;

public class BiDashboardResult {
    private Long totalAmount = 0L;
    private Long totalInvoiceAmount = 0L;
    private Long totalOrderCount = 0L;
    private Long abnormalOrderCount = 0L;
    private List<TrendItem> trend = new ArrayList<>();
    private List<TaxItemRatioItem> taxItemRatio = new ArrayList<>();
    private List<MerchantRankItem> merchantTop10 = new ArrayList<>();
    private List<AbnormalOrderItem> abnormalOrders = new ArrayList<>();

    public Long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Long totalAmount) { this.totalAmount = totalAmount; }
    public Long getTotalInvoiceAmount() { return totalInvoiceAmount; }
    public void setTotalInvoiceAmount(Long totalInvoiceAmount) { this.totalInvoiceAmount = totalInvoiceAmount; }
    public Long getTotalOrderCount() { return totalOrderCount; }
    public void setTotalOrderCount(Long totalOrderCount) { this.totalOrderCount = totalOrderCount; }
    public Long getAbnormalOrderCount() { return abnormalOrderCount; }
    public void setAbnormalOrderCount(Long abnormalOrderCount) { this.abnormalOrderCount = abnormalOrderCount; }
    public List<TrendItem> getTrend() { return trend; }
    public void setTrend(List<TrendItem> trend) { this.trend = trend; }
    public List<TaxItemRatioItem> getTaxItemRatio() { return taxItemRatio; }
    public void setTaxItemRatio(List<TaxItemRatioItem> taxItemRatio) { this.taxItemRatio = taxItemRatio; }
    public List<MerchantRankItem> getMerchantTop10() { return merchantTop10; }
    public void setMerchantTop10(List<MerchantRankItem> merchantTop10) { this.merchantTop10 = merchantTop10; }
    public List<AbnormalOrderItem> getAbnormalOrders() { return abnormalOrders; }
    public void setAbnormalOrders(List<AbnormalOrderItem> abnormalOrders) { this.abnormalOrders = abnormalOrders; }

    public static class TrendItem {
        private String day;
        private Long amount;
        private Long count;

        public TrendItem() {
        }

        public TrendItem(String day, Long amount, Long count) {
            this.day = day;
            this.amount = amount;
            this.count = count;
        }

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }

    public static class TaxItemRatioItem {
        private Long taxItemId;
        private String taxItemName;
        private Long amount;

        public TaxItemRatioItem() {
        }

        public TaxItemRatioItem(Long taxItemId, String taxItemName, Long amount) {
            this.taxItemId = taxItemId;
            this.taxItemName = taxItemName;
            this.amount = amount;
        }

        public Long getTaxItemId() { return taxItemId; }
        public void setTaxItemId(Long taxItemId) { this.taxItemId = taxItemId; }
        public String getTaxItemName() { return taxItemName; }
        public void setTaxItemName(String taxItemName) { this.taxItemName = taxItemName; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
    }

    public static class MerchantRankItem {
        private Long merchantId;
        private String merchantName;
        private Long amount;
        private Long count;

        public MerchantRankItem() {
        }

        public MerchantRankItem(Long merchantId, String merchantName, Long amount, Long count) {
            this.merchantId = merchantId;
            this.merchantName = merchantName;
            this.amount = amount;
            this.count = count;
        }

        public Long getMerchantId() { return merchantId; }
        public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }

    public static class AbnormalOrderItem {
        private Long id;
        private String outTradeNo;
        private String invoiceStatus;
        private String payStatus;
        private Long amount;

        public AbnormalOrderItem() {
        }

        public AbnormalOrderItem(Long id, String outTradeNo, String invoiceStatus, String payStatus, Long amount) {
            this.id = id;
            this.outTradeNo = outTradeNo;
            this.invoiceStatus = invoiceStatus;
            this.payStatus = payStatus;
            this.amount = amount;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOutTradeNo() { return outTradeNo; }
        public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
        public String getInvoiceStatus() { return invoiceStatus; }
        public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }
        public String getPayStatus() { return payStatus; }
        public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
    }
}
