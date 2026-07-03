# 支付即开票（Pay & Invoice）平台 — 设计规格

- **版本**：v1.0
- **日期**：2026-07-02
- **状态**：待用户审查
- **模块名**：`manzhushaka-biz-pii`
- **表前缀**：`pii_`
- **业务目标**：消费者扫描商户支付二维码 → 在公众号 H5 内选择税目 + 输入金额 + 填写抬头 → 调起银联公众号支付 → 支付回调成功后由平台自动调用银联发票服务平台（E开票）开具电子发票 → 消费者下载 PDF；支持运营方全局 BI 看板；商户可发起退款触发红冲。

---

## 1. 项目背景与约束

### 1.1 角色

| 角色 | v1 范围 |
| --- | --- |
| **运营方管理人员** (`operator`) | 全功能：商户管理、税目管理、二维码总览、订单查询、退款管理、所有商户开票查询、运营全局 BI 看板 |
| **商户** (`merchant`) | 本商户后台：商户配置（支付参数 + 开票参数）、二维码管理、本商户订单查询、退款发起、本商户开票查询 |
| **海南各市县税务局** (`tax_bureau_city`) | 账号体系 + 登录预留，**v1 无独立页面**（v2 BI dashboard 用） |
| **海南税务局** (`tax_bureau_province`) | 同上 |
| **消费者** | 扫码匿名访问 H5，无登录 |

### 1.2 核心约束（来自用户确认）

1. **集成通道**：
   - 支付：银联商务公众号支付（`com.chinaums:open-pay:1.0.0`，`OfficialAccountsPayUtil`）
   - 开票：银联商务发票服务平台（E开票），自研 HTTP + 银联签名（无现成 Java SDK）
2. **公众号配置**：平台统一公众号参数（`app_id` / `app_secret` / `pay_sign_key`）；每商户独立银联商户号 + 终端号
3. **多租户隔离**：中小量级，**单库逻辑隔离** + `merchant_id` + MyBatis 拦截器
4. **税目**：平台运营统一维护，**全局共享**，商户只能选用不能自创
5. **消费者抬头**：抬头**必填** + 税号**选填** + 邮箱**选填** + 手机号**选填**
6. **红冲**：**不开独立流程**，由退款/交易撤销/超时关闭触发，**退款成功后**才调银联红冲接口（避免退款失败但红冲先走的资损风险）
7. **BI**：v1 仅交付**运营方全局看板**；商户自助报表与税局 dashboard 留 v2
8. **退款发起**：仅**商户手动发起**；银联退款异步回调后触发红冲
9. **消费者前端**：放在 `ui-admin/src/views/pay/` 目录，`@Anonymous` 匿名访问，**不另起工程**
10. **模块边界**：新建独立业务模块 `manzhushaka-biz-pii`，命名沿用 `manzhushaka-biz-XXX` 规则

---

## 2. 模块结构与依赖

### 2.1 Maven 模块依赖图

```
manzhushaka-common         (不引用)
       ↑
manzhushaka-framework      (不引用)
       ↑
manzhushaka-system         (复用用户/角色/部门/字典/参数/MQ/操作日志)
       ↑
manzhushaka-quartz         (定时任务)
       ↑
manzhushaka-biz-pii        ★ 新增
       ↑
manzhushaka-admin          (新增本业务 Controller/DTO/VO)
```

### 2.2 `manzhushaka-biz-pii` 内部包结构

```
com.manzhushaka.biz.pii
├── domain
│   ├── model           # 领域模型
│   ├── repository      # 仓储接口
│   └── vo              # 业务领域 VO
├── application
│   ├── command         # 入参命令
│   ├── query           # 查询条件
│   ├── result          # 出参结果
│   └── service         # 业务用例
└── infrastructure
    ├── persistence     # MyBatis Entity + Mapper + Converter
    ├── gateway         # 网关层接口 + 实现
    │   ├── pay         # PaymentGateway + UmsMpPaymentGateway
    │   ├── invoice     # InvoiceGateway + UmsInvoiceGateway
    │   └── wechat      # WechatOAuthGateway + UmsWechatOAuthGateway
    └── config          # 业务配置 Bean
```

### 2.3 `manzhushaka-admin` 新增

```
com.manzhushaka.web.controller.pii    # 商家/运营方/消费者（匿名）Controller
com.manzhushaka.web.dto.pii           # HTTP 入参
com.manzhushaka.web.vo.pii            # HTTP 出参
```

### 2.4 商户账号模型

- 商户 = `sys_dept`（`dept_type='merchant'`）的子节点
- 商户账号 = `sys_user`（`dept_id` 指向该商户 dept）
- 商户档案 = `pii_merchant_profile`，与 `sys_dept.id` 一对一
- 不自建账号体系

### 2.5 复用项

- 用户/角色/部门/字典/参数 → `manzhushaka-system`
- MQ（Redis Stream）基础设施 → `manzhushaka-framework`
- 异常/敏感字段加密/SQL 注入防护/CSRF/外部重定向白名单 → `manzhushaka-common` + `framework`
- 操作日志 → 通过 `@Log` 注解复用

---

## 3. 领域模型

### 3.1 表前缀与公共约定

- 表前缀：`pii_`
- 金额：`BIGINT` 存"分"（整型最小货币单位），`BigDecimal` 比较用 `compareTo`
- 逻辑删除：`del_flag TINYINT DEFAULT 0`
- 时间字段：`create_time DATETIME / update_time DATETIME`
- 创建/更新人：`create_by / update_by BIGINT` 引用 `sys_user.id`
- 业务表均带 `merchant_id BIGINT NULL`（平台级数据为 NULL）
- `toString()` 截断 `payload` / `password` / `token` / `last_error_msg` 等敏感/超长字段
- 布尔属性不加 `is` 前缀，数据库字段加 `is_` 前缀并在 `resultMap` 映射

