# IIP 发票积分平台 系统说明（v1.0.0）

> 版本号：v1.0.0（语义化版本：主版本.次版本.修订号）
> 发布日期：2026-07-18
> 基线：manzhushaka-scaff 脚手架 3.9.2 · 基于安阳"发票核验 + 积分兑换"模式
> 本文档由集成验证阶段产出，包含架构、数据、接口、运行指南与真实端到端验证记录；版本变更历史见文末「版本记录」。

## 1. 需求背景（安阳模式）

参考安阳发票核验积分兑换案例与云闪付满减活动，核心业务闭环：

```
用户在参与商户消费 → 取得发票 → 小程序上传发票 → 管理员审核
→ 按发票面额 × 活动比例发放积分（当前活动 1:1，四舍五入）→ 积分商城兑换门票/优惠券
→ 用户到店/到景区出示核销码 → 商户小程序核销 → 完成闭环
```

本期明确不做：发票 OCR 自动识别、税局真伪查验（预留 image_url 与人工审核位）、真实平台登录密钥（默认 mock 降级登录）、支付/退款/券转赠。

## 2. 角色与端

| 角色 | 使用端 | 核心能力 |
| ---- | ------ | -------- |
| 普通用户 member | 小程序 ui-miniapp | 平台登录、上传发票、查审核状态、查积分与流水、积分商城兑换券、查看我的券与核销码 |
| 商户 merchant | 小程序 ui-miniapp | 申请入驻、查看商户信息、输码/扫码核销、查看核销记录 |
| 管理员 admin | 后台 ui-admin | 商户/用户管理、发票审核、积分账户与流水、手工调整积分、券定义管理、兑换记录查询导出、活动管理与配置、数据概览 |

## 3. 架构

### 3.1 模块划分

- 后端 Maven 多模块：`manzhushaka-admin`（Web 启动层、Controller、DTO/VO）、`manzhushaka-framework`（安全/配置/AOP）、`manzhushaka-system`（系统业务）、`manzhushaka-quartz`（定时任务）、`manzhushaka-common`（通用基础）、**`manzhushaka-iip`（本业务域）**。
- 前端后台：`ui-admin`（Vue 3 + Element Plus），新增 `src/api/iip/*` 与 `src/views/iip/*` 共 8 个页面。
- 小程序：`ui-miniapp`（uni-app Vue3 + Vite + pinia），一套代码编译 mp-weixin / mp-alipay 双端。

### 3.2 iip 模块分层

```
manzhushaka-iip/src/main/java/com/manzhushaka/iip/
  domain/        11 个持久化实体（继承 BaseEntity，手写 getter/setter/toString，无 Lombok）
  mapper/        MyBatis Mapper 接口（@MapperScan 挂载 com.manzhushaka.iip.mapper）
  service/       IIipXxxService + impl/，含跨域契约 IIipPointsService（awardPoints/consumePoints/getAvailablePoints）
  application/   query/command/result/service/impl（record 风格）
  task/          PointExpireTask（@Component("pointExpireTask")）
  miniapp/       MiniappLoginService（wechat/alipay/unionpay 适配 + mock 降级）
  resources/mapper/iip/  各 Mapper XML
```

跨域约定：业务域之间只读引用允许直接使用对方 Mapper（如用户域读 `IipPointsAccountMapper` 取累计积分），写操作必须走对方 Service 契约。

### 3.3 鉴权

- 管理端 `/iip/**`：标准 `@PreAuthorize("@ss.hasPermi('iip:xxx:yyy')")`，JWT + Redis 会话。
- 小程序登录 `POST /miniapp/auth/login`：`@Anonymous` 匿名放行；MiniappLoginService 按 platform 取 openid，未配置真实 appid 时走 mock（`mock_{platform}_{code}`），getOrCreate 用户后签发 token，返回 `{token, member}`。
- 其余 `/miniapp/**`：复用 `anyRequest().authenticated()` + JwtAuthenticationTokenFilter，Controller 内 `SecurityContextHelper.getUserId()` 取 member_id。
- 商户能力：接口内校验 `iip_merchant.member_id = 当前用户 且 status = '0'`，否则抛"非商户账号或商户未通过审核"，不走后台 perms。

## 4. 数据库（库 `iip`，utf8mb4）

库为完整应用库：`sys_*` 全套 + 11 张 `iip_*` 业务表 + iip 菜单/授权/定时任务种子。初始化脚本 `sql/manzhushaka_db_init.sql`。

