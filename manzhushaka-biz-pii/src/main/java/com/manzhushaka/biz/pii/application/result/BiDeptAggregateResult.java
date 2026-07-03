package com.manzhushaka.biz.pii.application.result;

import java.util.ArrayList;
import java.util.List;

public class BiDeptAggregateResult {
    private String level;
    private List<BiDeptAggregateItem> items = new ArrayList<>();

    public BiDeptAggregateResult() {
    }

    public BiDeptAggregateResult(String level, List<BiDeptAggregateItem> items) {
        this.level = level;
        this.items = items;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public List<BiDeptAggregateItem> getItems() { return items; }
    public void setItems(List<BiDeptAggregateItem> items) { this.items = items; }

    public static class BiDeptAggregateItem {
        private Long deptId;
        private String deptName;
        private String regionCode;
        private Long amount;
        private Long count;
        private Long merchantCount;

        public BiDeptAggregateItem() {
        }

        public BiDeptAggregateItem(Long deptId, String deptName, String regionCode, Long amount, Long count, Long merchantCount) {
            this.deptId = deptId;
            this.deptName = deptName;
            this.regionCode = regionCode;
            this.amount = amount;
            this.count = count;
            this.merchantCount = merchantCount;
        }

        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        public String getRegionCode() { return regionCode; }
        public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public Long getMerchantCount() { return merchantCount; }
        public void setMerchantCount(Long merchantCount) { this.merchantCount = merchantCount; }
    }
}