### 3.2 数据表清单

#### 3.2.1 `sys_dept` 扩展（不是新表）

新增 3 字段，迁移现有数据 `dept_type='platform_org'`，行政区划与商户作为不同类型节点挂在同一棵部门树：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `dept_type` | `VARCHAR(16)` NOT NULL DEFAULT `'platform_org'` | `platform_org`（运营组织）/ `region`（行政区划）/ `merchant`（商户主体） |
| `region_code` | `VARCHAR(6)` NULL | 行政区划代码（`dept_type='region'` 时填，6 位数字，如 `460106`） |
| `region_level` | `TINYINT` NULL | 1=省 / 2=市县 / 3=区/镇（`dept_type='region'` 时填） |

#### 3.2.2 平台级表（运营方维护）

**`pii_platform_wechat_config`**（1 行）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `app_id` | VARCHAR(32) NOT NULL | 微信公众号 AppID |
| `app_secret_enc` | VARCHAR(512) NOT NULL | AES 加密存储 |
| `pay_sign_key_enc` | VARCHAR(512) NOT NULL | 公众号支付签名密钥，加密 |
| `token` | VARCHAR(64) | 公众号 token |
| `aes_key_enc` | VARCHAR(512) | 公众号消息加密 AESKey，加密 |
| `status` | TINYINT NOT NULL DEFAULT 1 | 启用/停用 |
| `create_time` / `update_time` | DATETIME | |
| `create_by` / `update_by` | BIGINT | |

**`pii_tax_item`**（全局税目）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `tax_item_code` | VARCHAR(19) NOT NULL UNIQUE | 税收分类编码（19 位） |
| `name` | VARCHAR(128) NOT NULL | 商品/服务名称 |
| `brevity_code` | VARCHAR(32) | 商品简码 |
| `category` | VARCHAR(64) | 商品分类 |
| `tax_rate` | DECIMAL(5,2) NOT NULL | 税率（如 6、13） |
| `vat_special` | VARCHAR(64) | 增值税特殊管理 |
| `free_tax_type` | VARCHAR(1) | 免税类型（空/0/1/2/3） |
| `prefer_policy_flag` | VARCHAR(1) | 是否使用优惠政策 |
| `sort` | INT DEFAULT 0 | 排序 |
| `status` | TINYINT NOT NULL DEFAULT 1 | |
| `remark` | VARCHAR(255) | |
| `create_time` / `update_time` | DATETIME | |
| `create_by` / `update_by` | BIGINT | |

#### 3.2.3 商户级表（按 `merchant_id` 隔离）

**`pii_merchant_profile`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `dept_id` | BIGINT NOT NULL UNIQUE | 关联 `sys_dept.id`（`dept_type='merchant'`） |
| `merchant_name` | VARCHAR(128) NOT NULL | 商户主体名（开票用） |
| `ums_merchant_id` | VARCHAR(15) NOT NULL | 银联商户号 |
| `ums_terminal_id` | VARCHAR(8) NOT NULL | 银联终端号 |
| `ums_pay_sign_key_enc` | VARCHAR(512) NOT NULL | 公众号支付签名密钥，加密 |
| `ums_invoice_sign_key_enc` | VARCHAR(512) NOT NULL | 银联发票签名密钥，加密 |
| `invoice_msg_src` | VARCHAR(32) NOT NULL | 发票接口消息来源 |
| `invoice_seller_name` | VARCHAR(128) NOT NULL | 卖方名称 |
| `invoice_seller_tax_code` | VARCHAR(32) NOT NULL | 卖方纳税人识别号 |
| `invoice_seller_address` | VARCHAR(128) | 卖方地址 |
| `invoice_seller_telephone` | VARCHAR(16) | 卖方电话 |
| `invoice_seller_bank` | VARCHAR(64) | 卖方开户行 |
| `invoice_seller_account` | VARCHAR(32) | 卖方账号 |
| `invoice_payee` | VARCHAR(8) | 收款人 |
| `invoice_checker` | VARCHAR(8) | 复核人 |
| `invoice_drawer` | VARCHAR(8) | 开票人 |
| `notify_url` | VARCHAR(256) NOT NULL | 开票回调地址（必须 80/443 端口） |
| `status` | TINYINT NOT NULL DEFAULT 1 | |
| `remark` | VARCHAR(255) | |
| `create_time` / `update_time` | DATETIME | |
| `create_by` / `update_by` | BIGINT | |

**`pii_pay_qrcode`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `merchant_id` | BIGINT NOT NULL | |
| `qrcode_code` | VARCHAR(32) NOT NULL UNIQUE | 二维码业务编码 |
| `qrcode_url` | VARCHAR(512) NOT NULL | H5 入口 URL |
| `qrcode_image_url` | VARCHAR(512) | 二维码图片（base64 或 OSS URL） |
| `name` | VARCHAR(64) NOT NULL | 二维码名称 |
| `status` | TINYINT NOT NULL DEFAULT 1 | |
| `expire_time` | DATETIME NULL | 失效时间 |
| `remark` | VARCHAR(255) | |
| `create_time` / `update_time` | DATETIME | |
| `create_by` / `update_by` | BIGINT | |

**`pii_pay_qrcode_tax_item`**（多对多）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `qrcode_id` | BIGINT NOT NULL | |
| `tax_item_id` | BIGINT NOT NULL | |
| `default_amount` | BIGINT NULL | 默认金额（分），可空 |
| UNIQUE | (`qrcode_id`, `tax_item_id`) | |

