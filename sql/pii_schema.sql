-- ============================================================================
-- 支付即开票（PII）业务表
-- 表前缀: pii_
-- 执行: 紧随 sys_dept 扩展之后
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 平台统一公众号配置
-- ----------------------------------------------------------------------------
drop table if exists pii_platform_wechat_config;
create table pii_platform_wechat_config (
    id               bigint       auto_increment primary key,
    app_id           varchar(32)  not null comment '微信公众号 AppID',
    app_secret_enc   varchar(512) not null comment 'AppSecret AES 加密',
    pay_sign_key_enc varchar(512) not null comment '公众号支付签名密钥 AES 加密',
    token            varchar(64)  null comment '公众号 Token',
    aes_key_enc      varchar(512) null comment '公众号消息加密 AESKey',
    status           tinyint      not null default 1 comment '1=启用 0=停用',
    remark           varchar(255) null,
    create_time      datetime     not null default current_timestamp,
    update_time      datetime     not null default current_timestamp on update current_timestamp,
    create_by        bigint       null,
    update_by        bigint       null,
    unique key uk_app_id (app_id)
) engine=innodb default charset=utf8mb4 comment='平台统一公众号配置';

-- ----------------------------------------------------------------------------
-- 税目
-- ----------------------------------------------------------------------------
drop table if exists pii_tax_item;
create table pii_tax_item (
    id                 bigint       auto_increment primary key,
    tax_item_code      varchar(19)  not null comment '税收分类编码 19 位',
    name               varchar(128) not null comment '商品/服务名称',
    brevity_code       varchar(32)  null comment '商品简码',
    category           varchar(64)  null comment '商品分类',
    tax_rate           decimal(5,2) not null comment '税率（6、13）',
    vat_special        varchar(64)  null comment '增值税特殊管理',
    free_tax_type      varchar(1)   null comment '免税类型',
    prefer_policy_flag varchar(1)   null comment '是否使用优惠政策',
    sort               int          not null default 0 comment '排序',
    status             tinyint      not null default 1 comment '1=启用 0=停用',
    remark             varchar(255) null,
    create_time        datetime     not null default current_timestamp,
    update_time        datetime     not null default current_timestamp on update current_timestamp,
    create_by          bigint       null,
    update_by          bigint       null,
    unique key uk_tax_item_code (tax_item_code),
    key idx_status (status, sort)
) engine=innodb default charset=utf8mb4 comment='税目';

-- ----------------------------------------------------------------------------
-- 商户档案
-- ----------------------------------------------------------------------------
drop table if exists pii_merchant_profile;
create table pii_merchant_profile (
    id                       bigint       auto_increment primary key,
    dept_id                  bigint       not null comment '关联 sys_dept.id（dept_type=merchant）',
    merchant_name            varchar(128) not null comment '商户主体名（开票用）',
    ums_merchant_id          varchar(15)  not null comment '银联商户号',
    ums_terminal_id          varchar(8)   not null comment '银联终端号',
    ums_pay_sign_key_enc     varchar(512) not null comment '公众号支付签名密钥 加密',
    ums_invoice_sign_key_enc varchar(512) not null comment '银联发票签名密钥 加密',
    invoice_msg_src          varchar(32)  not null comment '发票接口消息来源',
    invoice_seller_name      varchar(128) not null comment '卖方名称',
    invoice_seller_tax_code  varchar(32)  not null comment '卖方纳税人识别号',
    invoice_seller_address   varchar(128) null,
    invoice_seller_telephone varchar(16)  null,
    invoice_seller_bank      varchar(64)  null,
    invoice_seller_account   varchar(32)  null,
    invoice_payee            varchar(8)   null comment '收款人',
    invoice_checker          varchar(8)   null comment '复核人',
    invoice_drawer           varchar(8)   null comment '开票人',
    notify_url               varchar(256) not null comment '开票回调地址 80/443 端口',
    status                   tinyint      not null default 1,
    remark                   varchar(255) null,
    create_time              datetime     not null default current_timestamp,
    update_time              datetime     not null default current_timestamp on update current_timestamp,
    create_by                bigint       null,
    update_by                bigint       null,
    unique key uk_dept_id (dept_id),
    unique key uk_ums (ums_merchant_id, ums_terminal_id)
) engine=innodb default charset=utf8mb4 comment='商户档案';