| 表 | 说明 | 关键字段 |
| --- | ---- | -------- |
| iip_member | 小程序用户 | member_id PK、nickname、avatar、phone、status |
| iip_member_account | 平台账号 | uk(platform, openid)、unionid |
| iip_merchant | 商户 | merchant_no 唯一（M+年月+5位序号）、member_id 绑定登录用户、status（0正常 1停用 2待审核） |
| iip_invoice | 发票 | uk(invoice_code, invoice_no) 防重复、amount、status（0待审核 1已通过 2已驳回）、points、activity_id |
| iip_points_account | 积分账户 | member_id 唯一、total_points 累计、available_points 可用、used_points、expired_points、version 乐观锁 |
| iip_points_record | 积分流水 | change_type（earn/consume/expire/adjust）、balance_after、biz_type/biz_id、remaining（earn 批次剩余）、expire_time |
| iip_coupon | 券定义 | coupon_type（ticket/virtual/full_reduction/discount）、points_cost、total_stock/remain_stock（-1 不限）、per_member_limit、valid_type（fixed/days）、threshold_amount/discount_amount（满减预留）、merchant_id（null 通用） |
| iip_coupon_record | 券实例 | verify_code 唯一、status（0未使用 1已使用 2已过期）、verify_merchant_id、activity_id |
| iip_activity | 活动 | activity_no 唯一、start_time/end_time、points_ratio、merchant_limit（-1 不限）、coupon_quota（-1 不限）、status |
| iip_activity_merchant | 活动商户 | uk(activity_id, merchant_id) |
| iip_activity_coupon | 活动券配置 | uk(activity_id, coupon_id)、issue_limit、issued_count |

积分规则要点：earn 流水携带 remaining 与 expire_time（默认发放起 365 天），消费按 FIFO 扣减批次；过期由定时任务每日结转；发分幂等（bizType+bizId 已存在 earn 则跳过）。

## 5. 接口清单

### 5.1 管理端（前缀 /iip，均需登录 + perms）

| 模块 | 接口 | perms |
| ---- | ---- | ----- |
| 数据概览 | GET /iip/overview/summary、GET /iip/overview/trend | iip:overview:list |
| 用户管理 | GET /iip/member/list、GET /iip/member/getInfo/{id}、PUT /iip/member/status、GET /iip/member/export | iip:member:list / query / edit / export |
| 商户管理 | GET /iip/merchant/list、GET /iip/merchant/getInfo/{id}、POST /iip/merchant、PUT /iip/merchant、DELETE /iip/merchant/{ids}、PUT /iip/merchant/audit、GET /iip/merchant/export | iip:merchant:list / query / add / edit / remove / audit / export |
| 发票审核 | GET /iip/invoice/list、GET /iip/invoice/getInfo/{id}、PUT /iip/invoice/audit、GET /iip/invoice/export | iip:invoice:list / query / audit / export |
| 积分 | GET /iip/points/account/list、GET /iip/points/record/list、POST /iip/points/adjust | iip:points:list / query / adjust |
| 券管理 | GET /iip/coupon/list、GET /iip/coupon/{id}、POST /iip/coupon、PUT /iip/coupon、DELETE /iip/coupon/{ids}、GET /iip/coupon/export | iip:coupon:list / query / add / edit / remove / export |
| 兑换记录 | GET /iip/exchange/list、GET /iip/exchange/{recordId}、GET /iip/exchange/export | iip:exchange:list / query / export |
| 活动管理 | GET /iip/activity/list、GET /iip/activity/{id}、POST /iip/activity、PUT /iip/activity、DELETE /iip/activity/{ids}；配置：GET/POST/DELETE /iip/activity/merchants、GET/POST/PUT/DELETE /iip/activity/coupons | iip:activity:list / query / add / edit / remove / config |

### 5.2 小程序端（前缀 /miniapp，除登录外均需 token）