**`pii_pay_order`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `merchant_id` | BIGINT NOT NULL | |
| `qrcode_id` | BIGINT NOT NULL | |
| `tax_item_id` | BIGINT NOT NULL | |
| `out_trade_no` | VARCHAR(32) NOT NULL UNIQUE | 本系统订单号（32 位，复用为银联 `merOrderId`） |
| `ums_mer_order_date` | VARCHAR(8) NOT NULL | yyyyMMdd |
| `amount` | BIGINT NOT NULL | 含税总金额（分） |
| `buyer_name` | VARCHAR(128) NOT NULL | 抬头 |
| `buyer_tax_code` | VARCHAR(32) | 税号 |
| `buyer_email` | VARCHAR(64) | 邮箱 |
| `buyer_mobile` | VARCHAR(16) | 手机号 |
| `buyer_openid` | VARCHAR(64) | 公众号 openId |
| `pay_status` | VARCHAR(16) NOT NULL DEFAULT 'PENDING' | `PENDING` / `PAID` / `REFUNDING` / `REFUNDED` / `CLOSED` |
| `pay_time` | DATETIME | 支付成功时间 |
| `pay_trade_no` | VARCHAR(64) | 银联支付流水号 |
| `pay_notify_status` | VARCHAR(16) | `NOT` / `OK` / `FAILED` |
| `refund_amount` | BIGINT NOT NULL DEFAULT 0 | 累计退款金额（分） |
| `invoice_status` | VARCHAR(16) NOT NULL DEFAULT 'NONE' | `NONE` / `PENDING` / `ISSUING` / `ISSUED` / `REVERSING` / `REVERSED` / `FAILED` |
| `invoice_no` | VARCHAR(20) | 发票号码 |
| `invoice_code` | VARCHAR(20) | 发票代码 |
| `invoice_pdf_url` | VARCHAR(256) | 发票 PDF 链接 |
| `invoice_issue_time` | DATETIME | 开票时间 |
| `invoice_reverse_time` | DATETIME | 红冲时间 |
| `order_token` | VARCHAR(32) NOT NULL | 消费者匿名查询短 token |
| `wechat_appid` | VARCHAR(32) | 公众号 AppID（冗余便于查询） |
| `client_ip` | VARCHAR(64) | 消费者 IP |
| `remark` | VARCHAR(255) | |
| `create_time` / `update_time` | DATETIME | |
| `create_by` / `update_by` | BIGINT | |

**`pii_refund_record`**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `merchant_id` | BIGINT NOT NULL | |
| `pay_order_id` | BIGINT NOT NULL | |
| `out_refund_no` | VARCHAR(32) NOT NULL UNIQUE | 本系统退款单号 |
| `ums_refund_mer_order_id` | VARCHAR(32) | 银联退款订单号 |
| `amount` | BIGINT NOT NULL | 退款金额（分） |
| `reason` | VARCHAR(255) | 退款原因 |
| `status` | VARCHAR(16) NOT NULL DEFAULT 'PENDING' | `PENDING` / `SUCCESS` / `FAILED` |
| `ums_trade_no` | VARCHAR(64) | 银联退款流水号 |
| `complete_time` | DATETIME | 完成时间 |
| `operator_id` | BIGINT NOT NULL | 发起人 sys_user.id |
| `trigger_invoice_reverse` | TINYINT NOT NULL DEFAULT 1 | 是否触发红冲（默认 1） |
| `create_time` / `update_time` | DATETIME | |

**`pii_payment_notify_log`**（支付回调幂等）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `out_trade_no` | VARCHAR(32) NOT NULL UNIQUE | |
| `notify_payload` | JSON NOT NULL | |
| `sign` | VARCHAR(128) | |
| `verify_result` | TINYINT NOT NULL | 1=通过 / 0=失败 |
| `processed` | TINYINT NOT NULL DEFAULT 0 | |
| `created_at` | DATETIME | |

**`pii_invoice_notify_log`**（开票回调幂等）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `ums_mer_order_id` | VARCHAR(32) NOT NULL | |
| `ums_mer_order_date` | VARCHAR(8) NOT NULL | |
| UNIQUE | (`ums_mer_order_id`, `ums_mer_order_date`) | |
| `qrcode_id` | VARCHAR(40) | 银联 `qrCodeId` |
| `notify_payload` | JSON NOT NULL | |
| `sign` | VARCHAR(128) | |
| `verify_result` | TINYINT NOT NULL | |
| `processed` | TINYINT NOT NULL DEFAULT 0 | |
| `created_at` | DATETIME | |

**`pii_invoice_call_log`**（银联发票接口调用日志，可观测 + 重放）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | |
| `pay_order_id` | BIGINT NOT NULL | |
| `msg_type` | VARCHAR(32) NOT NULL | `issue` / `reverse` / `query` / `pickup` |
| `msg_id` | VARCHAR(64) NOT NULL | |
| `request_body` | JSON | |
| `response_body` | JSON | |
| `duration_ms` | INT | |
| `is_success` | TINYINT NOT NULL | |
| `error_msg` | VARCHAR(512) | |
| UNIQUE | (`pay_order_id`, `msg_type`) | 同一订单同一类型只发一次 |
| `created_at` | DATETIME | |

### 3.3 幂等保证（三层）

| 层 | 机制 |
| --- | --- |
| HTTP 网关 | 回调 controller `@Anonymous` + 银联验签 + `pii_payment_notify_log` / `pii_invoice_notify_log` 唯一索引 |
| MQ handler | 框架 `AbstractRedisStreamMessageHandler`，`idempotentKey()` 返回 `payOrderId:msgType` |
| 外部接口 | 银联 `complex.issue` / `complex.reverse` 按 `merOrderId+merOrderDate` 幂等，HTTP 层不重试，由 MQ 重试 |

### 3.4 多租户隔离

- `PiiTenantInterceptor`：从登录用户 `dept_id`（若是 `dept_type='merchant'`）取出 `merchant_id`，自动注入 `WHERE merchant_id = ?`
- `sys_role.data_scope` 机制叠加：商户角色（`data_scope=4`）只看本部门及以下数据；运营方（`data_scope=1`）看全部
- 平台级表（`pii_tax_item` / `pii_platform_wechat_config`）不加 `merchant_id`

