# 支付即开票 v1 手工验证记录

日期：2026-07-03
分支：feat/pii-v1

## 验证结论

当前本地已完成自动化验收覆盖，真实手工截图验收待联调环境补齐。原因是本机未配置真实微信公众号支付参数、银联商务发票 sandbox 参数、运营方/商户登录账号和可访问回调域名，无法完成真实扫码支付、真实开票回调、真实退款红冲截图。

## 已完成的本地证据

- 支付、开票、退款、红冲数据链路：`PiiIntegrationTest` 使用 TestContainers MySQL 执行 `sql/pii_schema.sql`，覆盖二维码税目绑定、订单支付成功、开票成功、退款成功、订单红冲状态。
- PII 模块测试：`mvn -pl manzhushaka-biz-pii -am test -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false`，87 个测试通过，0 失败，0 跳过。
- 全量架构检查：`bash scripts/architecture/check-module-boundaries.sh`，全部边界检查通过。
- 全量后端测试：`mvn test -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false`，7 个 Maven 模块全部 SUCCESS。

## 手工清单

| 场景 | 状态 | 说明 |
| --- | --- | --- |
| 商户扫码 -> 选税目 -> 填抬头 -> 支付 -> 开票成功截图 | 待联调环境验证 | 需要真实或 sandbox 公众号支付参数、可访问回调域名、银联发票 sandbox 参数。 |
| 商户登录 -> 退款 -> 红冲成功截图 | 待联调环境验证 | 需要商户账号、已支付已开票订单、银联退款/发票红冲 sandbox 回调。 |
| 运营方登录 -> BI 看板 -> 地图下钻截图 | 待联调环境验证 | 需要运营方账号、后端服务和可用数据集；前端页面与接口已有自动化/构建验收。 |

## 联调前置项

1. 配置微信公众号支付 sandbox/真实参数：`appId`、`appKey`、商户号、终端号、支付签名密钥、支付回调地址。
2. 配置银联发票平台参数：`msgSrc`、发票签名密钥、卖方信息、开票回调地址。
3. 准备商户账号和运营方账号，并绑定对应 `sys_dept` / `pii_merchant_profile` 数据。
4. 准备公网可访问的 80/443 回调域名，指向本系统支付、发票、退款回调入口。
5. 用真实设备或微信开发者工具执行扫码支付页面流程并保存截图。
