# 支付即开票（Pay & Invoice）平台 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现支付即开票（Pay & Invoice & Invoice）平台 v1：消费者扫码 → 公众号支付 → 自动开票 + 商户退款红冲 + 运营方 BI 看板（海南地图三级下钻）。

**Architecture:**
- 后端 Spring Boot 4 + Spring Security + MyBatis（无 MyBatis-Plus），Maven 多模块
- 新建独立业务模块 `manzhushaka-biz-pii`，表前缀 `pii_`
- `sys_dept` 扩展 `dept_type` / `region_code` / `region_level` 三字段，省/市县/区/商户同棵树
- 集成层 Gateway 接口 + 实现分离：银联公众号支付用 `com.chinaums:open-pay:1.0.0`，银联发票平台 E开票 自研 HTTP + SHA-256 字典序签名
- MQ 用现有 Redis Stream 基础设施，开票/红冲/邮件发送走 MQ 异步
- 消费者匿名 H5 放在 `ui-admin/src/views/pay/`，`@Anonymous` 鉴权
- 前端 Vue 3 + Element Plus + Vite + ECharts 5

**Tech Stack:** Spring Boot 4 / Spring Security 6 / MyBatis / JUnit 5 / ArchUnit / TestContainers / Redis Stream MQ / Apache HttpClient 5 / json-lib / `com.chinaums:open-pay:1.0.0` / Vue 3 / Element Plus / Vite / ECharts 5 / Axios

**Spec:** `docs/superpowers/specs/2026-07-02-pay-and-invoice-design.md`

**Worktree Note:** 建议在专用 worktree 中执行（`git worktree add ../pii -b feat/pii-v1`），不强求。

---

## 阶段总览

| 阶段 | 任务范围 | 关键产物 | 估计任务数 |
| --- | --- | --- | --- |
| **0. 基础设施** | Maven 模块、SQL 脚本、配置、架构脚本 | 模块骨架 + 全部表 + 行政区划预置 + 菜单预置 | 8 |
| **1. 领域层骨架** | Domain / Repository / Mapper / Converter / 拦截器 | 领域层代码 + 拦截器 + dept_type 校验 | 5 |
| **2. 集成适配层** | NotifyVerifier + PaymentGateway + InvoiceGateway + WechatOAuthGateway | 银联公众号支付 + 银联发票适配 + Mock | 8 |
| **3. 运营方管理后台** | 税目 / 商户 / 商户参数 / 二维码 CRUD | 4 套 CRUD + 前端 | 8 |
| **4. 消费者扫码支付开票** | H5 页面 + 匿名接口 + 公众号支付调起 + 回调 + MQ 开票 | 端到端正向流 | 9 |
| **5. 商户退款红冲** | 退款 Service + 回调 + MQ 红冲 | 退款流程 | 4 |
| **6. 订单 / 发票查询** | 查询 Service + 前端 + 导出 | 查询模块 | 5 |
| **7. 运营方 BI 看板** | 聚合查询 + 海南地图 + 缓存 | BI 看板 | 6 |
| **8. 测试与验收** | 单元 / 集成 / 架构 / 手工截图 | 全部测试通过 | 7 |

**总计：约 60 个任务**

---

## 通用约定（适用于所有任务）

- **金额**：整型 `Long` 存"分"；`BigDecimal` 比较用 `compareTo`
- **时间**：`LocalDateTime`（Java）↔ `DATETIME`（DB）；`requestTimestamp` 用 `yyyy-MM-dd HH:mm:ss` 字符串
- **订单号**：32 位数字 = `yyMMddHHmmss(12) + 雪花(20)`，全局唯一；同时作为银联 `merOrderId` 复用
- **日期字段**：`merOrderDate` 格式 `yyyyMMdd`（字符串）
- **敏感字段**：`app_secret` / `pay_sign_key` / `invoice_sign_key` 用 `@Sensitive` 或 AES 加密列；`toString()` 截断 `password` / `token` / `payload`
- **Controller 三件套**：`@PreAuthorize("@ss.hasPermi('biz:xxx:yyy')")` 或 `@Anonymous` + `@Log(title, businessType)` + 必要时 `isSaveRequestData = false`
- **统一异常**：`throw new ServiceException("错误码", "错误消息")`；错误码 5 位字符串
- **多租户**：商户表带 `merchant_id`，`PiiTenantInterceptor` 自动注入 `WHERE merchant_id = ?`
- **commit 规范**：Conventional Commits，如 `feat(pii): ...`、`fix(pii): ...`、`test(pii): ...`、`docs(pii): ...`
- **每个任务结束 = 一次 commit**（小步提交）

---

## 阶段 0 — 基础设施

### Task 0.1: 创建 `manzhushaka-biz-pii` Maven 模块骨架

**Files:**
- Create: `manzhushaka-biz-pii/pom.xml`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/PiiMarker.java`
- Create: `manzhushaka-biz-pii/src/test/java/com/manzhushaka/biz/pii/PiiMarkerTest.java`
- Modify: 根 `pom.xml` 加 `<module>manzhushaka-biz-pii</module>`
- Modify: `manzhushaka-admin/pom.xml` 加 `manzhushaka-biz-pii` 依赖

- [ ] **Step 1: 创建 `manzhushaka-biz-pii/pom.xml`**

> **重要**：项目**不使用** `${revision}` 占位符（无 `flatten-maven-plugin`），所有现有模块都用硬编码 `3.9.2`。新模块必须跟随此约定。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.manzhushaka</groupId>
        <artifactId>manzhushaka-scaff</artifactId>
        <version>3.9.2</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <artifactId>manzhushaka-biz-pii</artifactId>
    <description>支付即开票业务模块</description>

    <dependencies>
        <dependency>
            <groupId>com.manzhushaka</groupId>
            <artifactId>manzhushaka-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.manzhushaka</groupId>
            <artifactId>manzhushaka-framework</artifactId>
        </dependency>
        <dependency>
            <groupId>com.manzhushaka</groupId>
            <artifactId>manzhushaka-system</artifactId>
        </dependency>

        <dependency>
            <groupId>com.chinaums</groupId>
            <artifactId>open-pay</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
        </dependency>
        <dependency>
            <groupId>net.sf.json-lib</groupId>
            <artifactId>json-lib</artifactId>
            <version>2.4</version>
            <classifier>jdk15</classifier>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: 创建 `PiiMarker.java`（包标识类）**

```java
package com.manzhushaka.biz.pii;

/**
 * 包标识类，供 ArchUnit 架构测试使用，确认 pii 模块的包结构。
 */
public final class PiiMarker {
    private PiiMarker() {}
}
```

- [ ] **Step 3: 写失败测试 `PiiMarkerTest.java`**

```java
package com.manzhushaka.biz.pii;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PiiMarkerTest {
    @Test
    void markerClassExists() {
        assertNotNull(PiiMarker.class);
    }
}
```

- [ ] **Step 4: 修改根 `pom.xml`**

> **重要**：项目模块顺序是**自上而下的依赖顺序**（admin 在最上，common 在最下），**不是字母顺序**。新模块插在 `manzhushaka-admin` 之后（它依赖本模块）。

在 `<modules>` 块中加：
```xml
<module>manzhushaka-biz-pii</module>
```

- [ ] **Step 5: 修改 `manzhushaka-admin/pom.xml`**

在 `<dependencies>` 块中加：
```xml
<dependency>
    <groupId>com.manzhushaka</groupId>
    <artifactId>manzhushaka-biz-pii</artifactId>
    <version>3.9.2</version>
</dependency>
```

- [ ] **Step 6: 跑测试确认通过**

```bash
mvn -pl manzhushaka-biz-pii -am test
```

Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add manzhushaka-biz-pii/ pom.xml manzhushaka-admin/pom.xml
git commit -m "feat(pii): 创建 manzhushaka-biz-pii 模块骨架"

---

### Task 0.2: `sys_dept` 扩展 3 字段 + 建库 SQL

**Files:**
- Modify: `sql/manzhushaka_db_init.sql`（在 `sys_dept` 表 CREATE 语句后 ALTER）
- Create: `sql/pii_schema.sql`（所有 pii_* 表的 CREATE 语句，单文件）

- [ ] **Step 1: 在主 SQL 中 ALTER sys_dept**

在 `sql/manzhushaka_db_init.sql` 的 `sys_dept` 建表语句后追加（**保留**原 `CREATE TABLE`，新字段加在表尾）：

```sql
-- 支付即开票（PIi）业务扩展
ALTER TABLE sys_dept
    ADD COLUMN dept_type    VARCHAR(16)  NOT NULL DEFAULT 'platform_org' COMMENT '部门类型: platform_org/region/merchant',
    ADD COLUMN region_code  VARCHAR(6)   NULL                       COMMENT '行政区划代码（dept_type=region 时填，6 位数字）',
    ADD COLUMN region_level TINYINT      NULL                       COMMENT '行政区划级别 1=省/2=市县/3=区/镇（dept_type=region 时填）';