---

## 4. 核心业务流程

### 4.1 流程 A：消费者扫码 → 公众号支付 → 自动开票（核心正向流）

```
消费者               平台 H5 (匿名)         平台后端                银联公众号支付          银联发票平台            消费者邮箱
  │                      │                    │                       │                    │                  │
  │  1.微信扫码 ───────► │                    │                       │                    │                  │
  │                      │ 2.加载页面拉配置    │                       │                    │                  │
  │                      │ ── GET /anon/pii/qrcode/{code} ─►│     │                    │                  │
  │                      │ ◄── 税目列表 + AppID ───────────│     │                    │                  │
  │  3.选税目             │                    │                       │                    │                  │
  │  4.填金额             │                    │                       │                    │                  │
  │  5.填抬头 (必填)      │                    │                       │                    │                  │
  │    +税号 (选填)       │                    │                       │                    │                  │
  │    +邮箱 (选填)       │                    │                       │                    │                  │
  │    +手机号 (选填)      │                    │                       │                    │                  │
  │  6.提交下单 ─────────►│                    │                       │                    │                  │
  │                      │ POST /anon/pii/pay/precreate           │                    │                  │
  │                      │  (taxItemId, amount, buyerName,       │                    │                  │
  │                      │   buyerTaxCode?, buyerEmail?,          │                    │                  │
  │                      │   buyerMobile?)      │                │                    │                  │
  │                      │                    │ 7.创建 pii_pay_order   │                    │                  │
  │                      │                    │  (pay_status=PENDING,  │                    │                  │
  │                      │                    │   invoice_status=NONE, │                    │                  │
  │                      │                    │   buyer_info 已落库)   │                    │                  │
  │                      │                    │ 8.调公众号支付预下单   │                    │                  │
  │                      │                    │ ─── PaymentGateway.preCreate(jsPayRequest) ─►│                  │
  │                      │ ◄── prepay_id ──── │                       │                    │                  │
  │  9.唤起微信支付       │                    │                       │                    │                  │
  │ ── WeixinJSBridge ──►│                    │                       │                    │                  │
  │     .invoke('getBrandWCPayRequest')       │                       │                    │                  │
  │                      │                    │                       │                    │                  │
  │                      │                    │ 10.银联支付回调 ────►  │                    │                  │
  │                      │                    │ POST /pii/pay/notify  │                    │                  │
  │                      │                    │ 验签+幂等+更新 PAID    │                    │                  │
  │                      │                    │ 返纯文本 SUCCESS      │                    │                  │
  │                      │                    │                       │                    │                  │
  │                      │                    │ 11.MQ 异步调开票(抬头/邮箱已在订单中)               │
  │                      │                    │ ─── InvoiceGateway.invoice(payload) ──────────►│                  │
  │                      │                    │                       │                    │ 受理开票         │
  │                      │                    │                       │                    │                  │
  │                      │                    │ 12.发票平台回调开票结果                     │                  │
  │                      │                    │ POST /pii/invoice/notify ◄──────────────── │                  │
  │                      │                    │ 验签+幂等+更新 ISSUED  │                    │                  │
  │                      │                    │ 返 SUCCESS ──────────────────────────────►│                  │
  │                      │                    │                       │                    │                  │
  │                      │                    │ 13.(若填了邮箱)MQ 异步推送邮件          │                  │
  │                      │                    │ ─── EmailService.sendInvoiceMail ───────►│                  │
  │                      │                    │                       │                    │                  │
  │  14.前端轮询查订单    │                    │                       │                    │                  │
  │  GET /anon/pii/order/{no}?token=xxx       │                       │                    │                  │
  │                      │ ─── 轮询 ────────► │                       │                    │                  │
  │                      │ ◄── 订单状态 ───── │                       │                    │                  │
  │                      │                    │                       │                    │                  │
  │  15.下载 PDF 发票     │                    │                       │                    │                  │
  │  GET /anon/pii/invoice/{no}/download      │                       │                    │                  │
  │                      │ ─── 调 complex.pickup ────────────────────────────────────►│                  │
  │                      │ ◄── PDF base64 ────────────────────────────────────────── │                  │
  │  16.浏览器下载 PDF   │                    │                       │                    │                  │
```

**关键设计点**：
1. 支付回调成功后**异步**调开票（MQ 解耦），避免回调阻塞
2. 开票结果以**回调为主**，状态查询为辅（与银联文档建议一致）
3. 抬头/税号/邮箱/手机号在订单创建时落库，开票接口直接读
4. 邮箱/手机号可选，填了才发邮件/短信
5. 消费者匿名查询通过 `order_token` 二次校验

### 4.2 流程 B：商户退款 → 触发红冲

```
商户                 平台后台(登录)             平台后端                银联公众号支付          银联发票平台
 │                       │                       │                       │                    │
 │ 1.进"开票查询"页       │                       │                       │                    │
 │ 2.点退款 ───────────► │                       │                       │                    │
 │ 3.填金额+原因          │                       │                       │                    │
 │                       │ POST /pii/refund ──► │                       │                    │
 │                       │                       │ 4.校验订单状态=PAID    │                    │
 │                       │                       │ 5.调公众号退款 API     │                    │
 │                       │                       │ ─── PaymentGateway.refund ─►│                  │
 │                       │                       │                       │                    │
 │                       │                       │ 6.银联支付异步回调 ────►│                    │
 │                       │                       │ POST /pii/refund/notify│                    │
 │                       │                       │ 验签+幂等+更新 refund=SUCCESS                 │
 │                       │                       │ 7.订单状态 PAID→REFUNDED                    │
 │                       │                       │ 8.MQ 异步调红冲 ───────────────────────────► │
 │                       │                       │ ─── InvoiceGateway.reverse(payload) ─────►│
 │                       │                       │                       │                    │ 受理红冲
 │                       │                       │                       │                    │ status=REVERSING
 │                       │                       │                       │                    │
 │                       │                       │ 9.开票平台回调红冲结果 ──────────────────────►│
 │                       │                       │ POST /pii/invoice/notify                    │
 │                       │                       │ 验签+幂等+更新 invoice_status=REVERSED     │
 │                       │                       │ 返 SUCCESS ──────────────────────────────► │
```