| 接口 | 说明 |
| ---- | ---- |
| POST /miniapp/auth/login（@Anonymous） | 入参 {platform, code, nickname, avatar}；返回顶层 {token, member} |
| GET /miniapp/member/profile | 用户资料，member 含 availablePoints、totalPoints |
| POST /miniapp/invoice/submit | 上传发票（发票号幂等，重复报"该发票已上传过"） |
| GET /miniapp/invoice/list、GET /miniapp/invoice/{id} | 我的发票 |
| GET /miniapp/points/records | 积分流水 |
| GET /miniapp/activity/current | 当前生效活动（状态启用且在时间窗内，取 start_time 最新） |
| GET /miniapp/coupon/mall | 积分商城（上架且在兑换窗口内） |
| GET /miniapp/coupon/{couponId} | 券详情（含当前用户已兑数量） |
| POST /miniapp/coupon/exchange | 兑换，返回含 verifyCode 的券实例 |
| GET /miniapp/coupon/mine?status=0/1/2 | 我的券（未使用/已使用/已过期） |
| POST /miniapp/merchant/apply | 商户入驻申请 |
| GET /miniapp/merchant/info | 我的商户信息 |
| POST /miniapp/merchant/verify | 核销（需已审核商户；重复核销报"券已使用或已过期"） |
| GET /miniapp/merchant/verify/records | 核销记录 |

## 6. 菜单权限闭环

菜单 menu_id 200（M 目录"发票积分"）+ 201~208（C 页面：概览/用户/商户/发票/积分/券/兑换/活动）+ 211~236（F 按钮），全部写入 `sql/manzhushaka_db_init.sql` 并授予 role_id=2。闭环链路：`sys_menu.perms` ↔ 前端按钮 `v-hasPermi` ↔ 后端 `@PreAuthorize` 一一对应；`F` 不生成路由，`M`/`C` 渲染侧边栏；初始化库后即可访问。

## 7. 种子数据

- 商户 3 家（状态正常）：老字号烩面馆（餐饮）、殷都宾馆（住宿）、中石化安阳加油站（加油），merchant_no M20260700001~3。
- 券 6 张：殷墟博物馆首道门票/殷墟宫殿宗庙遗址门票/红旗渠景区门票/太行大峡谷门票（各 2000 分）、羑里城门票（1500 分）、餐饮满100减20券（full_reduction，500 分，threshold 100/discount 20），有效期 fixed 至 2027-05-31。
- 活动 1 个："乐享安阳——发票核验积分兑换活动"，2026-07-01 ~ 2027-05-31，points_ratio 1.00，merchant_limit -1，coupon_quota -1，关联 3 商户与 6 券。
- 定时任务：`pointExpireTask.expire()`，cron `0 17 2 * * ?`（每日 02:17）。
- 管理员：admin / admin@123（即 `sys.user.initPassword` 配置值；注意不是 RuoYi 默认的 admin123）。

## 8. 本地运行指南

1. 建库导入：Docker MySQL 容器 `123`（root/1a2s3d4f），`CREATE DATABASE iip DEFAULT CHARSET utf8mb4;` 后导入 `sql/manzhushaka_db_init.sql`。Redis 在 localhost:6379。
2. 后端：`export JAVA_HOME=$HOME/.sdkman/candidates/java/current`，`mvn -q install -DskipTests` 后 `mvn -pl manzhushaka-admin spring-boot:run`（注意：直接 `-pl ... -am spring-boot:run` 会在根聚合工程上报 "Unable to find a suitable main class"，需先 install 再去掉 -am；或运行 `manzhushaka-admin/target/manzhushaka-admin.jar`）。`application-dev.yml` 默认连 iip 库，验证码默认关闭（CAPTCHA_ENABLED:false）。
3. ui-admin：`cd ui-admin && npm install && npm run dev`（生产 `npm run build:prod`）。
4. ui-miniapp：`npm install` 后 `npm run dev:mp-weixin` / `build:mp-weixin` / `build:mp-alipay`，产物在 `dist/build/<平台>`，用对应开发者工具导入。dcloudio 依赖必须固定为 npm 上存在的完整版本号（如 `3.0.0-alpha-5020120260710001`），`^` 前缀会错误解析到陈旧 alpha。`manifest.json` 中 appid 为占位 `__UNI__IIP001`，真机运行需替换为真实 appid。
5. 云闪付接入：复用支付宝小程序容器语法，`common/platform.js` 适配层已预留 unionpay provider；真机接入补 appid/密钥后，MiniappLoginService 的 unionpay 分支由 mock 降级切换为真实 code2session。

## 9. 定时任务

`PointExpireTask.expire()`（bean 名 `pointExpireTask`）：每日 02:17 扫描 `change_type='earn' 且 remaining>0 且 expire_time<now` 的批次，将剩余积分结转为 expire 流水，同步账户 available_points/expired_points。sys_job 种子已含。

## 10. 端到端验证记录（2026-07-18 真实执行）