-- ----------------------------------------------------------------------------
-- 支付二维码
-- ----------------------------------------------------------------------------
drop table if exists pii_pay_qrcode;
create table pii_pay_qrcode (
    id               bigint       auto_increment primary key,
    merchant_id      bigint       not null comment '关联 pii_merchant_profile.id',
    qrcode_code      varchar(32)  not null comment '二维码业务编码',
    qrcode_url       varchar(512) not null comment 'H5 入口 URL',
    qrcode_image_url varchar(512) null comment '二维码图片 base64/OSS',
    name             varchar(64)  not null comment '二维码名称',
    status           tinyint      not null default 1,
    expire_time      datetime     null comment '失效时间',
    remark           varchar(255) null,
    create_time      datetime     not null default current_timestamp,
    update_time      datetime     not null default current_timestamp on update current_timestamp,
    create_by        bigint       null,
    update_by        bigint       null,
    unique key uk_qrcode_code (qrcode_code),
    key idx_merchant_status (merchant_id, status)
) engine=innodb default charset=utf8mb4 comment='支付二维码';

-- ----------------------------------------------------------------------------
-- 二维码-税目 关联
-- ----------------------------------------------------------------------------
drop table if exists pii_pay_qrcode_tax_item;
create table pii_pay_qrcode_tax_item (
    id             bigint auto_increment primary key,
    qrcode_id      bigint not null,
    tax_item_id    bigint not null,
    default_amount bigint null comment '默认金额（分）',
    unique key uk_qrcode_tax (qrcode_id, tax_item_id),
    key idx_tax_item (tax_item_id)
) engine=innodb default charset=utf8mb4 comment='二维码-税目关联';

-- ----------------------------------------------------------------------------
-- 支付订单
-- ----------------------------------------------------------------------------
drop table if exists pii_pay_order;
create table pii_pay_order (
    id                   bigint       auto_increment primary key,
    merchant_id          bigint       not null,
    qrcode_id            bigint       not null,
    tax_item_id          bigint       not null,
    out_trade_no         varchar(32)  not null comment '本系统订单号（32 位，复用为银联 merOrderId）',
    ums_mer_order_date   varchar(8)   not null comment 'yyyyMMdd',
    amount               bigint       not null comment '含税总金额（分）',
    buyer_name           varchar(128) not null comment '抬头',
    buyer_tax_code       varchar(32)  null comment '税号',
    buyer_email          varchar(64)  null,
    buyer_mobile         varchar(16)  null,
    buyer_openid         varchar(64)  null comment '公众号 openId',
    pay_status           varchar(16)  not null default 'PENDING' comment 'PENDING/PAID/REFUNDING/REFUNDED/CLOSED',
    pay_time             datetime     null,
    pay_trade_no         varchar(64)  null comment '银联支付流水号',
    pay_notify_status    varchar(16)  null comment 'NOT/OK/FAILED',
    refund_amount        bigint       not null default 0 comment '累计退款（分）',
    invoice_status       varchar(16)  not null default 'NONE' comment 'NONE/PENDING/ISSUING/ISSUED/REVERSING/REVERSED/FAILED',
    invoice_no           varchar(20)  null comment '发票号码',
    invoice_code         varchar(20)  null comment '发票代码',
    invoice_pdf_url      varchar(256) null,
    invoice_issue_time   datetime     null,
    invoice_reverse_time datetime     null,
    order_token          varchar(32)  not null comment '消费者匿名查询短 token',
    wechat_appid         varchar(32)  null,
    client_ip            varchar(64)  null,
    remark               varchar(255) null,
    create_time          datetime     not null default current_timestamp,
    update_time          datetime     not null default current_timestamp on update current_timestamp,
    create_by            bigint       null,
    update_by            bigint       null,
    unique key uk_out_trade_no (out_trade_no),
    key idx_merchant_pay_status (merchant_id, pay_status, pay_time),
    key idx_merchant_invoice (merchant_id, invoice_status),
    key idx_invoice_status (invoice_status, invoice_issue_time)
) engine=innodb default charset=utf8mb4 comment='支付订单';