### 4.3 流程 C：消费者超时未支付 → 自动关闭订单

- quartz job `PiiPayOrderExpireJob`，cron `0 */5 * * * ?` 每 5 分钟扫描
- 关闭条件：`pay_status='PENDING' AND create_time < NOW() - INTERVAL 30 MINUTE`
- 关闭动作：`pay_status='CLOSED'`，不发开票

### 4.4 流程 D：BI 数据采集

- 不建独立数仓，直接基于业务库查询
- 运营方全局看板查询走独立 `BiReportService`，**不走操作日志**，**5 分钟 Redis 缓存**
- 数据权限自动叠加（运营方 `data_scope=1` = 全部；商户 `data_scope=4` = 本部门及以下）

---

## 5. 集成适配层

### 5.1 接口与实现清单

```
gateway/
├── pay/
│   ├── PaymentGateway.java                # 接口
│   ├── UmsMpPaymentGateway.java           # 银联公众号支付实现（注入 OfficialAccountsPayUtil）
│   ├── MockPaymentGateway.java            # MOCK 实现
│   └── dto/{PreCreateRequest, PreCreateResponse, RefundRequest, RefundResponse, NotifyPayload, NotifyVerifyResult}
├── invoice/
│   ├── InvoiceGateway.java                # 接口
│   ├── UmsInvoiceGateway.java             # 银联发票平台实现（自研 HTTP + SHA-256 签名）
│   ├── MockInvoiceGateway.java            # MOCK 实现
│   ├── dto/{InvoiceRequest, InvoiceResponse, ReverseRequest, ReverseResponse, QueryRequest, QueryResponse, PickupRequest, PickupResponse, NotifyPayload, NotifyVerifyResult}
│   └── handler/{InvoiceOpenHandler, InvoiceReverseHandler}  # MQ handler
├── wechat/
│   ├── WechatOAuthGateway.java
│   ├── UmsWechatOAuthGateway.java
│   └── dto/{OAuthExchangeRequest, OAuthExchangeResponse}
└── NotifyVerifier.java                    # 银联字典序签名工具
```

### 5.2 银联发票平台适配（重点）

- **无现成 Java SDK**，自研 HTTP 客户端（Apache HttpClient 5）
- 报文：HTTPS + JSON
- 签名算法（按文档 §2.2）：
  1. 集合 M 非空参数按 ASCII 字典序排序
  2. 拼接 `key1=value1&key2=value2`
  3. 末尾追加 `&key=<密钥>`
  4. SHA-256 哈希 → 转为大写 = `sign`
- JSON 子域（`goodsDetail` 等）按 `json-lib` 序列化（**FastJSON 会重排字段，签名会错**）
- 超时：连接 5s、读 15s
- HTTP 层**不重试**，由 MQ 层重试（避免重复开票）

### 5.3 银联公众号支付适配

- 依赖 `com.chinaums:open-pay:1.0.0`
- 使用 `OfficialAccountsPayUtil` 完成 `preCreate` / `refund` / `callback` 验签
- 默认商户号 `898201612345678`（文档），但本项目每商户独立 `ums_merchant_id + ums_terminal_id`，**从 `pii_merchant_profile` 读取后传入**

### 5.4 Mock 模式

- 通过 `pii.mode=REAL|MOCK` 配置项控制
- `MOCK` 时所有 Gateway 返回预设成功响应，不发真实 HTTP
- 单元测试强制 `MOCK`；集成测试可切 `REAL` 接银联 sandbox

### 5.5 调用入口映射

| 场景 | Service / Handler | Gateway |
| --- | --- | --- |
| 公众号支付预下单 | `PayOrderService.preCreate` | `PaymentGateway.preCreate` |
| 公众号支付回调验签 | `PayNotifyController` | `PaymentGateway.verifyAndParse` |
| 公众号退款 | `RefundService.create` | `PaymentGateway.refund` |
| 公众号退款回调验签 | `RefundNotifyController` | `PaymentGateway.verifyAndParse` |
| 直接开票 | `InvoiceOpenHandler.doHandle` (MQ) | `InvoiceGateway.invoice` |
| 红冲 | `InvoiceReverseHandler.doHandle` (MQ) | `InvoiceGateway.reverse` |
| 开票回调验签 | `InvoiceNotifyController` | `InvoiceGateway.verifyAndParse` |
| 发票状态查询 | `InvoiceService.query` | `InvoiceGateway.query` |
| 下载版式文件 | `InvoiceService.pickup` | `InvoiceGateway.pickup` |
| 微信公众号 OAuth | `WechatOAuthService.exchangeOpenId` | `WechatOAuthGateway.exchangeOpenId` |

### 5.6 配置 Bean

`PiiGatewayConfig`（`manzhushaka-biz-pii/infrastructure/config`）：

```java
@Bean PaymentGateway paymentGateway(@Value("${pii.mode}") String mode, ...) {
    return "MOCK".equals(mode) ? new MockPaymentGateway(...) : new UmsMpPaymentGateway(...);
}
// InvoiceGateway / WechatOAuthGateway 同理
```

### 5.7 Maven 依赖

`manzhushaka-biz-pii/pom.xml` 新增：

```xml
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
```

---

## 6. 权限模型

### 6.1 角色与数据范围