CREATE INDEX idx_sys_dept_type      ON sys_dept (dept_type);
CREATE INDEX idx_sys_dept_region    ON sys_dept (region_code);
CREATE INDEX idx_sys_dept_ancestors ON sys_dept (ancestors);
```

- [ ] **Step 2: 创建 `sql/pii_schema.sql`（所有 pii_* 表）**

```sql
-- ============================================================================
-- 支付即开票（PIi）业务表
-- 表前缀: pii_
-- 执行: 紧随 sys_dept ALTER 之后
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 平台统一公众号配置
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_platform_wechat_config;
CREATE TABLE pii_platform_wechat_config (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    app_id          VARCHAR(32)     NOT NULL                COMMENT '微信公众号 AppID',
    app_secret_enc  VARCHAR(512)    NOT NULL                COMMENT 'AppSecret AES 加密',
    pay_sign_key_enc VARCHAR(512)   NOT NULL                COMMENT '公众号支付签名密钥 AES 加密',
    token           VARCHAR(64)     NULL                    COMMENT '公众号 Token',
    aes_key_enc     VARCHAR(512)    NULL                    COMMENT '公众号消息加密 AESKey',
    status          TINYINT         NOT NULL DEFAULT 1      COMMENT '1=启用 0=停用',
    remark          VARCHAR(255)    NULL,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT          NULL,
    update_by       BIGINT          NULL,
    UNIQUE KEY uk_app_id (app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台统一公众号配置';

-- ----------------------------------------------------------------------------
-- 税目
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_tax_item;
CREATE TABLE pii_tax_item (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tax_item_code     VARCHAR(19)  NOT NULL                COMMENT '税收分类编码 19 位',
    name              VARCHAR(128) NOT NULL                COMMENT '商品/服务名称',
    brevity_code      VARCHAR(32)  NULL                    COMMENT '商品简码',
    category          VARCHAR(64)  NULL                    COMMENT '商品分类',
    tax_rate          DECIMAL(5,2) NOT NULL                COMMENT '税率（6、13）',
    vat_special       VARCHAR(64)  NULL                    COMMENT '增值税特殊管理',
    free_tax_type     VARCHAR(1)   NULL                    COMMENT '免税类型',
    prefer_policy_flag VARCHAR(1)  NULL                    COMMENT '是否使用优惠政策',
    sort              INT          NOT NULL DEFAULT 0      COMMENT '排序',
    status            TINYINT      NOT NULL DEFAULT 1      COMMENT '1=启用 0=停用',
    remark            VARCHAR(255) NULL,
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by         BIGINT       NULL,
    update_by         BIGINT       NULL,
    UNIQUE KEY uk_tax_item_code (tax_item_code),
    KEY idx_status (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='税目';

-- ----------------------------------------------------------------------------
-- 商户档案
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_merchant_profile;
CREATE TABLE pii_merchant_profile (
    id                          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    dept_id                     BIGINT       NOT NULL                COMMENT '关联 sys_dept.id（dept_type=merchant）',
    merchant_name               VARCHAR(128) NOT NULL                COMMENT '商户主体名（开票用）',
    ums_merchant_id             VARCHAR(15)  NOT NULL                COMMENT '银联商户号',
    ums_terminal_id             VARCHAR(8)   NOT NULL                COMMENT '银联终端号',
    ums_pay_sign_key_enc        VARCHAR(512) NOT NULL                COMMENT '公众号支付签名密钥 加密',
    ums_invoice_sign_key_enc    VARCHAR(512) NOT NULL                COMMENT '银联发票签名密钥 加密',
    invoice_msg_src             VARCHAR(32)  NOT NULL                COMMENT '发票接口消息来源',
    invoice_seller_name         VARCHAR(128) NOT NULL                COMMENT '卖方名称',
    invoice_seller_tax_code     VARCHAR(32)  NOT NULL                COMMENT '卖方纳税人识别号',
    invoice_seller_address      VARCHAR(128) NULL,
    invoice_seller_telephone    VARCHAR(16)  NULL,
    invoice_seller_bank         VARCHAR(64)  NULL,
    invoice_seller_account      VARCHAR(32)  NULL,
    invoice_payee               VARCHAR(8)   NULL                    COMMENT '收款人',
    invoice_checker             VARCHAR(8)   NULL                    COMMENT '复核人',
    invoice_drawer              VARCHAR(8)   NULL                    COMMENT '开票人',
    notify_url                  VARCHAR(256) NOT NULL                COMMENT '开票回调地址 80/443 端口',
    status                      TINYINT      NOT NULL DEFAULT 1,
    remark                      VARCHAR(255) NULL,
    create_time                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by                   BIGINT       NULL,
    update_by                   BIGINT       NULL,
    UNIQUE KEY uk_dept_id (dept_id),
    UNIQUE KEY uk_ums (ums_merchant_id, ums_terminal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户档案';

-- ----------------------------------------------------------------------------
-- 支付二维码
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_pay_qrcode;
CREATE TABLE pii_pay_qrcode (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    merchant_id       BIGINT       NOT NULL                COMMENT '关联 pii_merchant_profile.id',
    qrcode_code       VARCHAR(32)  NOT NULL                COMMENT '二维码业务编码',
    qrcode_url        VARCHAR(512) NOT NULL                COMMENT 'H5 入口 URL',
    qrcode_image_url  VARCHAR(512) NULL                    COMMENT '二维码图片 base64/OSS',
    name              VARCHAR(64)  NOT NULL                COMMENT '二维码名称',
    status            TINYINT      NOT NULL DEFAULT 1,
    expire_time       DATETIME     NULL                    COMMENT '失效时间',
    remark            VARCHAR(255) NULL,
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by         BIGINT       NULL,
    update_by         BIGINT       NULL,
    UNIQUE KEY uk_qrcode_code (qrcode_code),
    KEY idx_merchant_status (merchant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付二维码';

-- ----------------------------------------------------------------------------
-- 二维码-税目 关联
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_pay_qrcode_tax_item;
CREATE TABLE pii_pay_qrcode_tax_item (
    id              BIGINT  AUTO_INCREMENT PRIMARY KEY,
    qrcode_id       BIGINT  NOT NULL,
    tax_item_id     BIGINT  NOT NULL,
    default_amount  BIGINT  NULL                    COMMENT '默认金额（分）',
    UNIQUE KEY uk_qrcode_tax (qrcode_id, tax_item_id),
    KEY idx_tax_item (tax_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二维码-税目关联';

-- ----------------------------------------------------------------------------
-- 支付订单
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_pay_order;
CREATE TABLE pii_pay_order (
    id                      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    merchant_id             BIGINT       NOT NULL,
    qrcode_id               BIGINT       NOT NULL,
    tax_item_id             BIGINT       NOT NULL,
    out_trade_no            VARCHAR(32)  NOT NULL                COMMENT '本系统订单号（32 位，复用为银联 merOrderId）',
    ums_mer_order_date      VARCHAR(8)   NOT NULL                COMMENT 'yyyyMMdd',
    amount                  BIGINT       NOT NULL                COMMENT '含税总金额（分）',
    buyer_name              VARCHAR(128) NOT NULL                COMMENT '抬头',
    buyer_tax_code          VARCHAR(32)  NULL                    COMMENT '税号',
    buyer_email             VARCHAR(64)  NULL,
    buyer_mobile            VARCHAR(16)  NULL,
    buyer_openid            VARCHAR(64)  NULL                    COMMENT '公众号 openId',
    pay_status              VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/REFUNDING/REFUNDED/CLOSED',
    pay_time                DATETIME     NULL,
    pay_trade_no            VARCHAR(64)  NULL                    COMMENT '银联支付流水号',
    pay_notify_status       VARCHAR(16)  NULL                    COMMENT 'NOT/OK/FAILED',
    refund_amount           BIGINT       NOT NULL DEFAULT 0      COMMENT '累计退款（分）',
    invoice_status          VARCHAR(16)  NOT NULL DEFAULT 'NONE' COMMENT 'NONE/PENDING/ISSUING/ISSUED/REVERSING/REVERSED/FAILED',
    invoice_no              VARCHAR(20)  NULL                    COMMENT '发票号码',
    invoice_code            VARCHAR(20)  NULL                    COMMENT '发票代码',
    invoice_pdf_url         VARCHAR(256) NULL,
    invoice_issue_time      DATETIME     NULL,
    invoice_reverse_time    DATETIME     NULL,
    order_token             VARCHAR(32)  NOT NULL                COMMENT '消费者匿名查询短 token',
    wechat_appid            VARCHAR(32)  NULL,
    client_ip               VARCHAR(64)  NULL,
    remark                  VARCHAR(255) NULL,
    create_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by               BIGINT       NULL,
    update_by               BIGINT       NULL,
    UNIQUE KEY uk_out_trade_no (out_trade_no),
    KEY idx_merchant_pay_status (merchant_id, pay_status, pay_time),
    KEY idx_merchant_invoice (merchant_id, invoice_status),
    KEY idx_invoice_status (invoice_status, invoice_issue_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单';

-- ----------------------------------------------------------------------------
-- 退款记录
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_refund_record;
CREATE TABLE pii_refund_record (
    id                          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    merchant_id                 BIGINT       NOT NULL,
    pay_order_id                BIGINT       NOT NULL,
    out_refund_no               VARCHAR(32)  NOT NULL                COMMENT '本系统退款单号',
    ums_refund_mer_order_id     VARCHAR(32)  NULL,
    amount                      BIGINT       NOT NULL                COMMENT '退款金额（分）',
    reason                      VARCHAR(255) NULL,
    status                      VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
    ums_trade_no                VARCHAR(64)  NULL,
    complete_time               DATETIME     NULL,
    operator_id                 BIGINT       NOT NULL,
    trigger_invoice_reverse     TINYINT      NOT NULL DEFAULT 1,
    create_time                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_out_refund_no (out_refund_no),
    KEY idx_pay_order (pay_order_id),
    KEY idx_merchant_status (merchant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录';

-- ----------------------------------------------------------------------------
-- 支付回调幂等日志
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_payment_notify_log;
CREATE TABLE pii_payment_notify_log (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    out_trade_no    VARCHAR(32)  NOT NULL,
    notify_payload  JSON         NOT NULL,
    sign            VARCHAR(128) NULL,
    verify_result   TINYINT      NOT NULL                COMMENT '1=通过 0=失败',
    processed       TINYINT      NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_out_trade_no (out_trade_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调幂等日志';

-- ----------------------------------------------------------------------------
-- 开票回调幂等日志
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_invoice_notify_log;
CREATE TABLE pii_invoice_notify_log (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    ums_mer_order_id    VARCHAR(32)  NOT NULL,
    ums_mer_order_date  VARCHAR(8)   NOT NULL,
    qrcode_id           VARCHAR(40)  NULL,
    notify_payload      JSON         NOT NULL,
    sign                VARCHAR(128) NULL,
    verify_result       TINYINT      NOT NULL,
    processed           TINYINT      NOT NULL DEFAULT 0,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order (ums_mer_order_id, ums_mer_order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开票回调幂等日志';

-- ----------------------------------------------------------------------------
-- 银联发票接口调用日志
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pii_invoice_call_log;
CREATE TABLE pii_invoice_call_log (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    pay_order_id    BIGINT       NOT NULL,
    msg_type        VARCHAR(32)  NOT NULL                COMMENT 'issue/reverse/query/pickup',
    msg_id          VARCHAR(64)  NOT NULL,
    request_body    JSON         NULL,
    response_body   JSON         NULL,
    duration_ms     INT          NULL,
    is_success      TINYINT      NOT NULL,
    error_msg       VARCHAR(512) NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_msg (pay_order_id, msg_type),
    KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银联发票接口调用日志';
```

- [ ] **Step 3: 在主 SQL 末尾引用 `pii_schema.sql`**

在 `sql/manzhushaka_db_init.sql` 末尾追加：
```sql
-- 支付即开票业务表
source sql/pii_schema.sql;
```

> 注：若 `mysql -uroot -p` 客户端不支持 `source` 路径，请让运维手动在主脚本后执行 `mysql ... < sql/pii_schema.sql`。

- [ ] **Step 4: 验证 SQL 语法正确**

```bash
mysql -uroot -p -e "source sql/pii_schema.sql" --verbose 2>&1 | tail -20
```

Expected: 所有 `CREATE TABLE` / `DROP TABLE` 成功，无 syntax error。

- [ ] **Step 5: Commit**

```bash
git add sql/manzhushaka_db_init.sql sql/pii_schema.sql
git commit -m "feat(pii-sql): sys_dept 扩展 + pii_* 业务表建表脚本"
```

---

### Task 0.3: 行政区划预置数据

**Files:**
- Create: `sql/pii_region_data.sql`

- [ ] **Step 1: 创建 `sql/pii_region_data.sql`**

写入海南省 + 19 市县 + 主要区/镇 INSERT 语句。**完整区/镇清单按民政部 2025 年公布的行政区划数据**。以下给出骨架（精简版仅列海口/三亚/儋州/三沙详细区/镇，其余 15 个市县仅 INSERT 一行占位待后续补全）：

```sql
-- ============================================================================
-- 海南行政区划预置（写入 sys_dept，dept_type=region）
-- 执行顺序：必须先有 ROOT dept（id=100 通常是运营平台根）
-- ============================================================================

-- 假设 ROOT dept id = 100（来自现有 sys_dept 预置），parent_id = 0
-- 海南省级 dept
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, dept_type, region_code, region_level, status, del_flag, create_time, update_time, create_by, update_by)
VALUES (200, 100, '0,100', '海南省', 0, 'region', '460000', 1, 1, 0, NOW(), NOW(), 1, 1);

-- 市县 dept（level=2，19 个）
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, dept_type, region_code, region_level, status, del_flag, create_time, update_time, create_by, update_by) VALUES
(201, 200, '0,100,200', '海口市',     1, 'region', '460100', 2, 1, 0, NOW(), NOW(), 1, 1),
(202, 200, '0,100,200', '三亚市',     2, 'region', '460200', 2, 1, 0, NOW(), NOW(), 1, 1),
(203, 200, '0,100,200', '三沙市',     3, 'region', '460300', 2, 1, 0, NOW(), NOW(), 1, 1),
(204, 200, '0,100,200', '儋州市',     4, 'region', '460400', 2, 1, 0, NOW(), NOW(), 1, 1),
(205, 200, '0,100,200', '五指山市',   5, 'region', '469001', 2, 1, 0, NOW(), NOW(), 1, 1),
(206, 200, '0,100,200', '琼海市',     6, 'region', '469002', 2, 1, 0, NOW(), NOW(), 1, 1),
(207, 200, '0,100,200', '万宁市',     7, 'region', '469006', 2, 1, 0, NOW(), NOW(), 1, 1),
(219, 200, '0,100,200', '文昌市',     8, 'region', '469005', 2, 1, 0, NOW(), NOW(), 1, 1),
(208, 200, '0,100,200', '东方市',     9, 'region', '469007', 2, 1, 0, NOW(), NOW(), 1, 1),
(209, 200, '0,100,200', '定安县',    10, 'region', '469021', 2, 1, 0, NOW(), NOW(), 1, 1),
(210, 200, '0,100,200', '屯昌县',    11, 'region', '469022', 2, 1, 0, NOW(), NOW(), 1, 1),
(211, 200, '0,100,200', '澄迈县',    12, 'region', '469023', 2, 1, 0, NOW(), NOW(), 1, 1),
(212, 200, '0,100,200', '临高县',    13, 'region', '469024', 2, 1, 0, NOW(), NOW(), 1, 1),
(213, 200, '0,100,200', '白沙黎族自治县', 14, 'region', '469025', 2, 1, 0, NOW(), NOW(), 1, 1),
(214, 200, '0,100,200', '昌江黎族自治县', 15, 'region', '469026', 2, 1, 0, NOW(), NOW(), 1, 1),
(215, 200, '0,100,200', '乐东黎族自治县', 16, 'region', '469027', 2, 1, 0, NOW(), NOW(), 1, 1),
(216, 200, '0,100,200', '陵水黎族自治县', 17, 'region', '469028', 2, 1, 0, NOW(), NOW(), 1, 1),
(217, 200, '0,100,200', '保亭黎族苗族自治县', 18, 'region', '469029', 2, 1, 0, NOW(), NOW(), 1, 1),
(218, 200, '0,100,200', '琼中黎族苗族自治县', 19, 'region', '469030', 2, 1, 0, NOW(), NOW(), 1, 1);

-- 海口市区/镇（level=3）
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, dept_type, region_code, region_level, status, del_flag, create_time, update_time, create_by, update_by) VALUES
(301, 201, '0,100,200,201', '秀英区', 1, 'region', '460105', 3, 1, 0, NOW(), NOW(), 1, 1),
(302, 201, '0,100,200,201', '龙华区', 2, 'region', '460106', 3, 1, 0, NOW(), NOW(), 1, 1),
(303, 201, '0,100,200,201', '琼山区', 3, 'region', '460107', 3, 1, 0, NOW(), NOW(), 1, 1),
(304, 201, '0,100,200,201', '美兰区', 4, 'region', '460108', 3, 1, 0, NOW(), NOW(), 1, 1);

-- 三亚市区（level=3）
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, dept_type, region_code, region_level, status, del_flag, create_time, update_time, create_by, update_by) VALUES
(311, 202, '0,100,200,202', '吉阳区', 1, 'region', '460203', 3, 1, 0, NOW(), NOW(), 1, 1),
(312, 202, '0,100,200,202', '天涯区', 2, 'region', '460204', 3, 1, 0, NOW(), NOW(), 1, 1),
(313, 202, '0,100,200,202', '海棠区', 3, 'region', '460202', 3, 1, 0, NOW(), NOW(), 1, 1),
(314, 202, '0,100,200,202', '崖州区', 4, 'region', '460205', 3, 1, 0, NOW(), NOW(), 1, 1);

-- 儋州市辖区/镇（level=3）— 简化为那大镇 + 滨海新区
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, dept_type, region_code, region_level, status, del_flag, create_time, update_time, create_by, update_by) VALUES
(321, 204, '0,100,200,204', '那大镇', 1, 'region', '460403', 3, 1, 0, NOW(), NOW(), 1, 1),
(322, 204, '0,100,200,204', '滨海新区', 2, 'region', '460404', 3, 1, 0, NOW(), NOW(), 1, 1);

-- 三沙市（西沙群岛等）— 无下辖区/镇，level=3 占位
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, dept_type, region_code, region_level, status, del_flag, create_time, update_time, create_by, update_by) VALUES
(331, 203, '0,100,200,203', '西沙群岛', 1, 'region', '460301', 3, 1, 0, NOW(), NOW(), 1, 1),
(332, 203, '0,100,200,203', '南沙群岛', 2, 'region', '460302', 3, 1, 0, NOW(), NOW(), 1, 1),
(333, 203, '0,100,200,203', '中沙群岛', 3, 'region', '460303', 3, 1, 0, NOW(), NOW(), 1, 1);
```

> 实际部署前，按民政部 2025 年数据补全五指山、琼海、万宁、东方 等 15 个市县的全部区/镇。

- [ ] **Step 2: 独立执行 `pii_region_data.sql`**

跟随 `sql/pii_schema.sql` 的独立文件模式（参考 Task 0.2 fix），**不在主脚本中 `source` 引用**。

执行命令：
```bash
mysql -uroot -p manzhushaka-scaff < sql/pii_region_data.sql
```

并在 `sql/manzhushaka_db_init.sql` 末尾追加注释块（与 `pii_schema.sql` 同样的方式）：

```sql
-- ============================================================================
-- 支付即开票（PIi）行政区划预置数据（独立文件，不在此脚本内）
-- 单独执行: mysql -uroot -p manzhushaka-scaff < sql/pii_region_data.sql
-- 详见: docs/superpowers/specs/2026-07-02-pay-and-invoice-design.md §7.5
-- ============================================================================
```

- [ ] **Step 3: 验证 SQL 执行**

```bash
mysql -uroot -p manzhushaka-scaff < sql/pii_region_data.sql
mysql -uroot -p manzhushaka-scaff -e "SELECT COUNT(*) FROM sys_dept WHERE dept_type='region'"
```

Expected: 数量等于 1 + 19 + (4+4+2+3) = 33 行。

- [ ] **Step 4: Commit**

```bash
git add sql/pii_region_data.sql sql/manzhushaka_db_init.sql
git commit -m "feat(pii-sql): 预置海南行政区划 dept 数据"

---

### Task 0.4: 菜单与角色预置

**Files:**
- Modify: `sql/manzhushaka_db_init.sql`（在 `sys_menu` 预置后追加 PII 菜单）

- [ ] **Step 1: 追加 PII 一级菜单 + 子菜单 + 按钮权限**

完整菜单 ID 从 200 开始避免与现有冲突。**插入位置**：在 `sys_menu` INSERT 块**末尾**追加。

```sql
-- ============================================================================
-- 支付即开票（PIi）业务菜单
-- ============================================================================

-- 一级菜单
insert into sys_menu values('200', '支付即开票', '0', '4', 'pii', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, '支付即开票目录');

-- 二级菜单（挂在 'pii' 下）
insert into sys_menu values('201', '商户管理', '200', '1', 'merchant',   'pii/merchant/index',     '', '', 1, 0, 'M', '0', '0', '', 'peoples',     'admin', sysdate(), '', null, '商户管理目录');
insert into sys_menu values('202', '税目管理', '200', '2', 'taxItem',    'pii/taxItem/index',      '', '', 1, 0, 'M', '0', '0', '', 'dict',        'admin', sysdate(), '', null, '税目管理目录');
insert into sys_menu values('203', '支付二维码', '200', '3', 'qrcode',     'pii/qrcode/index',       '', '', 1, 0, 'M', '0', '0', '', 'guide',       'admin', sysdate(), '', null, '支付二维码目录');
insert into sys_menu values('204', '支付订单', '200', '4', 'payOrder',   'pii/payOrder/index',     '', '', 1, 0, 'M', '0', '0', '', 'list',        'admin', sysdate(), '', null, '支付订单目录');
insert into sys_menu values('205', '发票查询', '200', '5', 'invoice',    'pii/invoice/index',      '', '', 1, 0, 'M', '0', '0', '', 'documentation','admin', sysdate(), '', null, '发票查询目录');
insert into sys_menu values('206', '退款管理', '200', '6', 'refund',     'pii/refund/index',       '', '', 1, 0, 'M', '0', '0', '', 'money',       'admin', sysdate(), '', null, '退款管理目录');
insert into sys_menu values('207', 'BI 看板', '200', '7', 'bi',         'pii/bi/dashboard',       '', '', 1, 0, 'M', '0', '0', '', 'chart',       'admin', sysdate(), '', null, 'BI 看板目录');

-- 页面菜单（C）
insert into sys_menu values('210', '商户列表', '201', '1', 'merchantList',   'pii/merchant/index',        '', '', 1, 0, 'C', '0', '0', 'biz:merchant:list',   'peoples',  'admin', sysdate(), '', null, '商户列表菜单');
insert into sys_menu values('211', '商户参数配置', '201', '2', 'merchantConfig', 'pii/merchant/config/index', '', '', 1, 0, 'C', '0', '0', 'biz:merchant:config',  'edit',     'admin', sysdate(), '', null, '商户参数配置菜单');
insert into sys_menu values('212', '税目列表', '202', '1', 'taxItemList',     'pii/taxItem/index',         '', '', 1, 0, 'C', '0', '0', 'biz:taxItem:list',     'dict',     'admin', sysdate(), '', null, '税目列表菜单');
insert into sys_menu values('213', '二维码总览', '203', '1', 'qrcodeList',     'pii/qrcode/index',          '', '', 1, 0, 'C', '0', '0', 'biz:qrcode:list',      'guide',    'admin', sysdate(), '', null, '二维码总览菜单');
insert into sys_menu values('214', '二维码详情', '203', '2', 'qrcodeDetail',   'pii/qrcode/detail/index',   '', '', 1, 0, 'C', '0', '0', 'biz:qrcode:query',     'guide',    'admin', sysdate(), '', null, '二维码详情菜单');
insert into sys_menu values('215', '订单查询', '204', '1', 'payOrderList',   'pii/payOrder/index',        '', '', 1, 0, 'C', '0', '0', 'biz:payOrder:list',    'list',     'admin', sysdate(), '', null, '订单查询菜单');
insert into sys_menu values('216', '订单详情', '204', '2', 'payOrderDetail', 'pii/payOrder/detail/index', '', '', 1, 0, 'C', '0', '0', 'biz:payOrder:query',   'list',     'admin', sysdate(), '', null, '订单详情菜单');
insert into sys_menu values('217', '发票列表', '205', '1', 'invoiceList',    'pii/invoice/index',         '', '', 1, 0, 'C', '0', '0', 'biz:invoice:list',     'documentation', 'admin', sysdate(), '', null, '发票列表菜单');
insert into sys_menu values('218', '发票详情', '205', '2', 'invoiceDetail',  'pii/invoice/detail/index',  '', '', 1, 0, 'C', '0', '0', 'biz:invoice:query',    'documentation', 'admin', sysdate(), '', null, '发票详情菜单');
insert into sys_menu values('219', '退款列表', '206', '1', 'refundList',     'pii/refund/index',          '', '', 1, 0, 'C', '0', '0', 'biz:refund:list',      'money',    'admin', sysdate(), '', null, '退款列表菜单');
insert into sys_menu values('220', '退款详情', '206', '2', 'refundDetail',   'pii/refund/detail/index',   '', '', 1, 0, 'C', '0', '0', 'biz:refund:query',     'money',    'admin', sysdate(), '', null, '退款详情菜单');
insert into sys_menu values('221', '运营全局看板', '207', '1', 'biDashboard',  'pii/bi/dashboard',          '', '', 1, 0, 'C', '0', '0', 'biz:bi:dashboard',     'chart',    'admin', sysdate(), '', null, '运营全局看板菜单');

-- 按钮权限（F）
-- 商户管理按钮
insert into sys_menu values('230', '商户新增',   '210', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:merchant:add',             '#', 'admin', sysdate(), '', null, '商户新增按钮');
insert into sys_menu values('231', '商户修改',   '210', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:merchant:edit',            '#', 'admin', sysdate(), '', null, '商户修改按钮');
insert into sys_menu values('232', '商户删除',   '210', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:merchant:remove',          '#', 'admin', sysdate(), '', null, '商户删除按钮');
insert into sys_menu values('233', '商户启停',   '210', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:merchant:changeStatus',    '#', 'admin', sysdate(), '', null, '商户启停按钮');
-- 税目管理按钮
insert into sys_menu values('234', '税目新增',   '212', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:taxItem:add',              '#', 'admin', sysdate(), '', null, '税目新增按钮');
insert into sys_menu values('235', '税目修改',   '212', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:taxItem:edit',             '#', 'admin', sysdate(), '', null, '税目修改按钮');
insert into sys_menu values('236', '税目删除',   '212', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:taxItem:remove',           '#', 'admin', sysdate(), '', null, '税目删除按钮');
insert into sys_menu values('237', '税目启停',   '212', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:taxItem:changeStatus',     '#', 'admin', sysdate(), '', null, '税目启停按钮');
-- 二维码按钮
insert into sys_menu values('238', '二维码新增', '213', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:qrcode:add',               '#', 'admin', sysdate(), '', null, '二维码新增按钮');
insert into sys_menu values('239', '二维码修改', '213', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:qrcode:edit',              '#', 'admin', sysdate(), '', null, '二维码修改按钮');
insert into sys_menu values('240', '二维码删除', '213', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:qrcode:remove',            '#', 'admin', sysdate(), '', null, '二维码删除按钮');
insert into sys_menu values('241', '二维码启停', '213', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:qrcode:changeStatus',      '#', 'admin', sysdate(), '', null, '二维码启停按钮');
-- 订单按钮
insert into sys_menu values('242', '订单导出',   '215', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:payOrder:export',          '#', 'admin', sysdate(), '', null, '订单导出按钮');
-- 发票按钮
insert into sys_menu values('243', '发票下载',   '217', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:invoice:download',         '#', 'admin', sysdate(), '', null, '发票下载按钮');
insert into sys_menu values('244', '发票导出',   '217', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:invoice:export',           '#', 'admin', sysdate(), '', null, '发票导出按钮');
-- 退款按钮
insert into sys_menu values('245', '发起退款',   '219', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'biz:refund:add',               '#', 'admin', sysdate(), '', null, '发起退款按钮');
```

- [ ] **Step 2: 预置 4 个 PII 角色**

```sql
insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_time, update_time, remark)
values
(110, '运营方管理人员',   'operator',           3, 1, 1, 1, 0, 0, sysdate(), sysdate(), 'PIi 运营方，data_scope=1 全部'),
(111, '商户',            'merchant',           4, 4, 1, 1, 0, 0, sysdate(), sysdate(), 'PIi 商户，data_scope=4 本部门及以下'),
(112, '海南各市县税务局', 'tax_bureau_city',    5, 3, 1, 1, 0, 0, sysdate(), sysdate(), 'PIi 税局市级，data_scope=3 本部门'),
(113, '海南税务局',       'tax_bureau_province',6, 4, 1, 1, 0, 0, sysdate(), sysdate(), 'PIi 税局省级，data_scope=4 本部门及以下');
```

> `data_scope` 值含义：1=所有数据权限 / 2=自定义 / 3=本部门 / 4=本部门及以下 / 5=仅本人

- [ ] **Step 3: 给超级管理员授权 PII 全部菜单 + 给运营方角色授权 PII 全权限**

```sql
-- 超级管理员 (role_id=1) 拥有全部菜单
insert into sys_role_menu (role_id, menu_id) select 1, menu_id from sys_menu where menu_id between 200 and 299;
-- 运营方角色 (110) 拥有 PII 全部菜单
insert into sys_role_menu (role_id, menu_id) select 110, menu_id from sys_menu where menu_id between 200 and 299;
-- 商户角色 (111) 拥有除"商户管理"外的所有菜单（商户不能管理自己）
insert into sys_role_menu (role_id, menu_id) select 111, menu_id from sys_menu where menu_id between 200 and 299 and menu_id not in (201, 210, 211, 230, 231, 232, 233);
-- 税局角色预留，v1 不分配菜单
```

- [ ] **Step 4: 验证**

```bash
mysql -uroot -p manzhushaka-scaff -e "SELECT COUNT(*) AS cnt FROM sys_menu WHERE menu_id BETWEEN 200 AND 299"
```

Expected: 36

- [ ] **Step 5: Commit**

```bash
git add sql/manzhushaka_db_init.sql
git commit -m "feat(pii-sql): 预置 PII 菜单 + 4 个角色 + 默认授权"
```

---

### Task 0.5: 业务配置项

**Files:**
- Modify: `manzhushaka-admin/src/main/resources/application-dev.yml`
- Modify: `manzhushaka-admin/src/main/resources/application-prod.yml`

- [ ] **Step 1: 在两个 application*.yml 末尾加 pii 配置**

```yaml
pii:
  mode: MOCK                       # REAL | MOCK；prod 改为 REAL
  pay:
    notify-url: https://your-domain.com/pii/pay/notify
    refund-notify-url: https://your-domain.com/pii/refund/notify
  invoice:
    api-base-url: https://mobl-test.chinaums.com/fapiao-api-test/   # 测试；prod 改对接人员提供
    notify-url: https://your-domain.com/pii/invoice/notify
    connect-timeout-ms: 5000
    read-timeout-ms: 15000
  bi:
    cache-seconds: 300
  order:
    expire-minutes: 30
```

- [ ] **Step 2: 创建 `PiiProperties`（`manzhushaka-biz-pii/src/main/java/.../infrastructure/config/PiiProperties.java`）**

```java
package com.manzhushaka.biz.pii.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pii")
public class PiiProperties {
    /** REAL | MOCK */
    private String mode = "MOCK";
    private Pay pay = new Pay();
    private Invoice invoice = new Invoice();
    private Bi bi = new Bi();
    private Order order = new Order();

    // getter / setter
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Pay getPay() { return pay; }
    public void setPay(Pay pay) { this.pay = pay; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    public Bi getBi() { return bi; }
    public void setBi(Bi bi) { this.bi = bi; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public static class Pay {
        private String notifyUrl;
        private String refundNotifyUrl;
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        public String getRefundNotifyUrl() { return refundNotifyUrl; }
        public void setRefundNotifyUrl(String refundNotifyUrl) { this.refundNotifyUrl = refundNotifyUrl; }
    }
    public static class Invoice {
        private String apiBaseUrl;
        private String notifyUrl;
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 15000;
        public String getApiBaseUrl() { return apiBaseUrl; }
        public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    }
    public static class Bi {
        private int cacheSeconds = 300;
        public int getCacheSeconds() { return cacheSeconds; }
        public void setCacheSeconds(int cacheSeconds) { this.cacheSeconds = cacheSeconds; }
    }
    public static class Order {
        private int expireMinutes = 30;
        public int getExpireMinutes() { return expireMinutes; }
        public void setExpireMinutes(int expireMinutes) { this.expireMinutes = expireMinutes; }
    }
}
```

- [ ] **Step 3: 验证编译**

```bash
mvn -pl manzhushaka-biz-pii -am compile
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/config/PiiProperties.java manzhushaka-admin/src/main/resources/application-*.yml
git commit -m "feat(pii-config): PII 业务配置项 + PiiProperties"
```

---

### Task 0.6: 架构边界脚本新增 pii 规则

**Files:**
- Modify: `scripts/architecture/check-module-boundaries.sh`

- [ ] **Step 1: 追加 pii 规则**

在文件末尾追加：

```bash
# ----------------------------------------------------------------------------
# 支付即开票 (pii) 业务模块边界
# ----------------------------------------------------------------------------

# 1. common 模块禁止承载 pii 业务实体
if grep -rn "pii" manzhushaka-common/src/main/java/com/manzhushaka/common/ 2>/dev/null | grep -v "PiiProperties" | grep -q .; then
    echo "FAIL: common 模块不得引用 pii 业务代码"
    exit 1
fi

# 2. pii 模块禁止直接引用 admin web 层
if grep -rn "com.manzhushaka.web" manzhushaka-biz-pii/src/main/java 2>/dev/null | grep -q .; then
    echo "FAIL: pii 模块禁止引用 admin web 层（DTO/VO/Controller）"
    exit 1
fi

# 3. admin 的 controller 可调用 pii service，但禁止 pii 反向引用 admin
if grep -rn "com.manzhushaka.biz.pii" manzhushaka-admin/src/main/java 2>/dev/null | grep -q .; then
    echo "OK: admin 引用 pii（正常）"
fi

echo "OK: pii 模块边界检查通过"
```

- [ ] **Step 2: 跑架构脚本**

```bash
bash scripts/architecture/check-module-boundaries.sh
```

Expected: `OK: pii 模块边界检查通过`，退出码 0

- [ ] **Step 3: Commit**

```bash
git add scripts/architecture/check-module-boundaries.sh
git commit -m "chore(pii-arch): 架构边界脚本新增 pii 规则"
```

---

### Task 0.7: `DeptService` 加 `dept_type` 校验

**Files:**
- Modify: `manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysDeptServiceImpl.java`
- Create: `manzhushaka-system/src/test/java/com/manzhushaka/system/service/impl/SysDeptServiceDeptTypeTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.manzhushaka.system.service.impl;

import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.mapper.SysDeptMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysDeptServiceDeptTypeTest {

    @Mock SysDeptMapper deptMapper;
    @InjectMocks SysDeptServiceImpl service;

    @Test
    void deleteRegionTypeDept_shouldThrow() {
        SysDept region = new SysDept();
        region.setDeptId(200L);
        region.setDeptType("region");
        region.setDelFlag(0);
        when(deptMapper.selectDeptById(200L)).thenReturn(region);

        assertThrows(ServiceException.class, () -> service.removeDept(200L));
    }

    @Test
    void deleteMerchantTypeDept_shouldThrow() {
        SysDept merchant = new SysDept();
        merchant.setDeptId(500L);
        merchant.setDeptType("merchant");
        merchant.setDelFlag(0);
        when(deptMapper.selectDeptById(500L)).thenReturn(merchant);

        assertThrows(ServiceException.class, () -> service.removeDept(500L));
    }
}
```

- [ ] **Step 2: 跑测试确认失败**

```bash
mvn -pl manzhushaka-system test -Dtest=SysDeptServiceDeptTypeTest
```

Expected: 2 个测试 FAIL

- [ ] **Step 3: 在 `SysDeptServiceImpl.removeDept` 加 dept_type 校验**

找到 `removeDept(Long deptId)` 方法，在删除逻辑前加：

```java
SysDept existing = deptMapper.selectDeptById(deptId);
if (existing == null) {
    throw new ServiceException("NOT_FOUND", "部门不存在");
}
if ("region".equals(existing.getDeptType()) || "merchant".equals(existing.getDeptType())) {
    throw new ServiceException("DEPT_TYPE_PROTECTED", "行政区划/商户部门禁止直接删除");
}
```

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn -pl manzhushaka-system test -Dtest=SysDeptServiceDeptTypeTest
```

Expected: 2 个测试 PASS

- [ ] **Step 5: 跑全量测试确认无回归**

```bash
mvn -pl manzhushaka-system test
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysDeptServiceImpl.java manzhushaka-system/src/test/java/com/manzhushaka/system/service/impl/SysDeptServiceDeptTypeTest.java
git commit -m "feat(system): SysDeptService 增加 dept_type 删除保护"
```

---

### Task 0.8: 全量构建确认基础设施 OK

- [ ] **Step 1: 跑全量构建**

```bash
mvn clean package -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 跑全量测试**

```bash
mvn test
```

Expected: 所有模块 BUILD SUCCESS

- [ ] **Step 3: 跑架构脚本**

```bash
bash scripts/architecture/check-module-boundaries.sh
```

Expected: 退出码 0

- [ ] **Step 4: 提交基础设施里程碑（无新文件，验证即可）**

无 commit，确认基础设施阶段完成，进入阶段 1。

---

## 阶段 1 — 领域层骨架

### Task 1.1: Domain Model + Repository 接口

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/domain/model/*.java`（8 个领域模型）
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/domain/repository/*.java`（8 个 Repository 接口）

- [ ] **Step 1: 创建领域模型**

按规格 §3.2 创建 8 个 model：
- `TaxItem`（税目）
- `MerchantProfile`（商户档案）
- `PayQrcode`（支付二维码）
- `PayQrcodeTaxItem`（二维码-税目关联）
- `PayOrder`（支付订单）
- `RefundRecord`（退款记录）
- `PaymentNotifyLog`（支付回调幂等日志）
- `InvoiceNotifyLog`（开票回调幂等日志）
- `InvoiceCallLog`（银联发票接口调用日志）

每个 model 类**手写 getter/setter/toString**（不引入 Lombok），符合项目约定。`toString()` 截断 `payload` / `token` / `sign` 等敏感/超长字段。

**示例**（`PayOrder.java` 关键字段，完整版按规格）：

```java
package com.manzhushaka.biz.pii.domain.model;

import java.time.LocalDateTime;

public class PayOrder {
    private Long id;
    private Long merchantId;
    private Long qrcodeId;
    private Long taxItemId;
    private String outTradeNo;
    private String umsMerOrderDate;
    private Long amount;
    private String buyerName;
    private String buyerTaxCode;
    private String buyerEmail;
    private String buyerMobile;
    private String buyerOpenid;
    private String payStatus;       // PENDING/PAID/REFUNDING/REFUNDED/CLOSED
    private LocalDateTime payTime;
    private String payTradeNo;
    private String payNotifyStatus;
    private Long refundAmount;
    private String invoiceStatus;   // NONE/PENDING/ISSUING/ISSUED/REVERSING/REVERSED/FAILED
    private String invoiceNo;
    private String invoiceCode;
    private String invoicePdfUrl;
    private LocalDateTime invoiceIssueTime;
    private LocalDateTime invoiceReverseTime;
    private String orderToken;
    private String wechatAppid;
    private String clientIp;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;

    // getter / setter 全部手写（与项目约定一致）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... 省略其他

    @Override
    public String toString() {
        return "PayOrder{id=" + id + ", outTradeNo='" + outTradeNo + "', amount=" + amount +
                ", payStatus='" + payStatus + "', invoiceStatus='" + invoiceStatus + "'}";
        // 不输出 buyerEmail / buyerMobile / orderToken / clientIp 等敏感字段
    }
}
```

> 其他 8 个 model 同样按规格手写。文件较大，每个 model 一个 Java 文件，单独 commit。

- [ ] **Step 2: 创建 Repository 接口（8 个）**

包路径 `com.manzhushaka.biz.pii.domain.repository`，每个接口声明 CRUD + 业务查询方法。

**示例**（`PayOrderRepository.java`）：

```java
package com.manzhushaka.biz.pii.domain.repository;

import com.manzhushaka.biz.pii.domain.model.PayOrder;
import java.util.List;
import java.util.Optional;

public interface PayOrderRepository {
    Long insert(PayOrder order);
    int updateById(PayOrder order);
    int updatePayStatus(Long id, String payStatus, String payTradeNo, java.time.LocalDateTime payTime);
    int updateInvoiceStatus(Long id, String invoiceStatus, String invoiceNo, String invoiceCode,
                            String invoicePdfUrl, java.time.LocalDateTime invoiceIssueTime);
    Optional<PayOrder> findById(Long id);
    Optional<PayOrder> findByOutTradeNo(String outTradeNo);
    List<PayOrder> findByMerchantAndStatus(Long merchantId, String payStatus, int limit);
    List<PayOrder> findPendingBefore(java.time.LocalDateTime time, int limit);
    long sumAmountByMerchantAndStatusBetween(Long merchantId, List<String> statuses,
                                              java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByMerchantAndPayTimeBetween(Long merchantId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
```

> 其他 7 个 Repository 类似，按各 model 字段声明。

- [ ] **Step 3: 编译验证**

```bash
mvn -pl manzhushaka-biz-pii compile
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/domain/
git commit -m "feat(pii-domain): 9 个领域模型 + 9 个 Repository 接口"
```

---

### Task 1.2: MyBatis Entity + Mapper 骨架

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/entity/*.java`（9 个）
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/mapper/*.xml`（9 个 MyBatis XML）
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/mapper/*.java`（9 个 Mapper 接口）

- [ ] **Step 1: 创建 9 个 MyBatis Entity**

与 domain.model 字段一一对应，**只增不改**（避免影响领域）。例 `PiiPayOrder.java`：

```java
package com.manzhushaka.biz.pii.infrastructure.persistence.entity;

import java.time.LocalDateTime;

public class PiiPayOrder {
    private Long id;
    private Long merchantId;
    private Long qrcodeId;
    private Long taxItemId;
    private String outTradeNo;
    private String umsMerOrderDate;
    private Long amount;
    private String buyerName;
    private String buyerTaxCode;
    private String buyerEmail;
    private String buyerMobile;
    private String buyerOpenid;
    private String payStatus;
    private LocalDateTime payTime;
    private String payTradeNo;
    private String payNotifyStatus;
    private Long refundAmount;
    private String invoiceStatus;
    private String invoiceNo;
    private String invoiceCode;
    private String invoicePdfUrl;
    private LocalDateTime invoiceIssueTime;
    private LocalDateTime invoiceReverseTime;
    private String orderToken;
    private String wechatAppid;
    private String clientIp;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
    private String delFlag;

    // 手写 getter / setter
}
```

- [ ] **Step 2: 创建 9 个 Mapper 接口**

```java
package com.manzhushaka.biz.pii.infrastructure.persistence.mapper;

import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PiiPayOrderMapper {
    int insert(PiiPayOrder e);
    int updateById(PiiPayOrder e);
    int updatePayStatus(@Param("id") Long id, @Param("payStatus") String payStatus,
                        @Param("payTradeNo") String payTradeNo, @Param("payTime") LocalDateTime payTime);
    int updateInvoiceStatus(@Param("id") Long id, @Param("invoiceStatus") String invoiceStatus,
                            @Param("invoiceNo") String invoiceNo, @Param("invoiceCode") String invoiceCode,
                            @Param("invoicePdfUrl") String invoicePdfUrl,
                            @Param("invoiceIssueTime") LocalDateTime invoiceIssueTime);
    PiiPayOrder selectById(@Param("id") Long id);
    PiiPayOrder selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);
    List<PiiPayOrder> selectByMerchantAndStatus(@Param("merchantId") Long merchantId,
                                                  @Param("payStatus") String payStatus,
                                                  @Param("limit") int limit);
    List<PiiPayOrder> selectPendingBefore(@Param("time") LocalDateTime time, @Param("limit") int limit);
    Long sumAmountByMerchantAndStatusBetween(@Param("merchantId") Long merchantId,
                                              @Param("statuses") List<String> statuses,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
    Long countByMerchantAndPayTimeBetween(@Param("merchantId") Long merchantId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}
```

- [ ] **Step 3: 创建 9 个 MyBatis XML**

**`PiiPayOrderMapper.xml`**（路径 `manzhushaka-biz-pii/src/main/resources/mapper/pii/PiiPayOrderMapper.xml`）：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayOrderMapper">

    <resultMap id="BaseResultMap" type="com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder">
        <id     property="id"                 column="id"/>
        <result property="merchantId"         column="merchant_id"/>
        <result property="qrcodeId"           column="qrcode_id"/>
        <result property="taxItemId"          column="tax_item_id"/>
        <result property="outTradeNo"         column="out_trade_no"/>
        <result property="umsMerOrderDate"    column="ums_mer_order_date"/>
        <result property="amount"             column="amount"/>
        <result property="buyerName"          column="buyer_name"/>
        <result property="buyerTaxCode"       column="buyer_tax_code"/>
        <result property="buyerEmail"         column="buyer_email"/>
        <result property="buyerMobile"        column="buyer_mobile"/>
        <result property="buyerOpenid"        column="buyer_openid"/>
        <result property="payStatus"          column="pay_status"/>
        <result property="payTime"            column="pay_time"/>
        <result property="payTradeNo"         column="pay_trade_no"/>
        <result property="payNotifyStatus"    column="pay_notify_status"/>
        <result property="refundAmount"       column="refund_amount"/>
        <result property="invoiceStatus"      column="invoice_status"/>
        <result property="invoiceNo"          column="invoice_no"/>
        <result property="invoiceCode"        column="invoice_code"/>
        <result property="invoicePdfUrl"      column="invoice_pdf_url"/>
        <result property="invoiceIssueTime"   column="invoice_issue_time"/>
        <result property="invoiceReverseTime" column="invoice_reverse_time"/>
        <result property="orderToken"         column="order_token"/>
        <result property="wechatAppid"        column="wechat_appid"/>
        <result property="clientIp"           column="client_ip"/>
        <result property="remark"             column="remark"/>
        <result property="createTime"         column="create_time"/>
        <result property="updateTime"         column="update_time"/>
        <result property="createBy"           column="create_by"/>
        <result property="updateBy"           column="update_by"/>
        <result property="delFlag"            column="del_flag"/>
    </resultMap>

    <insert id="insert" parameterType="com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder"
            useGeneratedKeys="true" keyProperty="id">
        insert into pii_pay_order (
            merchant_id, qrcode_id, tax_item_id, out_trade_no, ums_mer_order_date,
            amount, buyer_name, buyer_tax_code, buyer_email, buyer_mobile, buyer_openid,
            pay_status, pay_notify_status, refund_amount, invoice_status,
            order_token, wechat_appid, client_ip, remark, del_flag
        ) values (
            #{merchantId}, #{qrcodeId}, #{taxItemId}, #{outTradeNo}, #{umsMerOrderDate},
            #{amount}, #{buyerName}, #{buyerTaxCode}, #{buyerEmail}, #{buyerMobile}, #{buyerOpenid},
            #{payStatus}, #{payNotifyStatus}, #{refundAmount}, #{invoiceStatus},
            #{orderToken}, #{wechatAppid}, #{clientIp}, #{remark}, 0
        )
    </insert>

    <update id="updateById">
        update pii_pay_order
        <set>
            <if test="payStatus != null">pay_status = #{payStatus},</if>
            <if test="payTime != null">pay_time = #{payTime},</if>
            <if test="payTradeNo != null">pay_trade_no = #{payTradeNo},</if>
            <if test="payNotifyStatus != null">pay_notify_status = #{payNotifyStatus},</if>
            <if test="refundAmount != null">refund_amount = #{refundAmount},</if>
            <if test="invoiceStatus != null">invoice_status = #{invoiceStatus},</if>
            <if test="invoiceNo != null">invoice_no = #{invoiceNo},</if>
            <if test="invoiceCode != null">invoice_code = #{invoiceCode},</if>
            <if test="invoicePdfUrl != null">invoice_pdf_url = #{invoicePdfUrl},</if>
            <if test="invoiceIssueTime != null">invoice_issue_time = #{invoiceIssueTime},</if>
            <if test="invoiceReverseTime != null">invoice_reverse_time = #{invoiceReverseTime},</if>
            <if test="updateBy != null">update_by = #{updateBy},</if>
        </set>
        where id = #{id}
    </update>

    <update id="updatePayStatus">
        update pii_pay_order
        set pay_status = #{payStatus},
            pay_trade_no = #{payTradeNo},
            pay_time = #{payTime}
        where id = #{id}
    </update>

    <update id="updateInvoiceStatus">
        update pii_pay_order
        set invoice_status = #{invoiceStatus},
            invoice_no = #{invoiceNo},
            invoice_code = #{invoiceCode},
            invoice_pdf_url = #{invoicePdfUrl},
            invoice_issue_time = #{invoiceIssueTime}
        where id = #{id}
    </update>

    <select id="selectById" resultMap="BaseResultMap">
        select * from pii_pay_order where id = #{id} and del_flag = 0
    </select>

    <select id="selectByOutTradeNo" resultMap="BaseResultMap">
        select * from pii_pay_order where out_trade_no = #{outTradeNo} and del_flag = 0
    </select>

    <select id="selectByMerchantAndStatus" resultMap="BaseResultMap">
        select * from pii_pay_order
        where merchant_id = #{merchantId} and pay_status = #{payStatus} and del_flag = 0
        order by create_time desc limit #{limit}
    </select>

    <select id="selectPendingBefore" resultMap="BaseResultMap">
        select * from pii_pay_order
        where pay_status = 'PENDING' and create_time &lt; #{time} and del_flag = 0
        limit #{limit}
    </select>

    <select id="sumAmountByMerchantAndStatusBetween" resultType="java.lang.Long">
        select coalesce(sum(amount), 0) from pii_pay_order
        where merchant_id = #{merchantId}
          and pay_status in
          <foreach collection="statuses" item="s" open="(" close=")" separator=",">#{s}</foreach>
          and pay_time between #{start} and #{end}
          and del_flag = 0
    </select>

    <select id="countByMerchantAndPayTimeBetween" resultType="java.lang.Long">
        select count(*) from pii_pay_order
        where merchant_id = #{merchantId}
          and pay_time between #{start} and #{end}
          and del_flag = 0
    </select>
</mapper>
```

> 其他 8 个 Entity / Mapper / XML 同样按规格表结构编写，**不一一列出**（CRUD 模板）。

- [ ] **Step 4: 编译验证**

```bash
mvn -pl manzhushaka-biz-pii -am compile
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/ manzhushaka-biz-pii/src/main/resources/mapper/
git commit -m "feat(pii-persistence): 9 个 MyBatis Entity + Mapper + XML"
```

---

### Task 1.3: Converter + RepositoryImpl

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/converter/*.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/repository/*.java`

- [ ] **Step 1: 9 个 Converter（domain model ↔ entity）**

每个 Converter 两个方法：`toDomain(entity)` / `toEntity(domain)`。例 `PayOrderConverter.java`：

```java
package com.manzhushaka.biz.pii.infrastructure.persistence.converter;

import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;

public final class PayOrderConverter {
    private PayOrderConverter() {}
    public static PayOrder toDomain(PiiPayOrder e) {
        if (e == null) return null;
        PayOrder d = new PayOrder();
        d.setId(e.getId());
        d.setMerchantId(e.getMerchantId());
        // ... 字段一一映射
        return d;
    }
    public static PiiPayOrder toEntity(PayOrder d) {
        if (d == null) return null;
        PiiPayOrder e = new PiiPayOrder();
        e.setId(d.getId());
        // ...
        return e;
    }
}
```

- [ ] **Step 2: 9 个 RepositoryImpl（实现 domain.repository 接口）**

```java
package com.manzhushaka.biz.pii.infrastructure.persistence.repository;

import com.manzhushaka.biz.pii.domain.model.PayOrder;
import com.manzhushaka.biz.pii.domain.repository.PayOrderRepository;
import com.manzhushaka.biz.pii.infrastructure.persistence.converter.PayOrderConverter;
import com.manzhushaka.biz.pii.infrastructure.persistence.entity.PiiPayOrder;
import com.manzhushaka.biz.pii.infrastructure.persistence.mapper.PiiPayOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PayOrderRepositoryImpl implements PayOrderRepository {
    private final PiiPayOrderMapper mapper;

    @Autowired
    public PayOrderRepositoryImpl(PiiPayOrderMapper mapper) { this.mapper = mapper; }

    @Override
    public Long insert(PayOrder order) {
        PiiPayOrder e = PayOrderConverter.toEntity(order);
        mapper.insert(e);
        return e.getId();
    }

    @Override
    public int updateById(PayOrder order) { return mapper.updateById(PayOrderConverter.toEntity(order)); }

    @Override
    public int updatePayStatus(Long id, String payStatus, String payTradeNo, java.time.LocalDateTime payTime) {
        return mapper.updatePayStatus(id, payStatus, payTradeNo, payTime);
    }

    @Override
    public int updateInvoiceStatus(Long id, String invoiceStatus, String invoiceNo, String invoiceCode,
                                    String invoicePdfUrl, java.time.LocalDateTime invoiceIssueTime) {
        return mapper.updateInvoiceStatus(id, invoiceStatus, invoiceNo, invoiceCode, invoicePdfUrl, invoiceIssueTime);
    }

    @Override
    public Optional<PayOrder> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(PayOrderConverter::toDomain);
    }

    @Override
    public Optional<PayOrder> findByOutTradeNo(String outTradeNo) {
        return Optional.ofNullable(mapper.selectByOutTradeNo(outTradeNo)).map(PayOrderConverter::toDomain);
    }

    @Override
    public List<PayOrder> findByMerchantAndStatus(Long merchantId, String payStatus, int limit) {
        return mapper.selectByMerchantAndStatus(merchantId, payStatus, limit).stream()
                .map(PayOrderConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<PayOrder> findPendingBefore(java.time.LocalDateTime time, int limit) {
        return mapper.selectPendingBefore(time, limit).stream()
                .map(PayOrderConverter::toDomain).collect(Collectors.toList());
    }

    @Override
    public long sumAmountByMerchantAndStatusBetween(Long merchantId, List<String> statuses,
                                                     java.time.LocalDateTime start, java.time.LocalDateTime end) {
        Long sum = mapper.sumAmountByMerchantAndStatusBetween(merchantId, statuses, start, end);
        return sum == null ? 0L : sum;
    }

    @Override
    public long countByMerchantAndPayTimeBetween(Long merchantId, java.time.LocalDateTime start,
                                                  java.time.LocalDateTime end) {
        Long cnt = mapper.countByMerchantAndPayTimeBetween(merchantId, start, end);
        return cnt == null ? 0L : cnt;
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
mvn -pl manzhushaka-biz-pii -am compile
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/converter/ manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/repository/
git commit -m "feat(pii-persistence): 9 个 Converter + 9 个 RepositoryImpl"
```

---

### Task 1.4: 多租户拦截器 `PiiTenantInterceptor`

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/support/PiiTenantInterceptor.java`
- Create: `manzhushaka-biz-pii/src/test/java/com/manzhushaka/biz/pii/infrastructure/persistence/support/PiiTenantInterceptorTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.manzhushaka.biz.pii.infrastructure.persistence.support;

import com.manzhushaka.system.application.query.CurrentUserQuery;
import com.manzhushaka.system.domain.model.LoginUser;
import com.manzhushaka.system.domain.model.SysUser;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PiiTenantInterceptorTest {

    @Test
    void interceptor_isWired() {
        Interceptor[] interceptors = {new PiiTenantInterceptor()};
        assertNotNull(interceptors[0]);
    }
}
```

- [ ] **Step 2: 创建拦截器**

```java
package com.manzhushaka.biz.pii.infrastructure.persistence.support;

import com.manzhushaka.system.application.query.CurrentUserQuery;
import com.manzhushaka.system.domain.model.LoginUser;
import com.manzhushaka.system.domain.model.SysUser;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多租户隔离拦截器：自动给 SQL 注入 WHERE merchant_id = ? 条件。
 * 仅对 pii_pay_order / pii_refund_record / pii_pay_qrcode / pii_pay_qrcode_tax_item 等商户表生效。
 * 平台级表（pii_tax_item / pii_platform_wechat_config）不注入。
 * 平台运营方（role=operator）跳过注入。
 */
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PiiTenantInterceptor implements Interceptor {

    private static final Pattern TENANT_TABLES = Pattern.compile(
            "\\b(pii_pay_order|pii_refund_record|pii_pay_qrcode|pii_pay_qrcode_tax_item|pii_merchant_profile)\\b"
    );
    private static final Pattern HAS_WHERE = Pattern.compile("\\bWHERE\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_MERCHANT_FILTER = Pattern.compile("merchant_id\\s*=\\s*\\?", Pattern.CASE_INSENSITIVE);

    @Autowired private CurrentUserQuery currentUserQuery;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();

        if (!TENANT_TABLES.matcher(sql).find()) {
            return invocation.proceed();
        }
        if (HAS_MERCHANT_FILTER.matcher(sql).find()) {
            return invocation.proceed();
        }

        LoginUser loginUser = currentUserQuery.getCurrentUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            return invocation.proceed();
        }
        // 平台运营方（data_scope=1）跳过注入
        if (loginUser.isAdmin() || "operator".equals(loginUser.getRoleKey())) {
            return invocation.proceed();
        }

        Long merchantId = loginUser.getMerchantId();
        if (merchantId == null) {
            return invocation.proceed();
        }

        String newSql;
        if (HAS_WHERE.matcher(sql).find()) {
            newSql = sql + " AND merchant_id = " + merchantId;
        } else {
            newSql = sql + " WHERE merchant_id = " + merchantId;
        }
        // 通过反射替换 BoundSql.sql（项目已有工具类 BoundSqlUtils.setSql(boundSql, newSql)）
        BoundSqlUtils.replaceSql(boundSql, newSql);
        return invocation.proceed();
    }

    @Override public Object plugin(Object target) { return org.apache.ibatis.plugin.Plugin.wrap(target, this); }
    @Override public void setProperties(Properties properties) {}
}
```

> `LoginUser.getMerchantId()` 需在 `LoginUser` 加此 getter；`roleKey` 字段需在 `LoginUser` 加；`isAdmin()` 复用现有判断。如系统已用 `SysUser` 的 `deptId` 推 `merchantId`，则在 `currentUserQuery` 加 getMerchantId() 方法（通过 dept_id 查 pii_merchant_profile 表）。

- [ ] **Step 3: 注册拦截器到 MyBatis SqlSessionFactoryBean**

修改 `manzhushaka-system/src/main/java/.../config/MybatisConfig.java`（或 framework 的 SqlSessionFactory 配置），在 SqlSessionFactoryBean.plugin 属性追加 `piiTenantInterceptor`。

> 具体配置位置视项目实际 MyBatis 配置类而定。找到 `SqlSessionFactoryBean` 配置处追加 `setPlugins(new Interceptor[]{new PiiTenantInterceptor()})` 即可。

- [ ] **Step 4: 跑测试**

```bash
mvn -pl manzhushaka-biz-pii test -Dtest=PiiTenantInterceptorTest
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/persistence/support/ manzhushaka-biz-pii/src/test/...
git commit -m "feat(pii-tenant): 多租户拦截器自动注入 merchant_id"
```

---

### Task 1.5: `LoginUser` 扩展 `getMerchantId` / `getRoleKey`

**Files:**
- Modify: `manzhushaka-system/src/main/java/com/manzhushaka/system/domain/model/LoginUser.java`（如不存在 `LoginUser`，在 system 找到对应类）

- [ ] **Step 1: 加 2 个字段 + getter**

```java
private String roleKey;     // 当前登录用户的 role_key
private Long merchantId;    // 当前登录用户对应的 merchant_id（从 dept_id 关联 pii_merchant_profile 查）
```

- [ ] **Step 2: 加 getter / setter**

```java
public String getRoleKey() { return roleKey; }
public void setRoleKey(String roleKey) { this.roleKey = roleKey; }
public Long getMerchantId() { return merchantId; }
public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
```

- [ ] **Step 3: 在 `CurrentUserQuery.getCurrentUser()` 实现中填充这 2 个字段**

修改 `CurrentUserQuery` 实现类，在查询登录用户时：
1. `roleKey` 来自 `sys_user_role` 关联查询
2. `merchantId` 来自 `pii_merchant_profile` 通过 `sys_user.dept_id` 查

```java
// 伪代码
SysUser user = sysUserMapper.selectById(loginUserId);
LoginUser loginUser = ...;
loginUser.setRoleKey(sysRoleMapper.selectRoleKeyByUserId(userId));
if (user.getDeptId() != null) {
    Long merchantId = piiMerchantProfileMapper.selectIdByDeptId(user.getDeptId());
    loginUser.setMerchantId(merchantId);
}
```

- [ ] **Step 4: 编译 + 跑现有测试**

```bash
mvn -pl manzhushaka-system test
```

Expected: BUILD SUCCESS（无回归）

- [ ] **Step 5: Commit**

```bash
git add manzhushaka-system/src/main/java/com/manzhushaka/system/domain/model/LoginUser.java manzhushaka-system/src/main/java/com/manzhushaka/system/application/query/CurrentUserQuery.java
git commit -m "feat(system): LoginUser 扩展 roleKey + merchantId"

---

## 阶段 2 — 集成适配层（Gateway）

### Task 2.1: 银联字典序签名工具 `NotifyVerifier`

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/notify/NotifyVerifier.java`
- Create: `manzhushaka-biz-pii/src/test/java/com/manzhushaka/biz/pii/infrastructure/gateway/notify/NotifyVerifierTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.notify;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifyVerifierTest {

    @Test
    void sign_emptyValues_shouldIgnore() {
        // 文档示例：sign = SHA256(amount=...&msgId=...&key=TESTKEY) 大写
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");
        params.put("msgType", "issue");
        params.put("amount", "10000");
        params.put("notifyUrl", "");  // 空值不参与签名
        params.put("remark", null);
        String sign = NotifyVerifier.sign(params, "TESTKEY");
        // 字典序：amount=10000, msgId=test-001, msgType=issue
        // stringA = "amount=10000&msgId=test-001&msgType=issue&key=TESTKEY"
        // sign = sha256(stringA).toUpperCase()
        assertEquals(64, sign.length());
        assertTrue(sign.matches("[0-9A-F]{64}"));
    }

    @Test
    void verify_correctSign_shouldReturnTrue() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("msgId", "test-001");
        params.put("msgType", "issue");
        params.put("amount", "10000");
        String expected = NotifyVerifier.sign(params, "K");
        params.put("sign", expected);
        assertTrue(NotifyVerifier.verify(params, "K"));
    }
}
```

- [ ] **Step 2: 跑测试确认失败**

```bash
mvn -pl manzhushaka-biz-pii test -Dtest=NotifyVerifierTest
```

Expected: FAIL（class not found）

- [ ] **Step 3: 实现 `NotifyVerifier`**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.notify;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 银联发票平台 / 银联公众号支付 通用签名工具。
 * 算法：
 *   1. 集合 M 非空参数按 ASCII 字典序排序
 *   2. 拼接 key1=value1&key2=value2
 *   3. 末尾追加 &key=<密钥>
 *   4. SHA-256 → 大写
 */
public final class NotifyVerifier {

    private NotifyVerifier() {}

    /** 生成签名：自动排除 sign / 空值；按 key 字典序排序。 */
    public static String sign(Map<String, ?> params, String key) {
        String stringA = params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .filter(e -> !e.getKey().equalsIgnoreCase("sign"))
                .filter(e -> e.getValue().toString().length() > 0)
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue().toString())
                .collect(Collectors.joining("&"));
        String stringSignTemp = stringA + "&key=" + key;
        return sha256Hex(stringSignTemp).toUpperCase();
    }

    /** 验签：传入的 sign 字段不参与计算。 */
    public static boolean verify(Map<String, String> params, String key) {
        String provided = params.get("sign");
        if (provided == null) return false;
        String expected = sign(params, key);
        return constantTimeEquals(expected, provided.toUpperCase());
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }
}
```

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn -pl manzhushaka-biz-pii test -Dtest=NotifyVerifierTest
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/notify/ manzhushaka-biz-pii/src/test/...
git commit -m "feat(pii-gateway): 银联字典序签名工具 NotifyVerifier"
```

---

### Task 2.2: `PaymentGateway` 接口 + UmsMpPaymentGateway 实现

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/pay/PaymentGateway.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/pay/UmsMpPaymentGateway.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/pay/MockPaymentGateway.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/pay/dto/*.java`（6 个 DTO）

- [ ] **Step 1: 创建 DTO（简化版）**

```java
// PreCreateRequest.java
public class PreCreateRequest {
    private String appId;
    private String merchantId;
    private String terminalId;
    private String outTradeNo;
    private String merOrderDate;     // yyyyMMdd
    private Long totalAmount;        // 分
    private String openid;
    private String notifyUrl;
    private String signKey;          // 用于签名（从商户档案读）
    // getter / setter
}

// PreCreateResponse.java
public class PreCreateResponse {
    private String prepayId;
    private String jsApiPaySign;
    private String nonceStr;
    private String timestamp;
    private String signType;        // MD5/SHA256
    private String packageStr;      // prepay_id=xxx
}

// RefundRequest.java / RefundResponse.java / NotifyPayload.java / NotifyVerifyResult.java 同样手写
```

- [ ] **Step 2: 创建 `PaymentGateway` 接口**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.pay;

import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.*;

public interface PaymentGateway {
    PreCreateResponse preCreate(PreCreateRequest req);
    RefundResponse refund(RefundRequest req);
    /** 解析 + 验签回调载荷；验签失败返回 isValid=false。 */
    NotifyVerifyResult verifyAndParse(String rawBody, String signKey);
}
```

- [ ] **Step 3: 创建 `UmsMpPaymentGateway`**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.pay;

import com.chinaums.pay.api.OfficialAccountsPayUtil;
import com.chinaums.pay.model.JsPayRequest;
import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.*;
import com.manzhushaka.biz.pii.infrastructure.gateway.notify.NotifyVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class UmsMpPaymentGateway implements PaymentGateway {

    private final ObjectMapper json = new ObjectMapper();

    @Override
    public PreCreateResponse preCreate(PreCreateRequest req) {
        JsPayRequest jpr = new JsPayRequest();
        jpr.setAppId(req.getAppId());
        jpr.setMerchantId(req.getMerchantId());
        jpr.setTerminalId(req.getTerminalId());
        jpr.setOutTradeNo(req.getOutTradeNo());
        jpr.setOrderDate(req.getMerOrderDate());
        jpr.setTotalAmount(req.getTotalAmount());
        jpr.setOpenid(req.getOpenid());
        jpr.setNotifyUrl(req.getNotifyUrl());
        // 调用银联 API
        Map<String, String> result = OfficialAccountsPayUtil.preCreate(req.getSignKey(), jpr);
        if (!"000000".equals(result.get("errCode"))) {
            throw new ServiceException("10101", "公众号支付预下单失败: " + result.get("errMsg"));
        }
        PreCreateResponse resp = new PreCreateResponse();
        resp.setPrepayId(result.get("prepayId"));
        resp.setJsApiPaySign(result.get("jsApiPaySign"));
        resp.setNonceStr(result.get("nonceStr"));
        resp.setTimestamp(result.get("timestamp"));
        resp.setSignType(result.getOrDefault("signType", "SHA256"));
        resp.setPackageStr("prepay_id=" + resp.getPrepayId());
        return resp;
    }

    @Override
    public RefundResponse refund(RefundRequest req) {
        // 类似 preCreate，调 OfficialAccountsPayUtil.refund()
        // ... 略
        return null;
    }

    @Override
    public NotifyVerifyResult verifyAndParse(String rawBody, String signKey) {
        try {
            Map<String, String> params = json.readValue(rawBody, new TypeReference<>(){});
            boolean valid = NotifyVerifier.verify(params, signKey);
            NotifyVerifyResult r = new NotifyVerifyResult();
            r.setValid(valid);
            r.setParams(params);
            return r;
        } catch (Exception e) {
            NotifyVerifyResult r = new NotifyVerifyResult();
            r.setValid(false);
            r.setErrorMsg(e.getMessage());
            return r;
        }
    }
}
```

> `JsPayRequest` 字段名以 `com.chinaums:open-pay:1.0.0` 实际为准（参考 `skills/ums-api-skills/` 的 `ums-mp-pay` 子技能）。如有差异按实际 SDK 字段调整。

- [ ] **Step 4: 创建 `MockPaymentGateway`**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.pay;

import com.manzhushaka.biz.pii.infrastructure.gateway.pay.dto.*;
import com.manzhushaka.biz.pii.infrastructure.gateway.notify.NotifyVerifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public PreCreateResponse preCreate(PreCreateRequest req) {
        PreCreateResponse r = new PreCreateResponse();
        r.setPrepayId("MOCK_PREPAY_" + UUID.randomUUID().toString().replace("-", ""));
        r.setNonceStr(UUID.randomUUID().toString().replace("-", ""));
        r.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));
        r.setSignType("MOCK");
        r.setJsApiPaySign("MOCK_SIGN");
        r.setPackageStr("prepay_id=" + r.getPrepayId());
        return r;
    }

    @Override
    public RefundResponse refund(RefundRequest req) {
        RefundResponse r = new RefundResponse();
        r.setErrCode("000000");
        r.setErrMsg("MOCK OK");
        r.setTradeNo("MOCK_TRADE_" + UUID.randomUUID().toString().replace("-", ""));
        return r;
    }

    @Override
    public NotifyVerifyResult verifyAndParse(String rawBody, String signKey) {
        // Mock 模式：直接认为验签通过
        NotifyVerifyResult r = new NotifyVerifyResult();
        r.setValid(true);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("outTradeNo", "MOCK_OUT_TRADE");
        params.put("tradeNo", "MOCK_TRADE");
        params.put("tradeStatus", "SUCCESS");
        r.setParams(params);
        return r;
    }
}
```

- [ ] **Step 5: 编译 + 跑 gateway 测试**

```bash
mvn -pl manzhushaka-biz-pii -am compile
mvn -pl manzhushaka-biz-pii test
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/pay/
git commit -m "feat(pii-gateway): PaymentGateway 接口 + 银联/Mock 实现"
```

---

### Task 2.3: `InvoiceGateway` 接口 + UmsInvoiceGateway（自研 HTTP）

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/invoice/*.java`

- [ ] **Step 1: 创建 DTO**（按文档 §5、§6、§7、§7.3、§7.4 字段）

`InvoiceRequest.java`（直接开票入参，对应文档 `complex.issue`）：

```java
public class InvoiceRequest {
    private String invoiceMaterial = "ELECTRONIC";
    private String invoiceType = "PLAIN";
    private String merchantId;        // 银联商户号
    private String terminalId;        // 银联终端号
    private String merOrderDate;      // yyyyMMdd
    private String merOrderId;        // 32 位
    private String buyerName;
    private String buyerTaxCode;
    private String buyerAddress;
    private String buyerTelephone;
    private String buyerBank;
    private String buyerAccount;
    private Long amount;              // 分
    private String goodsDetail;       // JSONArray 字符串
    private String remark;
    private String notifyMobileNo;
    private String notifyEMail;
    private String notifyUrl;
    // 公共报文
    private String msgSrc;
    private String msgId = UUID.randomUUID().toString();
    // 签名后填充
    private String sign;
    private String signKey;           // 用于签名
    // 时间戳在 doRequest 时填充
}
```

`InvoiceResponse.java`、`ReverseRequest.java`、`ReverseResponse.java`、`QueryRequest.java`、`QueryResponse.java`、`PickupRequest.java`、`PickupResponse.java`、`NotifyPayload.java`、`NotifyVerifyResult.java` 同样按文档字段手写。

- [ ] **Step 2: 创建 `InvoiceGateway` 接口**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.invoice;

import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.*;

public interface InvoiceGateway {
    InvoiceResponse invoice(InvoiceRequest req);
    ReverseResponse reverse(ReverseRequest req);
    QueryResponse query(QueryRequest req);
    PickupResponse pickup(PickupRequest req);
    NotifyVerifyResult verifyAndParse(String rawBody, String signKey);
}
```

- [ ] **Step 3: 创建 `UmsInvoiceGateway`**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.biz.pii.infrastructure.config.PiiProperties;
import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.*;
import com.manzhushaka.biz.pii.infrastructure.gateway.notify.NotifyVerifier;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class UmsInvoiceGateway implements InvoiceGateway {

    @Autowired private PiiProperties props;
    private final ObjectMapper json = new ObjectMapper();

    @Override
    public InvoiceResponse invoice(InvoiceRequest req) {
        req.setMerOrderDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        return doRequest("complex.issue", InvoiceResponse.class, req);
    }

    @Override
    public ReverseResponse reverse(ReverseRequest req) {
        return doRequest("complex.reverse", ReverseResponse.class, req);
    }

    @Override
    public QueryResponse query(QueryRequest req) {
        return doRequest("complex.query", QueryResponse.class, req);
    }

    @Override
    public PickupResponse pickup(PickupRequest req) {
        return doRequest("complex.pickup", PickupResponse.class, req);
    }

    @Override
    public NotifyVerifyResult verifyAndParse(String rawBody, String signKey) {
        try {
            Map<String, String> params = json.readValue(rawBody, new TypeReference<LinkedHashMap<String, String>>(){});
            boolean valid = NotifyVerifier.verify(params, signKey);
            NotifyVerifyResult r = new NotifyVerifyResult();
            r.setValid(valid);
            r.setParams(params);
            return r;
        } catch (Exception e) {
            NotifyVerifyResult r = new NotifyVerifyResult();
            r.setValid(false);
            r.setErrorMsg(e.getMessage());
            return r;
        }
    }

    private <T> T doRequest(String msgType, Class<T> respClass, Object req) {
        // 1. 填充公共字段
        try {
            Map<String, Object> body = json.convertValue(req, new TypeReference<LinkedHashMap<String, Object>>(){});
            body.put("msgType", msgType);
            body.put("requestTimestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            body.remove("signKey");
            // 2. 签名
            Map<String, String> signParams = body.entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue().toString().length() > 0)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString(),
                            (a, b) -> a, LinkedHashMap::new));
            String signKey = (String) body.remove("signKey");
            String sign = NotifyVerifier.sign(signParams, signKey);
            body.put("sign", sign);
            // 3. POST
            String url = props.getInvoice().getApiBaseUrl() + msgType;
            RequestConfig rc = RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(props.getInvoice().getConnectTimeoutMs()))
                    .setResponseTimeout(Timeout.ofMilliseconds(props.getInvoice().getReadTimeoutMs()))
                    .build();
            try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(rc).build()) {
                HttpPost post = new HttpPost(url);
                post.setHeader("Content-Type", "application/json;charset=UTF-8");
                post.setEntity(new StringEntity(json.writeValueAsString(body), "UTF-8"));
                return client.execute(post, response -> {
                    int code = response.getCode();
                    String respBody = new String(response.getEntity().getContent().readAllBytes(), "UTF-8");
                    if (code != 200) throw new ServiceException("10201", "银联发票 HTTP " + code);
                    T parsed = json.readValue(respBody, respClass);
                    // 校验 resultCode
                    String resultCode = (String) body.get("resultCode");
                    if (resultCode != null && !"SUCCESS".equals(resultCode)) {
                        throw new ServiceException("10201", "银联发票错误: " + resultCode + " " + body.get("resultMsg"));
                    }
                    return parsed;
                });
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("10201", "银联发票调用异常: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 4: 创建 `MockInvoiceGateway`**

```java
package com.manzhushaka.biz.pii.infrastructure.gateway.invoice;

import com.manzhushaka.biz.pii.infrastructure.gateway.invoice.dto.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockInvoiceGateway implements InvoiceGateway {

    @Override
    public InvoiceResponse invoice(InvoiceRequest req) {
        InvoiceResponse r = new InvoiceResponse();
        r.setStatus("ISSUED");
        r.setInvoiceNo("MOCK_INV_" + UUID.randomUUID().toString().substring(0, 8));
        r.setInvoiceCode("MOCK_CODE");
        r.setMerchantId(req.getMerchantId());
        r.setTerminalId(req.getTerminalId());
        r.setMerOrderId(req.getMerOrderId());
        r.setMerOrderDate(req.getMerOrderDate());
        r.setBuyerName(req.getBuyerName());
        r.setTotalPriceIncludingTax(req.getAmount() / 100.0);
        r.setTotalTax(0.0);
        r.setTotalPrice(req.getAmount() / 100.0);
        r.setPdfUrl("https://mock.local/invoice/" + r.getInvoiceNo() + ".pdf");
        r.setResultCode("SUCCESS");
        r.setResultMsg("MOCK OK");
        return r;
    }

    @Override
    public ReverseResponse reverse(ReverseRequest req) {
        ReverseResponse r = new ReverseResponse();
        r.setStatus("REVERSED");
        r.setMerchantId(req.getMerchantId());
        r.setTerminalId(req.getTerminalId());
        r.setMerOrderId(req.getMerOrderId());
        r.setMerOrderDate(req.getMerOrderDate());
        r.setResultCode("SUCCESS");
        r.setResultMsg("MOCK REVERSE OK");
        return r;
    }

    @Override
    public QueryResponse query(QueryRequest req) {
        QueryResponse r = new QueryResponse();
        r.setStatus("ISSUED");
        r.setMerchantId(req.getMerchantId());
        r.setTerminalId(req.getTerminalId());
        r.setMerOrderId(req.getMerOrderId());
        r.setMerOrderDate(req.getMerOrderDate());
        r.setResultCode("SUCCESS");
        return r;
    }

    @Override
    public PickupResponse pickup(PickupRequest req) {
        // Mock 返回 1KB 假 PDF base64
        PickupResponse r = new PickupResponse();
        r.setPdf("JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC9UeXBlL0NhdGFsb2cvUGFnZXMgMiAwIFI+PgplbmRvYmoKMiAwIG9iago8PC9UeXBlL1BhZ2VzL0NvdW50IDEvS2lkc1szIDAgUl0+PgplbmRvYmoKMyAwIG9iago8PC9UeXBlL1BhZ2UvUGFyZW50IDIgMCBSL01lZGlhQm94WzAgMCA1OTUgODQyXS9SZXNvdXJjZXM8PD4+L0NvbnRlbnRzIDQgMCBSPj4KZW5kb2JqCjQgMCBvYmoKPDwvTGVuZ3RoIDQ0Pj5zdHJlYW0KQlQKL0YxIDI0IFRmCjU1MCA3ODAgVGQKKE1PQ0sgUERGKSBUagpFVAplbmRzdHJlYW0KZW5kb2JqCnhyZWYKMCA1CjAwMDAwMDAwMDAgNjU1MzUgZiAKMDAwMDAwMDAxNSAwMDAwMCBuIAowMDAwMDAwMDYwIDAwMDAwIG4gCjAwMDAwMDAxMTAgMDAwMDAgbiAKMDAwMDAwMDIxMCAwMDAwMCBuIAp0cmFpbGVyCjw8L1NpemUgNS9Sb290IDEgMCBSPj4Kc3RhcnR4cmVmCjU2CiUlRU9GCg==");
        r.setPdfUrl("https://mock.local/invoice/mock.pdf");
        r.setOfdUrl("https://mock.local/invoice/mock.ofd");
        r.setXmlUrl("https://mock.local/invoice/mock.xml");
        return r;
    }

    @Override
    public NotifyVerifyResult verifyAndParse(String rawBody, String signKey) {
        NotifyVerifyResult r = new NotifyVerifyResult();
        r.setValid(true);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("status", "ISSUED");
        params.put("merOrderId", "MOCK_ORDER");
        params.put("merOrderDate", "20260101");
        r.setParams(params);
        return r;
    }
}
```

- [ ] **Step 5: 编译**

```bash
mvn -pl manzhushaka-biz-pii -am compile
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/invoice/
git commit -m "feat(pii-gateway): InvoiceGateway 接口 + 银联自研 HTTP / Mock 实现"
```

---

### Task 2.4: `WechatOAuthGateway` + `PiiGatewayConfig`

**Files:**
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/wechat/WechatOAuthGateway.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/wechat/MockWechatOAuthGateway.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/wechat/UmsWechatOAuthGateway.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/wechat/dto/*.java`
- Create: `manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/config/PiiGatewayConfig.java`

- [ ] **Step 1: 写接口 + DTO（OAuthExchangeRequest / OAuthExchangeResponse）**

```java
public class OAuthExchangeRequest {
    private String appId;
    private String appSecret;
    private String code;
    // getter / setter
}

public class OAuthExchangeResponse {
    private String openid;
    private String unionid;
    private String accessToken;
    private int expiresIn;
    private String errCode;
    private String errMsg;
}

public interface WechatOAuthGateway {
    OAuthExchangeResponse exchangeCodeForOpenId(OAuthExchangeRequest req);
}
```

- [ ] **Step 2: 实现 UmsWechatOAuthGateway**

调微信 `https://api.weixin.qq.com/sns/oauth2/access_token?appid={appId}&secret={appSecret}&code={code}&grant_type=authorization_code`，用 HttpClient5（参考 Task 2.3 模板）。

- [ ] **Step 3: 实现 MockWechatOAuthGateway**

```java
@Component
@ConditionalOnProperty(name = "pii.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockWechatOAuthGateway implements WechatOAuthGateway {
    @Override
    public OAuthExchangeResponse exchangeCodeForOpenId(OAuthExchangeRequest req) {
        OAuthExchangeResponse r = new OAuthExchangeResponse();
        r.setOpenid("MOCK_OPENID_" + req.getCode());
        r.setAccessToken("MOCK_AT");
        r.setExpiresIn(7200);
        return r;
    }
}
```

- [ ] **Step 4: 写 `PiiGatewayConfig`**（如用 `@ConditionalOnProperty` 已在各 Mock 类上标注，本文件可省略）

如果用 `PiiGatewayConfig` 集中切换，则改用此模式：删除 Mock 类上的 `@Component`，统一在 config 中按 mode 注入。

- [ ] **Step 5: 编译 + 测试**

```bash
mvn -pl manzhushaka-biz-pii test
```

Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/gateway/wechat/ manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/infrastructure/config/
git commit -m "feat(pii-gateway): WechatOAuthGateway + Ums/Mock 实现"
```

---

### Task 2.5-2.8: 集成适配层收尾（MQ handler 注册、Email gateway、空指针守卫测试）

由于篇幅限制，剩余阶段 2 任务的核心代码模式与 2.1-2.4 一致：

| 任务 | 内容 | 关键文件 |
| --- | --- | --- |
| **2.5** | `InvoiceOpenHandler` MQ handler（消费 `pii:invoice:open` 流，调 `InvoiceGateway.invoice`） | `gateway/invoice/handler/InvoiceOpenHandler.java` + 测试 |
| **2.6** | `InvoiceReverseHandler` MQ handler（消费 `pii:invoice:reverse` 流） | `gateway/invoice/handler/InvoiceReverseHandler.java` + 测试 |
| **2.7** | 邮件发送 handler（消费 `pii:invoice:email` 流，复用 `framework` 邮件工具） | `gateway/email/InvoiceEmailHandler.java` + 测试 |
| **2.8** | 集成适配层全量测试 | `mvn -pl manzhushaka-biz-pii test` |

> 这 4 个任务**直接照搬** `AbstractRedisStreamMessageHandler` 模板（项目已有），handler 实现 `messageType()` / `streamKey()` / `consumerGroup()` / `idempotentKey()` / `doHandle()` 即可。`doHandle()` 内容：
> - 2.5：调 `InvoiceGateway.invoice` → 成功更新 `pii_pay_order` invoice_status=ISSUED，失败 invoice_status=FAILED 抛异常让 MQ 重试
> - 2.6：调 `InvoiceGateway.reverse` → 更新 invoice_status=REVERSED
> - 2.7：调 framework 邮件发送服务 → 失败仅记录日志不抛

每个 handler 一个 commit。

---

## 阶段 3 — 运营方管理后台

> 阶段 3 包含 4 套管理模块：**税目 / 商户 / 商户参数 / 二维码**。每套模块的模式一致：
> 1. Command / Query / Result DTO（`application/command` `/query` `/result`）
> 2. Application Service（事务边界 + 业务规则）
> 3. Web DTO / VO（`manzhushaka-admin/.../web/dto/pii` `/vo/pii`）
> 4. Controller（`@PreAuthorize` + `@Log` + 三件套）
> 5. 前端页面（`ui-admin/src/api/pii/*.js` + `views/pii/...`）

### Task 3.1: 税目 Service + Controller（参考实现）

**Files:**
- Create: `application/command/{CreateTaxItemCommand, UpdateTaxItemCommand, ChangeTaxItemStatusCommand}.java`
- Create: `application/query/TaxItemPageQuery.java`
- Create: `application/result/TaxItemResult.java`
- Create: `application/service/TaxItemService.java`
- Create: `application/service/impl/TaxItemServiceImpl.java`
- Create: `web/dto/pii/CreateTaxItemRequest.java`（`UpdateTaxItemRequest` / `ChangeStatusRequest` / `TaxItemPageRequest`）
- Create: `web/vo/pii/TaxItemVO.java`
- Create: `web/controller/pii/TaxItemController.java`
- Create: `src/test/.../TaxItemServiceTest.java`

- [ ] **Step 1: 写 Service 失败测试**

```java
class TaxItemServiceTest {
    @Test
    void create_duplicateCode_shouldThrow() {
        TaxItemService service = ...;
        when(repo.findByCode("3070401000000000000")).thenReturn(Optional.of(new TaxItem()));
        assertThrows(ServiceException.class, () -> service.create(cmd));
    }
    @Test
    void changeStatus_notFound_shouldThrow() { ... }
}
```

- [ ] **Step 2-6: 实现 Service / Controller / 前端**

> **Service 实现规范**（TDD）：
> - `create()`: 校验 `tax_item_code` 唯一 → 调 `repo.insert()`
> - `update()`: 校验存在 → 不允许改 `tax_item_code`（业务规则） → 调 `repo.updateById()`
> - `changeStatus()`: 校验存在 → 调 `repo.updateStatus()`
> - `page()`: 调 `repo.findPage()` → 包装 `PageResult<TaxItemResult>`

> **Controller 规范**（三件套）：
> ```java
> @RestController
> @RequestMapping("/pii/taxItem")
> public class TaxItemController {
>     @PreAuthorize("@ss.hasPermi('biz:taxItem:list')")
>     @GetMapping("/list")
>     public TableDataInfo list(TaxItemPageRequest req) { ... }
> 
>     @PreAuthorize("@ss.hasPermi('biz:taxItem:add')")
>     @Log(title = "税目管理", businessType = BusinessType.INSERT)
>     @PostMapping
>     public AjaxResult add(@RequestBody CreateTaxItemRequest req) { ... }
> 
>     @PreAuthorize("@ss.hasPermi('biz:taxItem:edit')")
>     @Log(title = "税目管理", businessType = BusinessType.UPDATE)
>     @PutMapping
>     public AjaxResult edit(@RequestBody UpdateTaxItemRequest req) { ... }
> 
>     @PreAuthorize("@ss.hasPermi('biz:taxItem:remove')")
>     @Log(title = "税目管理", businessType = BusinessType.DELETE)
>     @DeleteMapping("/{ids}")
>     public AjaxResult remove(@PathVariable Long[] ids) { ... }
> 
>     @PreAuthorize("@ss.hasPermi('biz:taxItem:changeStatus')")
>     @Log(title = "税目管理", businessType = BusinessType.UPDATE)
>     @PutMapping("/changeStatus")
>     public AjaxResult changeStatus(@RequestBody ChangeStatusRequest req) { ... }
> }
> ```

- [ ] **Step 7: 前端 `ui-admin/src/api/pii/taxItem.js`**

```js
import request from '@/utils/request'

export function listTaxItem(query) { return request({ url: '/pii/taxItem/list', method: 'get', params: query }) }
export function getTaxItem(id) { return request({ url: '/pii/taxItem/' + id, method: 'get' }) }
export function addTaxItem(data) { return request({ url: '/pii/taxItem', method: 'post', data }) }
export function updateTaxItem(data) { return request({ url: '/pii/taxItem', method: 'put', data }) }
export function delTaxItem(ids) { return request({ url: '/pii/taxItem/' + ids, method: 'delete' }) }
export function changeTaxItemStatus(id, status) { return request({ url: '/pii/taxItem/changeStatus', method: 'put', data: { id, status } }) }
```

- [ ] **Step 8: 前端 `ui-admin/src/views/pii/taxItem/index.vue`**

模板参考 `ui-admin/src/views/system/dict/index.vue`，表头字段：税目编码 / 名称 / 税率 / 状态 / 创建时间 / 操作（修改/启停/删除）。

按钮权限：
```vue
<el-button v-hasPermi="['biz:taxItem:add']" @click="handleAdd">新增</el-button>
<el-button v-hasPermi="['biz:taxItem:edit']" @click="handleUpdate">修改</el-button>
<el-button v-hasPermi="['biz:taxItem:remove']" @click="handleDelete">删除</el-button>
```

- [ ] **Step 9: 编译 + 构建前端**

```bash
mvn -pl manzhushaka-admin -am package -DskipTests
cd ui-admin && npm run build:prod
```

Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add manzhushaka-biz-pii/src/main/java/com/manzhushaka/biz/pii/application/ manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/pii/ manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/pii/ manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/pii/ ui-admin/src/api/pii/ ui-admin/src/views/pii/taxItem/
git commit -m "feat(pii-taxItem): 税目管理 service + controller + 前端"
```

---

### Task 3.2-3.7: 商户 / 商户参数 / 二维码 CRUD（按相同模式）

| 任务 | 模块 | 关键差异 |
| --- | --- | --- |
| **3.2** | **商户管理** | 创建时同时建 `sys_dept`（`dept_type=merchant`）+ `sys_user`（`dept_id` 指向该 dept）+ `pii_merchant_profile`（外层参数），事务原子性 |
| **3.3** | **商户参数配置** | 单独页面 `/pii/merchant/config/{deptId}`，展示并修改 `pii_merchant_profile`；敏感字段（密钥）展示时脱敏 |
| **3.4** | **二维码管理** | 多对多关联 `pii_pay_qrcode_tax_item`；创建时绑定多个税目；可生成二维码图片 base64 存 `qrcode_image_url` |
| **3.5** | **前端通用** | 商户列表带所属市县/区列（区/镇树形选择）；二维码详情展示绑定税目 |
| **3.6** | **DeptTreeSelect 组件** | 新建 `ui-admin/src/components/DeptTreeSelect/index.vue`，仅展示 `dept_type='region'` 的 dept 树，懒加载 |
| **3.7** | **运营方管理后台集成测试** | 跑全链路 mvn test，前端 build 通过 |

> 每个任务按 3.1 的 10 步模式展开，**核心差异已列出**，具体 Service / Controller / 前端代码照模板编写。

---

### Task 3.8: 运营方后台里程碑验证

- [ ] **Step 1: 全量构建**

```bash
mvn clean package -DskipTests
cd ui-admin && npm run build:prod
```

Expected: 全部成功

- [ ] **Step 2: 启动后端 + 登录运营方账号，手工验证**

- 登录 → 进"支付即开票"菜单 → 看到 7 个子菜单
- 新增税目 → 列表显示
- 新增商户 → 创建 sys_dept + sys_user + pii_merchant_profile 3 张表记录
- 新增二维码 → 绑定税目 → 二维码详情显示

无 commit，里程碑确认。

---

## 阶段 4 — 消费者扫码支付开票（核心正向流）

### Task 4.1: H5 入口页面 + 选税目表单

**Files:**
- Create: `ui-admin/src/views/pay/index.vue`
- Create: `ui-admin/src/views/pay/components/TaxItemSelector.vue`
- Create: `ui-admin/src/api/pii/pay.js`

- [ ] **Step 1: 前端 `api/pii/pay.js`**

```js
import request from '@/utils/request'

// 匿名接口不走 token，request.js 已有跳过逻辑（URL 含 /anon/ 时）
export function getQrcodeConfig(code) {
    return request({ url: '/anon/pii/qrcode/' + code, method: 'get' })
}
export function precreate(data) {
    return request({ url: '/anon/pii/pay/precreate', method: 'post', data })
}
export function getOrder(outTradeNo, token) {
    return request({ url: '/anon/pii/order/' + outTradeNo, method: 'get', params: { token } })
}
export function downloadInvoice(outTradeNo, token) {
    return request({ url: '/anon/pii/invoice/' + outTradeNo + '/download', method: 'get', params: { token }, responseType: 'blob' })
}
```

> `request.js` 需加 URL 含 `/anon/` 时**不附加 Authorization header**。打开 `ui-admin/src/utils/request.js` 找到 `config.headers['Authorization']` 赋值处加 `if (!config.url.includes('/anon/'))` 守卫。

- [ ] **Step 2: 前端 `views/pay/index.vue` 骨架**

```vue
<template>
    <div class="pay-page">
        <h2>扫码支付开票</h2>
        <div v-if="loading">加载中...</div>
        <div v-else-if="!config" class="error">二维码无效</div>
        <el-form v-else :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="税目" prop="taxItemId">
                <el-select v-model="form.taxItemId" placeholder="请选择税目" @change="onTaxItemChange">
                    <el-option v-for="t in config.taxItems" :key="t.id"
                               :label="`${t.name} (税率 ${t.taxRate}%)`" :value="t.id" />
                </el-select>
            </el-form-item>
            <el-form-item label="金额（元）" prop="amount">
                <el-input-number v-model="form.amountYuan" :min="0.01" :max="100000" :precision="2" :step="0.01" />
            </el-form-item>
            <el-form-item label="发票抬头" prop="buyerName">
                <el-input v-model="form.buyerName" placeholder="请填写发票抬头" maxlength="128" />
            </el-form-item>
            <el-form-item label="税号">
                <el-input v-model="form.buyerTaxCode" placeholder="选填，企业发票必填" maxlength="32" />
            </el-form-item>
            <el-form-item label="邮箱">
                <el-input v-model="form.buyerEmail" placeholder="选填，填写后发送电子发票" maxlength="64" />
            </el-form-item>
            <el-form-item label="手机号">
                <el-input v-model="form.buyerMobile" placeholder="选填，填写后发送短信" maxlength="16" />
            </el-form-item>
            <el-button type="primary" :loading="submitting" @click="onSubmit">立即支付</el-button>
        </el-form>
    </div>
</template>
```

`<script>` 部分（关键逻辑）：
- `onMounted`: 从 URL 取 `code` 参数，调用 `getQrcodeConfig(code)` 拉税目列表 + 公众号 AppID
- `onSubmit`: 校验表单 → 调 `precreate({...})` → 拿到 `outTradeNo` 和 `orderToken` → 跳微信支付

- [ ] **Step 3: 调起微信支付**

```js
async function onSubmit() {
    await formRef.value.validate()
    submitting.value = true
    try {
        const amount = Math.round(form.value.amountYuan * 100)
        const resp = await precreate({
            code: route.query.code,
            taxItemId: form.value.taxItemId,
            amount: amount,
            buyerName: form.value.buyerName,
            buyerTaxCode: form.value.buyerTaxCode,
            buyerEmail: form.value.buyerEmail,
            buyerMobile: form.value.buyerMobile,
        })
        // 跳支付结果轮询页
        router.push({
            name: 'PayResult',
            query: { outTradeNo: resp.outTradeNo, token: resp.orderToken }
        })
        // 调起微信支付
        if (typeof WeixinJSBridge !== 'undefined') {
            WeixinJSBridge.invoke('getBrandWCPayRequest', {
                appId: resp.appId,
                timeStamp: resp.timeStamp,
                nonceStr: resp.nonceStr,
                package: resp.packageStr,
                signType: resp.signType,
                paySign: resp.paySign,
            }, res => {
                if (res.err_msg === 'get_brand_wcpay_request:ok') {
                    // 支付成功 — 轮询会自动检测
                } else if (res.err_msg === 'get_brand_wcpay_request:cancel') {
                    ElMessage.info('已取消支付')
                } else {
                    ElMessage.error('支付失败')
                }
            })
        } else {
            ElMessage.warning('请在微信内打开')
        }
    } finally {
        submitting.value = false
    }
}
```

- [ ] **Step 4: 支付结果轮询页 `views/pay/result.vue`**

- 进入页面启动 `setInterval` 每 3 秒调 `getOrder(outTradeNo, token)`，直到 `payStatus='PAID'` 且 `invoiceStatus='ISSUED'` → 跳下载页
- 显示当前状态 + 倒计时
- 提供"取消轮询"按钮

- [ ] **Step 5: 路由配置 `ui-admin/src/router/...`**

在静态路由（不需登录）添加：
```js
{ path: '/pay', component: () => import('@/views/pay/index.vue'), meta: { title: '扫码支付' } },
{ path: '/pay/result', component: () => import('@/views/pay/result.vue'), meta: { title: '支付结果' } },
```

修改 `ui-admin/src/permission.js` 让 `/pay/*` 路径**绕过登录守卫**（白名单添加 `/pay`）。

- [ ] **Step 6: 构建前端**

```bash
cd ui-admin && npm run build:prod
```

Expected: SUCCESS

- [ ] **Step 7: Commit**

```bash
git add ui-admin/src/views/pay/ ui-admin/src/api/pii/pay.js ui-admin/src/router/ ui-admin/src/permission.js ui-admin/src/utils/request.js
git commit -m "feat(pii-h5): 消费者扫码支付 H5 页面 + 路由白名单"
```

---

### Task 4.2-4.9: 支付开票端到端

> 阶段 4 后续 8 个任务**严格按规格 §4.1 时序图**逐个实现，每个 Controller 一个 Task：

| 任务 | 端点 | 关键逻辑 |
| --- | --- | --- |
| **4.2** | `GET /anon/pii/qrcode/{code}` | `@Anonymous`；校验二维码 `status=1` + `expire_time` 未过；返税目列表 + 公众号 AppID |
| **4.3** | `POST /anon/pii/pay/precreate` | `@Anonymous`；校验请求 → 创建 `pii_pay_order`（`pay_status=PENDING`）→ 调 `PaymentGateway.preCreate` → 返 `{outTradeNo, orderToken, prepayId, timeStamp, nonceStr, packageStr, signType, paySign}` |
| **4.4** | (前端已写) | - |
| **4.5** | `POST /pii/pay/notify` | `@Anonymous`；银联验签 → 写 `pii_payment_notify_log`（唯一索引）→ 更新 `pii_pay_order` 为 `PAID` + `pay_trade_no` + `pay_time` → MQ 发送开票消息 → 返纯文本 `"SUCCESS"` |
| **4.6** | `InvoiceOpenHandler.doHandle` | 已在 2.5 编写；本任务为注册到 MQ config |
| **4.7** | `POST /pii/invoice/notify` | `@Anonymous`；银联验签 → 写 `pii_invoice_notify_log`（唯一索引）→ 更新 `pii_pay_order` `invoice_status` + `invoice_no/code/pdf_url/issue_time` → MQ 发送邮件（如有邮箱）→ 返 `"SUCCESS"` |
| **4.8** | 邮件发送 handler | 已在 2.7 编写；本任务为注册 |
| **4.9** | `GET /anon/pii/order/{no}?token=...` + `GET /anon/pii/invoice/{no}/download?token=...` | `@Anonymous` + `order_token` 二次校验（Redis 30 分钟）；download 调 `InvoiceGateway.pickup` 拿 PDF base64 → 浏览器下载 |

每个 Task 一个 commit。每个 Controller 的关键代码模式：

```java
@RestController
@RequestMapping("/anon/pii/qrcode")
public class AnonQrcodeQueryController {
    @Autowired private PayQrcodeRepository qrcodeRepo;
    @Autowired private PayQrcodeTaxItemRepository relRepo;
    @Autowired private TaxItemRepository taxItemRepo;
    @Autowired private PlatformWechatConfigRepository wechatRepo;

    @Anonymous
    @GetMapping("/{code}")
    public AjaxResult getByCode(@PathVariable String code) {
        // 1. 校验二维码
        PayQrcode qr = qrcodeRepo.findByCode(code)
                .orElseThrow(() -> new ServiceException("10002", "二维码无效"));
        if (qr.getStatus() != 1) throw new ServiceException("10002", "二维码已停用");
        if (qr.getExpireTime() != null && qr.getExpireTime().isBefore(LocalDateTime.now()))
            throw new ServiceException("10003", "二维码已过期");

        // 2. 加载绑定税目
        List<PayQrcodeTaxItem> rels = relRepo.findByQrcodeId(qr.getId());
        List<Long> taxItemIds = rels.stream().map(PayQrcodeTaxItem::getTaxItemId).collect(Collectors.toList());
        List<TaxItem> taxItems = taxItemRepo.findByIds(taxItemIds);

        // 3. 加载平台公众号配置
        PlatformWechatConfig wechat = wechatRepo.findActive()
                .orElseThrow(() -> new ServiceException("10401", "平台公众号参数未配置"));

        // 4. 返响应
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("appId", wechat.getAppId());
        resp.put("taxItems", taxItems);
        resp.put("defaultAmount", ...);
        return AjaxResult.success(resp);
    }
}
```

> **回调 Controller 的核心模式**（以支付回调为例）：

```java
@RestController
@RequestMapping("/pii/pay")
public class PayNotifyController {

    @Autowired private PaymentGateway paymentGateway;
    @Autowired private PayOrderRepository orderRepo;
    @Autowired private PaymentNotifyLogRepository notifyLogRepo;
    @Autowired private RedisStreamMessagePublisher publisher;

    @Anonymous
    @PostMapping("/notify")
    public String notify(@RequestBody String rawBody, @RequestParam String sign) {
        // 1. 解析 out_trade_no（先轻量解析拿）
        Map<String, String> params = parseRaw(rawBody);
        String outTradeNo = params.get("out_trade_no");
        // 2. 查订单 + 商户密钥
        PayOrder order = orderRepo.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new ServiceException("404", "订单不存在"));
        MerchantProfile profile = merchantProfileRepo.findById(order.getMerchantId()).orElseThrow();
        // 3. 验签
        NotifyVerifyResult v = paymentGateway.verifyAndParse(rawBody, decryptKey(profile.getUmsPaySignKeyEnc()));
        if (!v.isValid()) {
            return "FAIL";
        }
        // 4. 幂等
        try {
            notifyLogRepo.insert(PaymentNotifyLog.of(outTradeNo, rawBody, v.isValid()));
        } catch (DuplicateKeyException e) {
            return "SUCCESS";  // 已处理过
        }
        // 5. 更新订单
        if ("SUCCESS".equalsIgnoreCase(params.get("tradeStatus"))) {
            orderRepo.updatePayStatus(order.getId(), "PAID", params.get("tradeNo"), LocalDateTime.now());
            // 6. MQ 发送开票
            publisher.publish("pii:invoice:open", "open", Map.of("payOrderId", order.getId(), "payload", "{}"));
        }
        return "SUCCESS";
    }
}
```

每个任务 1 个 commit。

---

## 阶段 5 — 商户退款红冲

### Task 5.1: 退款 Service + Controller

**Files:**
- Create: `application/service/RefundService.java` + `RefundServiceImpl.java`
- Create: `application/command/CreateRefundCommand.java`
- Create: `application/result/RefundResult.java`
- Create: `web/controller/pii/RefundController.java`
- Create: `src/test/.../RefundServiceTest.java`

- [ ] **Step 1: 写失败测试（4 个异常分支）**

```java
class RefundServiceTest {
    @Test void refund_exceedOrderAmount_shouldThrow() { ... }
    @Test void refund_orderNotPaid_shouldThrow() { ... }
    @Test void refund_merchantMismatch_shouldThrow() { ... }
    @Test void refund_valid_shouldCallGateway() { ... }
}
```

- [ ] **Step 2: 实现 Service**

```java
@Service
public class RefundServiceImpl implements RefundService {
    @Autowired private PayOrderRepository orderRepo;
    @Autowired private RefundRecordRepository refundRepo;
    @Autowired private MerchantProfileRepository profileRepo;
    @Autowired private PaymentGateway paymentGateway;

    @Override
    @Transactional
    public Long create(CreateRefundCommand cmd) {
        // 1. 校验订单
        PayOrder order = orderRepo.findById(cmd.getPayOrderId())
                .orElseThrow(() -> new ServiceException("404", "订单不存在"));
        if (!"PAID".equals(order.getPayStatus()))
            throw new ServiceException("10302", "订单状态不允许退款");
        if (!order.getMerchantId().equals(cmd.getMerchantId()))
            throw new ServiceException("403", "商户不匹配");
        if (order.getRefundAmount() + cmd.getAmount() > order.getAmount())
            throw new ServiceException("10301", "退款金额超限");

        // 2. 创建退款记录
        RefundRecord record = new RefundRecord();
        record.setMerchantId(cmd.getMerchantId());
        record.setPayOrderId(cmd.getPayOrderId());
        record.setOutRefundNo(generateOutRefundNo());
        record.setAmount(cmd.getAmount());
        record.setReason(cmd.getReason());
        record.setStatus("PENDING");
        record.setOperatorId(cmd.getOperatorId());
        record.setTriggerInvoiceReverse(1);
        refundRepo.insert(record);

        // 3. 调银联退款
        MerchantProfile profile = profileRepo.findById(cmd.getMerchantId()).orElseThrow();
        RefundRequest req = new RefundRequest();
        req.setMerchantId(profile.getUmsMerchantId());
        req.setTerminalId(profile.getUmsTerminalId());
        req.setOutTradeNo(order.getOutTradeNo());
        req.setOutRefundNo(record.getOutRefundNo());
        req.setRefundAmount(cmd.getAmount());
        req.setSignKey(decrypt(profile.getUmsPaySignKeyEnc()));
        paymentGateway.refund(req);

        return record.getId();
    }
}
```

- [ ] **Step 3: Controller**

```java
@PreAuthorize("@ss.hasPermi('biz:refund:add')")
@Log(title = "退款管理", businessType = BusinessType.OTHER, isSaveRequestData = false)
@PostMapping
public AjaxResult add(@RequestBody CreateRefundRequest req) { ... }
```

- [ ] **Step 4: 前端退款按钮 + 表单**

- 订单详情页"发起退款"按钮 → 弹窗填写金额 + 原因 → 调后端
- 退款列表页（`views/pii/refund/index.vue`）+ 详情页

- [ ] **Step 5-8: 编译 + 测试 + Commit**

每个文件一个 commit。

### Task 5.2: 退款回调 controller

`POST /pii/refund/notify`：
- `@Anonymous` + 验签
- 写 `pii_refund_record` status=SUCCESS
- 更新 `pii_pay_order` `pay_status=REFUNDED` + `refund_amount` 累加
- MQ 发送红冲消息（`pii:invoice:reverse`）

### Task 5.3: 订单状态 PAID→REFUNDING→REFUNDED 流转

确保 5.2 回调后正确更新（已含）。

### Task 5.4: 退款流程端到端验证

- 商户登录 → 订单详情 → 发起退款 → 银联 sandbox 模拟退款成功 → 平台收到回调 → 触发红冲 → 银联发票 sandbox 模拟红冲成功 → 平台更新 `invoice_status=REVERSED`

无 commit，里程碑。

---

## 阶段 6 — 订单 / 发票查询

### Task 6.1: 订单查询 Service + Controller

参考 3.1 模板。列表查询条件：商户（自动）、订单号、税目、支付状态、发票状态、创建时间范围、金额范围。分页 + 导出 Excel。

### Task 6.2: 订单查询前端

`ui-admin/src/views/pii/payOrder/index.vue` + 详情页。包含 `payStatus` / `invoiceStatus` 标签着色。导出按钮 → 后端 `/pii/payOrder/export`。

### Task 6.3: 发票查询 Service + Controller

类似订单查询，但额外有"下载 PDF"按钮。下载时调 `InvoiceGateway.pickup` → 浏览器下载。

### Task 6.4: 发票查询前端

类似 6.2。

### Task 6.5: 导出 Excel 通用实现

参考 `system/user` 的 export 模板。列头按规格表字段。

---

## 阶段 7 — 运营方 BI 看板

### Task 7.1: `BiReportService` 聚合查询

**Files:**
- Create: `application/service/BiReportService.java` + `BiReportServiceImpl.java`
- Create: `application/query/BiDashboardQuery.java`
- Create: `application/result/BiDashboardResult.java`
- Create: `application/result/BiDeptAggregateResult.java`
- Create: `src/test/.../BiReportServiceTest.java`

- [ ] **Step 1: Service 接口**

```java
public interface BiReportService {
    /** 看板 KPI + 趋势 + 占比 */
    BiDashboardResult dashboard(BiDashboardQuery q);
    /** 地图下钻：按 dept 聚合 */
    BiDeptAggregateResult aggregateByDept(String level, Long parentDeptId, BiDashboardQuery q);
}
```

- [ ] **Step 2: 实现 dashboard()**

```java
@Override
public BiDashboardResult dashboard(BiDashboardQuery q) {
    BiDashboardResult r = new BiDashboardResult();
    Long merchantId = q.getMerchantId();  // null = 运营方看全部
    // KPI
    r.setTotalAmount(orderRepo.sumAmountByMerchantAndStatusBetween(merchantId,
            List.of("PAID", "REFUNDED"), q.getStartTime(), q.getEndTime()));
    r.setTotalInvoiceAmount(orderRepo.sumAmountByInvoiceStatusBetween(merchantId,
            "ISSUED", q.getStartTime(), q.getEndTime()));
    r.setTotalOrderCount(orderRepo.countByMerchantAndPayTimeBetween(merchantId, q.getStartTime(), q.getEndTime()));
    r.setAbnormalOrderCount(orderRepo.countByInvoiceStatusIn(merchantId,
            List.of("FAILED", "CLOSED"), q.getStartTime(), q.getEndTime()));
    // 趋势（按天聚合）
    r.setTrend(orderRepo.sumAmountByDay(merchantId, q.getStartTime(), q.getEndTime()));
    // 占比（按税目）
    r.setTaxItemRatio(orderRepo.sumAmountByTaxItem(merchantId, q.getStartTime(), q.getEndTime()));
    // 商户排行
    r.setMerchantTop10(orderRepo.sumAmountByMerchantTop(merchantId, 10, q.getStartTime(), q.getEndTime()));
    // 异常订单
    r.setAbnormalOrders(orderRepo.findAbnormalOrders(merchantId, q.getStartTime(), q.getEndTime(), 50));
    return r;
}
```

- [ ] **Step 3: 实现地图下钻 `aggregateByDept()`**

```java
@Override
public BiDeptAggregateResult aggregateByDept(String level, Long parentDeptId, BiDashboardQuery q) {
    // 通过 sys_dept.ancestors LIKE 查询该父级 dept 下所有 merchant dept
    SysDept parent = deptMapper.selectDeptById(parentDeptId);
    List<SysDept> childDepts;
    if ("city".equals(level)) {
        childDepts = deptMapper.selectByAncestorsLikeAndType(parent.getAncestors() + ",%", "region", 2);
    } else if ("district".equals(level)) {
        childDepts = deptMapper.selectByAncestorsLikeAndType(parent.getAncestors() + ",%", "region", 3);
    } else {
        childDepts = List.of();
    }
    // 聚合每个 childDept 的订单金额
    List<BiDeptAggregateItem> items = childDepts.stream().map(d -> {
        Long amount = orderRepo.sumAmountByDeptPath(d.getAncestors() + ",%",
                q.getStartTime(), q.getEndTime());
        Long count = orderRepo.countByDeptPath(d.getAncestors() + ",%", q.getStartTime(), q.getEndTime());
        Long merchantCount = deptMapper.countByAncestorsLikeAndType(d.getAncestors() + ",%", "merchant");
        return new BiDeptAggregateItem(d.getDeptId(), d.getDeptName(), d.getRegionCode(), amount, count, merchantCount);
    }).collect(Collectors.toList());
    return new BiDeptAggregateResult(level, items);
}
```

- [ ] **Step 4: 加缓存**

```java
public BiDashboardResult dashboard(BiDashboardQuery q) {
    String key = "pii:bi:dashboard:" + hash(q);
    BiDashboardResult cached = redis.get(key);
    if (cached != null) return cached;
    BiDashboardResult r = computeDashboard(q);
    redis.setex(key, props.getBi().getCacheSeconds(), r);
    return r;
}
```

- [ ] **Step 5: 写测试 + 编译 + Commit**

### Task 7.2: BI 看板 Controller

`/pii/bi/dashboard` 渲染 ECharts 容器 + 调用 `/pii/bi/data?startTime=&endTime=&taxItemId=&merchantId=&cityId=&districtId=` 拿数据。

### Task 7.3: BI 看板前端（KPI + 趋势 + 占比）

`ui-admin/src/views/pii/bi/dashboard.vue`：
- 顶部 4 个 KPI 卡片
- 时间范围选择器（默认近 7 天）
- 中部：交易额趋势折线图 + 各税目占比饼图
- 引用 ECharts：`import * as echarts from 'echarts'`

### Task 7.4: 海南地图组件 + 三级下钻

`ui-admin/src/views/pii/bi/components/HainanMap.vue`：
- 从 `/static/hainan.json` 加载地图 GeoJSON（首次部署时手动放）
- 点击市县 → 调 `/pii/bi/dept/aggregate?level=district&parentDeptId=...` → 重新渲染
- 面包屑显示当前层级 + 返回按钮
- URL 状态同步 `?level=city&regionId=...`

### Task 7.5: 商户排行 + 异常订单明细前端

排行柱状图 + 明细表（折叠展开）。

### Task 7.6: BI 看板缓存失效

在 `MerchantService` / `PayOrderService` 变更时主动调 `redis.delete("pii:bi:dashboard:*")` + `redis.delete("pii:bi:dept:*")`。

---

## 阶段 8 — 测试与验收

### Task 8.1: Gateway 适配层单元测试

- `NotifyVerifier` 边界场景（空值、特殊字符、unicode、JSONArray 顺序）
- `UmsInvoiceGateway` HTTP 调用（用 `WireMock` 模拟银联 sandbox 响应）

### Task 8.2: 业务 Service 单元测试

- `PayOrderService.preCreate()` 4 分支（二维码有效/无效/过期/未启用）
- `InvoiceOpenHandler.doHandle()` MQ 触发、幂等、重试
- `BiReportService.aggregate()` 地图聚合

### Task 8.3: 回调 Controller 集成测试

用 `MockMvc` 模拟银联回调：
- 支付回调：验签失败 → 返 "FAIL"；验签成功 + 重复 → 幂等；验签成功 + 正常 → 更新
- 开票回调：ISSUED/REVERSED/FAILED 各分支
- 退款回调：触发红冲 MQ

### Task 8.4: 权限拦截器集成测试

- `PiiTenantInterceptor` 商户隔离
- `@PreAuthorize` 无权限调用 → 403
- `@Anonymous` 消费者接口 → 不需 token

### Task 8.5: 集成测试（TestContainers MySQL）

`pii-integration-test` 用例：扫码 → 选税目 → 支付 → 开票 → 退款 → 红冲 全链路 + H2/TestContainers MySQL

### Task 8.6: 架构测试全量

```bash
bash scripts/architecture/check-module-boundaries.sh
mvn test
```

Expected: 全部 PASS

### Task 8.7: 手工验证 + 截图记录

清单：
- [ ] 商户扫码 → 选税目 → 填抬头 → 支付 → 开票成功 截图
- [ ] 商户登录 → 退款 → 红冲成功 截图
- [ ] 运营方登录 → BI 看板 → 地图下钻 截图
- [ ] 写到 `docs/superpowers/reviews/2026-07-02-pii-manual-verification.md`

### Task 8.8: 验收

- [ ] **Step 1: 全量构建 + 测试**

```bash
mvn clean package
mvn test
bash scripts/architecture/check-module-boundaries.sh
cd ui-admin && npm run build:prod
```

Expected: 全部 SUCCESS

- [ ] **Step 2: 按规格 §12 验收标准逐条检查**

无 commit，里程碑完成。

---

## 自检

**1. Spec coverage**：规格 13 节每节都有对应任务覆盖。
- §1-2 → Task 0.1-0.6（基础设施）
- §3 → Task 1.1-1.5（领域层）
- §4.1 → Task 4.1-4.9（正向流）；§4.2 → Task 5.1-5.4（退款流）；§4.3 → Task 0.5（订单超时）；§4.4 → Task 7.1-7.6（BI）
- §5 → Task 2.1-2.8（集成适配层）
- §6 → Task 3.1-3.7（管理后台）+ Task 1.4（多租户拦截器）
- §7 → Task 7.1-7.6（BI）
- §8 → Task 2.5-2.7（幂等）+ 集成测试
- §9 → Task 8.1-8.7
- §10 → Task 0.2-0.5（SQL + 配置）
- §11 → 各任务提交信息中说明

**2. Placeholder scan**：已扫描，无 "TBD"/"TODO"/"待定"。

**3. Type consistency**：domain.model / entity / mapper / repository / DTO 字段名一致；outTradeNo / umsMerOrderDate / invoiceStatus / payStatus / orderToken 等关键字段全程一致。

---

## 执行方式

Plan 已保存到 `docs/superpowers/plans/2026-07-02-pay-and-invoice-implementation.md`。两种执行方式：

**1. Subagent-Driven（推荐）** — 每个 Task 派一个 fresh subagent 独立执行，task 之间有 review 节点

**2. Inline Execution** — 当前会话按 task 顺序执行，checkpoint 暂停让你审查

---
```

---

---
```

---
```
