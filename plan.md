# 首页推荐板块 + 商城游客浏览 执行蓝图

## 需求与取舍（已探查确认）

1. **推荐商户 / 推荐景区**：数据源自 `iip_merchant`（category 含 餐饮/住宿/加油/景区）。
   加 `is_recommend char(1) NOT NULL DEFAULT '1'`（0=推荐，RuoYi 惯例），Admin 商户页加推荐开关；
   不加排序字段（过度设计）。C 端无商户详情页，推荐卡片本期纯展示。
2. **商城游客浏览**（现状：mall 接口未匿名、mall.vue 整页拦截游客）：
   - `/miniapp/coupon/mall` 加 `@Anonymous`（无用户上下文，直接放行）
   - `/miniapp/coupon/{couponId}` 加 `@Anonymous`，controller 内 try/catch 取 userId（匿名=null），已兑数量按 0
   - `SecurityContextHelper.getUserId()` 匿名抛 401 ServiceException，必须 catch
   - 首页积分商城板块去掉 `userStore.isLogin &&`，游客可见
   - mall.vue 去整页拦截，点「兑换」未登录 → redirectToLogin
   - detail.vue 游客可看，点兑换未登录 → redirectToLogin
3. **演示数据**：现有商户仅 1 个景区类，需补 3-4 个景区商户（殷墟/红旗渠/太行大峡谷/羑里城，与券数据呼应）+ logo 演示图。

## 冻结契约（各 worker 严格遵守，不得改名）

- 字段：`iip_merchant.is_recommend char(1) NOT NULL DEFAULT '1' COMMENT '是否推荐（0推荐 1不推荐）'`
- C 端接口：`GET /miniapp/merchant/recommend`，`@Anonymous`
  - 参数：`category`（可选精确匹配）、`excludeCategory`（可选排除）、`limit`（可选默认 6，上限 20）
  - 只查 `status='0' AND is_recommend='0'`，按 `update_time DESC, merchant_id DESC`
  - 返回 record `MiniappMerchantRecommendResult`：merchantId, merchantName, category, city, logo, description, businessHours, address（裁剪掉 contactPhone/memberId/audit 字段）
- 首页板块顺序（底部追加）：积分商城（游客可见）→ 推荐景区（category=景区）→ 推荐商户（excludeCategory=景区）

## Stage 1（并行 4 worker）

| Worker | 类型 | 范围（互不重叠） | 验证 |
|---|---|---|---|
| A 后端 | coder | manzhushaka-iip/**, manzhushaka-admin/** | mvn -pl manzhushaka-iip,manzhushaka-admin -am package 通过 |
| B miniapp 前端 | coder | ui-miniapp/** | npm run build:h5 通过 |
| C Admin 前端 | coder | ui-admin/src/views/iip/merchant/, api | npm run build:prod 通过 |
| D SQL+演示数据 | coder | sql/, docs/prototype/, uploadPath/ | 增量 SQL 在 docker 容器 123 执行成功 |

依赖处理：接口契约已冻结，B 按契约写不依赖 A 完成；D 的 SQL 加列不影响旧代码运行（显式列名 select），可先执行。

## Stage 2（我收口）

1. 后端重启（kill 旧进程 + java -jar），确认 8080 正常
2. curl 验证：recommend 接口（category/excludeCategory 两种调用）、mall 匿名、券详情匿名
3. WebBridge 手机视口截图：首页（游客+登录两态）、商城页游客态
4. 三个前端/后端构建已过确认 → git 提交 main