| 角色编码 | 角色名 | `sys_role.data_scope` | 备注 |
| --- | --- | --- | --- |
| `operator` | 运营方管理人员 | `1`（所有数据权限） | 全功能 |
| `merchant` | 商户 | `4`（本部门及以下数据权限） | 通过 `dept_id` 关联 `pii_merchant_profile.dept_id` 自动限定；v2 支持子账号时仍生效 |
| `tax_bureau_city` | 海南各市县税务局 | `3`（本部门数据权限） | 账号体系预留，v1 无页面 |
| `tax_bureau_province` | 海南税务局 | `4`（本部门及以下数据权限） | 同上，覆盖市县 |

### 6.2 菜单结构（v1 新增）

新一级菜单 `pii`（支付即开票），与 `system` / `monitor` 平级。

```
"支付即开票" [M, path=pii]
├── "商户管理" [M]
│   ├── "商户列表" [C]         biz:merchant:list
│   ├── "商户参数配置" [C]      biz:merchant:config
│   └── 按钮 [F]: add/edit/remove/changeStatus
├── "税目管理" [M]
│   ├── "税目列表" [C]         biz:taxItem:list
│   └── 按钮 [F]: add/edit/remove/changeStatus
├── "支付二维码" [M]
│   ├── "二维码总览" [C]       biz:qrcode:list
│   ├── "二维码详情" [C]       biz:qrcode:query
│   └── 按钮 [F]: add/edit/remove/changeStatus
├── "支付订单" [M]
│   ├── "订单查询" [C]         biz:payOrder:list
│   ├── "订单详情" [C]         biz:payOrder:query
│   └── "导出" [F]            biz:payOrder:export
├── "发票查询" [M]
│   ├── "发票列表" [C]         biz:invoice:list
│   ├── "发票详情" [C]         biz:invoice:query
│   ├── "PDF 下载" [F]         biz:invoice:download
│   └── "导出" [F]            biz:invoice:export
├── "退款管理" [M]
│   ├── "退款列表" [C]         biz:refund:list
│   ├── "退款详情" [C]         biz:refund:query
│   └── "发起退款" [F]         biz:refund:add
└── "BI 看板" [M]
    └── "运营全局看板" [C]      biz:bi:dashboard
```

权限动作命名遵循 `AGENTS.md` 规范：`list / query / add / edit / remove / export / import`，不混用 `update / delete`。

### 6.3 前端按钮权限

每个按钮加 `v-hasPermi="['biz:xxx:yyy']"`，与后端 `@PreAuthorize("@ss.hasPermi('biz:xxx:yyy')")` 严格一一对应。

### 6.4 消费者匿名接口

| 接口 | 鉴权 |
| --- | --- |
| `GET /anon/pii/qrcode/{code}` | `@Anonymous` |
| `POST /anon/pii/pay/precreate` | `@Anonymous`（按 qrcode 校验合法性） |
| `GET /anon/pii/order/{no}?token=xxx` | `@Anonymous` + `order_token` 二次校验（Redis 30 分钟） |
| `GET /anon/pii/invoice/{no}/download?token=xxx` | `@Anonymous` + `order_token` |
| `POST /pii/pay/notify` | `@Anonymous`（验签代替） |
| `POST /pii/invoice/notify` | `@Anonymous`（验签代替） |
| `POST /pii/refund/notify` | `@Anonymous`（验签代替） |

### 6.5 Controller 三件套（每个业务接口）

1. `@PreAuthorize("@ss.hasPermi('biz:xxx:yyy')")` 或 `@Anonymous`
2. `@Log(title="...", businessType=...)`（来自 `com.manzhushaka.common.annotation.Log`；`businessType` 取自 `com.manzhushaka.common.enums.BusinessType`，如 `INSERT` / `UPDATE` / `DELETE` / `EXPORT` / `OTHER` / `GRANT` / `CLEAN` / `FORCE` / `IMPORT`）
3. 涉及敏感/超长数据时通过 `isSaveRequestData = false` / `isSaveResponseData = false` / `excludeParamNames` 避免落库

### 6.6 商户子账号

v1 不做。一个商户一个主账号。后续可扩展（`sys_user` 加父账号字段）。

---

## 7. BI 看板

### 7.1 页面布局

```
┌──────────────────────────────────────────────────────────────┐
│  PageHeader: 标题 + 描述                                     │
├──────────────────────────────────────────────────────────────┤
│  [时间范围] [税目] [商户] [市县筛选] [导出]                   │
├──────────────────────────────────────────────────────────────┤
│  KPI 卡片行 (4 张)                                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         │
│  │ 总交易额 │ │ 总开票额 │ │ 总订单数 │ │ 异常订单 │         │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘         │
├──────────────────────────────────────────────────────────────┤
│  趋势 + 占比                                                 │
│  ┌──────────────────┐ ┌──────────────────┐                  │
│  │ 交易额趋势        │ │ 各税目占比        │                  │
│  └──────────────────┘ └──────────────────┘                  │
├──────────────────────────────────────────────────────────────┤
│  海南地图 + 下钻                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  ECharts 海南地图                                      │ │
│  │  - L1 省 → L2 市县 → L3 区/镇                          │ │
│  │  - 颜色深浅 = 交易额                                    │ │
│  │  - 悬停: 名称 / 金额 / 订单数 / 商户数                  │ │
│  │  - 面包屑导航当前层级                                   │ │
│  │  - URL 状态: ?level=city&regionId=...                   │ │
│  └────────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────────┤
│  商户排行 + 异常明细                                         │
│  ┌──────────────────┐ ┌──────────────────┐                  │
│  │ 商户排行 Top 10   │ │ 异常订单明细      │                  │
│  └──────────────────┘ └──────────────────┘                  │
└──────────────────────────────────────────────────────────────┘
```

### 7.2 关键指标