-- ----------------------------------------------------------------------------
-- 退款记录
-- ----------------------------------------------------------------------------
drop table if exists pii_refund_record;
create table pii_refund_record (
    id                      bigint       auto_increment primary key,
    merchant_id             bigint       not null,
    pay_order_id            bigint       not null,
    out_refund_no           varchar(32)  not null comment '本系统退款单号',
    ums_refund_mer_order_id varchar(32)  null,
    amount                  bigint       not null comment '退款金额（分）',
    reason                  varchar(255) null,
    status                  varchar(16)  not null default 'PENDING' comment 'PENDING/SUCCESS/FAILED',
    ums_trade_no            varchar(64)  null,
    complete_time           datetime     null,
    operator_id             bigint       not null,
    trigger_invoice_reverse tinyint      not null default 1,
    create_time             datetime     not null default current_timestamp,
    update_time             datetime     not null default current_timestamp on update current_timestamp,
    unique key uk_out_refund_no (out_refund_no),
    key idx_pay_order (pay_order_id),
    key idx_merchant_status (merchant_id, status)
) engine=innodb default charset=utf8mb4 comment='退款记录';

-- ----------------------------------------------------------------------------
-- 支付回调幂等日志
-- ----------------------------------------------------------------------------
drop table if exists pii_payment_notify_log;
create table pii_payment_notify_log (
    id             bigint       auto_increment primary key,
    out_trade_no   varchar(32)  not null,
    notify_payload json         not null,
    sign           varchar(128) null,
    verify_result  tinyint      not null comment '1=通过 0=失败',
    processed      tinyint      not null default 0,
    created_at     datetime     not null default current_timestamp,
    unique key uk_out_trade_no (out_trade_no)
) engine=innodb default charset=utf8mb4 comment='支付回调幂等日志';

-- ----------------------------------------------------------------------------
-- 开票回调幂等日志
-- ----------------------------------------------------------------------------
drop table if exists pii_invoice_notify_log;
create table pii_invoice_notify_log (
    id                 bigint       auto_increment primary key,
    ums_mer_order_id   varchar(32)  not null,
    ums_mer_order_date varchar(8)   not null,
    qrcode_id          varchar(40)  null,
    notify_payload     json         not null,
    sign               varchar(128) null,
    verify_result      tinyint      not null,
    processed          tinyint      not null default 0,
    created_at         datetime     not null default current_timestamp,
    unique key uk_order (ums_mer_order_id, ums_mer_order_date)
) engine=innodb default charset=utf8mb4 comment='开票回调幂等日志';

-- ----------------------------------------------------------------------------
-- 银联发票接口调用日志
-- ----------------------------------------------------------------------------
drop table if exists pii_invoice_call_log;
create table pii_invoice_call_log (
    id            bigint       auto_increment primary key,
    pay_order_id  bigint       not null,
    msg_type      varchar(32)  not null comment 'issue/reverse/query/pickup',
    msg_id        varchar(64)  not null,
    request_body  json         null,
    response_body json         null,
    duration_ms   int          null,
    is_success    tinyint      not null,
    error_msg     varchar(512) null,
    created_at    datetime     not null default current_timestamp,
    unique key uk_order_msg (pay_order_id, msg_type),
    key idx_created (created_at)
) engine=innodb default charset=utf8mb4 comment='银联发票接口调用日志';