环境：后端 `mvn -pl manzhushaka-admin spring-boot:run`（2.78s 启动），MySQL 容器 123，Redis localhost:6379，全部 curl 实测。

| 步骤 | 操作 | 结果 |
| ---- | ---- | ---- |
| 1 | admin 登录 | admin123 失败；**实际密码 admin@123**（sys.user.initPassword 一致），登录成功 |
| 2 | 小程序登录 e2e_user_001 | 成功，memberId=100，返回顶层 {token, member}，availablePoints=0 |
| 3 | 提交发票 E2E20260718001（235.50 元） | 成功 invoiceId=1000；重复提交被拦截"该发票已上传过" ✅ 幂等 |
| 4 | admin 审核通过 | 成功；用户获 earn 流水 236 分（235.50×1.00 四舍五入），余额 236 |
| 5 | admin 积分调整 +3000 | 成功；可用积分 3236 |
| 6 | 当前活动/商城 | 返回"乐享安阳"活动（含 3 商户 6 券）；商城 6 张券 |
| 7 | 兑换 couponId=1（殷墟博物馆 2000 分） | 成功，verifyCode=DD71C541321F4550；余额 1236；consume 流水正确 |
| 8 | 商户登录/入驻/未审核核销 | memberId=101；申请成功 merchantId=100；未审核核销被拦截"非商户账号或商户未通过审核" ✅ |
| 9 | admin 审核商户 → 核销 | 核销成功；商户核销记录 1 条；用户券 status=1；重复核销报"券已使用或已过期" ✅ 幂等 |
| 10 | 数据概览 | summary：memberCount=2、merchantCount=4、approvedInvoice=1、pointsIssued=3236、pointsConsumed=2000、exchange=1、verified=1，与操作一致；trend 当日各 1 条；兑换记录齐全 |
| 11 | totalPoints 补丁验证 | 登录与 profile 返回 totalPoints=3236、availablePoints=1236 ✅ |

验证后已 kill 后端进程，8080 端口释放，无孤儿进程。

## 11. 构建与测试结论

- 后端：`mvn clean package` + `mvn test` 115 测试全绿（开发阶段结论）；totalPoints 修改后 `mvn -q -pl manzhushaka-admin -am compile` 与 `mvn -q -pl manzhushaka-iip test` 通过。
- ui-admin：`npm run build:prod` 通过（3.95s）。
- ui-miniapp：`npm run build:mp-weixin` 与 `npm run build:mp-alipay` 均通过（修复 package.json 中不存在的 dcloudio 版本号为 `3.0.0-alpha-5020120260710001` 精确固定后）。

## 12. 已知限制与后续路线

1. 发票 OCR：当前人工审核，预留 image_url；后续可接 OCR 服务自动回填发票字段。
2. 真实平台登录：三端均为 mock 降级（code 即 openid），接入真实微信/支付宝/云闪付需补 appid/密钥与 code2session 调用。
3. 满减券核销场景：full_reduction 类型字段已预留（threshold_amount/discount_amount），核销时未校验消费金额门槛，需商户端补充实付金额录入与满减计算。
4. 活动额度联动：merchant_limit/coupon_quota 字段与配置接口已就绪，发票发分与兑换已校验活动券 issue_limit，活动级 quota 汇总校验可进一步加强。
5. 券转赠、退款、支付未实现（本期范围外）。

## 13. 版本记录

| 版本号 | 发布日期 | 类型 | 变更摘要 |
| ------ | -------- | ---- | -------- |
| v1.0.0 | 2026-07-18 | 首次发布 | 基于 manzhushaka-scaff 3.9.2 脚手架完成 IIP 发票积分平台首期开发：新增 manzhushaka-iip 后端模块（用户/商户/发票/积分/券/活动/概览 7 个业务域、60+ 接口、积分批次 FIFO 与过期结转定时任务）；Docker MySQL `iip` 库（11 张业务表 + 菜单权限闭环 + 种子数据）；ui-admin 新增 8 个管理页；新建 ui-miniapp uni-app 小程序端（13 页面，微信/支付宝双端编译通过，云闪付适配层预留）；端到端全链路实测通过（mvn 115 测试全绿）。 |

> 版本号规则：遵循语义化版本（Semantic Versioning）。主版本号：不兼容的架构/接口变更；次版本号：向下兼容的功能新增（如发票 OCR、满减券支付联动、券转赠等后续路线项）；修订号：向下兼容的问题修复。每次变更同步更新本文档头部版本号与本表。