| 指标 | 公式 |
| --- | --- |
| 总交易额 | `SUM(amount)` WHERE `pay_status IN ('PAID','REFUNDED')` AND `pay_time BETWEEN ?` |
| 总开票额 | `SUM(amount)` WHERE `invoice_status='ISSUED'` AND `invoice_issue_time BETWEEN ?` |
| 总订单数 | `COUNT(*)` WHERE `pay_time BETWEEN ?` |
| 异常订单数 | `COUNT(*)` WHERE `pay_status='PAID' AND invoice_status IN ('FAILED','CLOSED')` 或 MQ 死信 |
| 各税目占比 | `SUM(amount) GROUP BY tax_item_id` |
| 各市县分布 | 按 `sys_dept.ancestors` 聚合到市县 |
| 商户排行 | `SUM(amount) GROUP BY merchant_id ORDER BY DESC LIMIT 10` |

### 7.3 地图下钻接口

`GET /pii/bi/dept/aggregate?level=city&parentDeptId={省id}&startTime=&endTime=&taxItemId=`

```json
{
  "level": "city",
  "items": [
    { "deptId": 100, "deptName": "海口市", "regionCode": "460100",
      "totalAmount": 1234500, "orderCount": 234, "merchantCount": 12 },
    ...
  ]
}
```

实现：`pii_pay_order.merchant_id` 关联 `sys_user.dept_id`，按 `sys_dept.ancestors` LIKE 过滤。

### 7.4 缓存策略

- 看板整体查询：Redis 5 分钟缓存，key `pii:bi:dashboard:{queryHash}`
- 地图聚合：Redis 5 分钟缓存，key `pii:bi:dept:{level}:{parentDeptId}:{queryHash}`
- 失效：被动 5 分钟过期 + 商户/订单变更时主动 invalidate

### 7.5 预置行政区划数据

`sql/manzhushaka_db_init.sql` 末尾新增 `sys_dept` 行政区划 INSERT：

- 1 个"海南省" dept（`dept_type=region`，`region_level=1`，`region_code=460000`）
- 19 个市县 dept（`dept_type=region`，`region_level=2`）
- 约 200 个区/镇 dept（`dept_type=region`，`region_level=3`，按民政部 2025 年数据）

### 7.6 v1 不做（显式不做）

- 商户自助报表
- 海南税局/各市县税局 dashboard（数据范围已配，角色已留，**留 v2**）
- 实时刷新（手动刷新 + 5 分钟缓存）
- 图表钻取到单笔订单（v2）

---

## 8. 异常、幂等、审计

### 8.1 异常体系

- 业务异常统一用 `com.manzhushaka.common.exception.ServiceException`
- 错误码 5 位字符串，**不与 HTTP 状态码挂钩**，**不体现版本号**：

| 错误码 | 说明 |
| --- | --- |
| `00000` | 成功 |
| `10001` | 参数错误 |
| `10002` | 二维码无效/已停用 |
| `10003` | 二维码已过期 |
| `10004` | 税目无效 |
| `10101` | 银联支付下单失败 |
| `10102` | 银联支付回调验签失败 |
| `10103` | 银联退款失败 |
| `10201` | 银联开票调用失败 |
| `10202` | 银联开票回调验签失败 |
| `10203` | 银联红冲失败 |
| `10301` | 退款金额超限 |
| `10302` | 订单状态不允许退款 |
| `10401` | 商户参数缺失 |
| `10501` | 消费者订单查询 token 无效 |

### 8.2 退款限制

- 单次退款 ≤ 原订单剩余可退金额
- 累计退款 ≤ 原订单金额
- 订单状态必须 `= PAID`
- 商户只能退自己的订单（拦截器自动注入 `merchant_id`）

### 8.3 订单超时关闭

- quartz job `PiiPayOrderExpireJob`，cron `0 */5 * * * ?` 每 5 分钟扫描
- 关闭条件：`pay_status='PENDING' AND create_time < NOW() - INTERVAL 30 MINUTE`
- 关闭动作：`pay_status='CLOSED'`，不发开票

### 8.4 审计

| 操作类型 | 落库表 |
| --- | --- |
| 后台操作（增删改） | `sys_oper_log`（`@Log` 注解） |
| 支付/开票/退款回调 | **不落操作日志**（高频、第三方） |
| MQ handler 执行 | `sys_mq_message_log`（框架） |
| 银联发票接口调用 | `pii_invoice_call_log`（可观测 + 重放） |
| 敏感字段 | `@Sensitive` 或 AES 加密列（`app_secret` / `pay_sign_key` / `invoice_sign_key`） |

---

## 9. 测试策略

### 9.1 测试分层

| 层 | 工具 | 覆盖率目标 |
| --- | --- | --- |
| 单元测试 | JUnit 5 + Mockito | statement 70%、核心 100% |
| 架构测试 | ArchUnit（项目已有） | 100% 通过 |
| 集成测试 | Spring Boot Test + MyBatis Test + TestContainers MySQL | 关键路径 |
| Controller 测试 | Spring Boot Test + MockMvc | 关键接口 |
| MQ handler 测试 | Spring Boot Test + MQ Mock | 100% |
| 回调测试 | Spring Boot Test + MockMvc | 100% |

### 9.2 测试关键场景

**Gateway 适配层**
- `UmsInvoiceGateway.invoice()` 签名生成正确性（按银联字典序 + key + SHA-256 + 大写）
- `UmsInvoiceGateway.verifyAndParse()` 验签正反例
- `NotifyVerifier` 边界场景（空值、特殊字符、JSONArray 顺序）
- Mock 模式（`pii.mode=MOCK`）下不发 HTTP

**业务 Service**
- `PayOrderService.preCreate()`：二维码有效/无效/过期/未启用四种分支
- `InvoiceOpenHandler.doHandle()`：MQ 触发、幂等、重试
- `RefundService.create()`：超额退款、累计超额、订单非 PAID、商户不匹配四种异常
- `BiReportService.aggregate()`：地图聚合查询正确性
- `DeptService` 的 dept_type 校验：禁止删 `region`/`merchant` 类型 dept

**回调 Controller**
- 支付回调：验签失败返回非 SUCCESS、验签成功 + 重复回调幂等、验签成功 + 正常更新
- 开票回调：状态 ISSUED/REVERSED/FAILED 各自更新正确
- 退款回调：触发红冲 MQ handler

**权限拦截器**
- `PiiTenantInterceptor`：商户账号只能查自己 `merchant_id` 数据
- `@PreAuthorize`：无权限账号调退款接口被拒
- `@Anonymous`：消费者接口不需 token

### 9.3 数据库测试

- 不依赖预置数据，测试方法内通过 `@Transactional + @Rollback` 或 `TestContainers` 自动回滚
- 集成测试 schema 用 `spring.datasource.url=jdbc:h2:mem:test` 或 TestContainers MySQL
- 行政区划数据 fixture：`dept_type=region` 预置 1 省 + 3 市县 + 5 区/镇的最小集

### 9.4 Mock / Sandbox

- 单元测试强制 `pii.mode=MOCK`
- 集成测试默认 `MOCK`，可切换 `REAL` 接银联 sandbox（`mobl-test.chinaums.com`）

### 9.5 前端测试

- v1 **无自动化测试**，UI 变更记录手工验证 + 截图
- 关键流程截图清单：
  - 商户扫码 → 选税目 → 填抬头 → 支付 → 开票成功
  - 商户登录 → 退款 → 红冲成功
  - 运营方登录 → BI 看板 → 地图下钻

---

## 10. 依赖与配置

### 10.1 新增 Maven 依赖

`manzhushaka-biz-pii/pom.xml`：

```xml
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
```

### 10.2 配置项

`application-dev.yml` / `application-prod.yml` 新增：

```yaml
pii:
  mode: MOCK                       # REAL | MOCK
  pay:
    notify-url: https://your-domain/pii/pay/notify
    refund-notify-url: https://your-domain/pii/refund/notify
  invoice:
    api-base-url: https://mobl-test.chinaums.com/fapiao-api-test/   # 测试环境
    notify-url: https://your-domain/pii/invoice/notify
  bi:
    cache-seconds: 300
  order:
    expire-minutes: 30
```

### 10.3 SQL 脚本

`sql/manzhushaka_db_init.sql` 末尾新增：
- `sys_dept` ALTER 增加 `dept_type` / `region_code` / `region_level`
- `pii_platform_wechat_config` / `pii_tax_item` / `pii_merchant_profile` / `pii_pay_qrcode` / `pii_pay_qrcode_tax_item` / `pii_pay_order` / `pii_refund_record` / `pii_payment_notify_log` / `pii_invoice_notify_log` / `pii_invoice_call_log` 建表
- 行政区划预置数据（1 省 + 19 市县 + ~200 区/镇）
- 4 个预置角色（`operator` / `merchant` / `tax_bureau_city` / `tax_bureau_province`）
- `pii` 一级菜单 + 二级菜单 + 按钮权限
- 默认授权：超级管理员拥有所有权限，运营方默认拥有 `pii` 全权限

### 10.4 SQL 增量脚本（按日期命名）

不在主脚本中的后续调整，按 `sql/yyyymmdd-pii-*.sql` 命名，**主脚本同步更新**。

---

## 11. 风险与边界

### 11.1 Agent 可自动保证

- `@PreAuthorize` / `@Anonymous` 鉴权
- `v-hasPermi` 显隐
- `M/C/F` 菜单路由过滤
- `@Sensitive` 字段脱敏
- 统一 `ServiceException`
- MyBatis XML 映射与字段同步
- `sql/manzhushaka_db_init.sql` 初始化

### 11.2 Agent 无法自动保证

- 业务语义正确性
- 银联接口字段映射准确性（需对照真实 `doc/银联商务发票服务平台复合接口文档-以旧换新v1.2(1).docx` 校对）
- 角色授权是否符合实际组织策略
- 敏感字段是否被产品允许展示
- UI 手工体验与跨环境迁移风险
- 银联公众号支付沙箱 vs 生产环境差异

### 11.3 已知 v1 不做（明确范围）

- 商户自助 BI 报表
- 海南税局/各市县税局 dashboard
- 实时数据看板
- 图表下钻到订单明细
- 商户子账号体系
- 多语言/多币种
- 增值税专用发票（v1 仅 `PLAIN`，`VAT` 字段已留）
- 商户主动撤销订单（消费者超时由定时任务自动关）

---

## 12. 验收标准

v1 交付通过条件：

1. **后台功能**：运营方可完成商户/税目/二维码的增删改查；商户可登录后台完成配置与查询
2. **消费者流程**：扫码 → 选税目 → 填抬头 → 支付 → 自动开票 → 下载 PDF 全链路可用
3. **退款流程**：商户发起退款 → 银联回调 → 自动红冲全链路可用
4. **BI 看板**：运营方全局看板 KPI + 地图下钻可用，缓存生效
5. **权限隔离**：商户只看到自己数据；税局角色账号可登录但无页面
6. **测试**：单元测试 + 架构测试 + 集成测试全部通过
7. **架构脚本**：`bash scripts/architecture/check-module-boundaries.sh` 通过
8. **构建**：`mvn clean package` 与 `npm run build:prod` 全部成功
9. **文档**：规格、计划、AGENTS.md 同步更新

---

## 13. 参考

- 银联商务发票服务平台复合接口文档 v1.2：`doc/银联商务发票服务平台复合接口文档-以旧换新v1.2(1).docx`
- 银联商务公众号支付技能包：`skills/ums-api-skills/`
- 项目架构与开发规范：`AGENTS.md` / `CLAUDE.md`