# ui-miniapp · 发票积分平台小程序端

uni-app（Vue 3 + Vite + pinia）一套代码编译 **微信小程序（mp-weixin）** 与 **支付宝小程序（mp-alipay）** 双端，H5 可用于本地开发调试。

## 环境要求

- Node.js 18+（推荐 20 LTS）
- 二选一：
  - **CLI 方式**：本目录 `npm install` 后用 npm scripts 编译，再用对应开发者工具导入产物目录；
  - **HBuilderX 方式**：直接打开本目录，使用 HBuilderX 内置的 uni-app 运行/发行菜单（manifest.json / pages.json 已就位）。
- 微信开发者工具（预览 mp-weixin 产物）、支付宝小程序开发者工具（预览 mp-alipay 产物）。

## 安装依赖

```bash
cd ui-miniapp
npm install
```

## 各端 dev / build 命令

| 端 | 开发（监听编译） | 生产构建 | 产物目录 |
| --- | --- | --- | --- |
| 微信小程序 | `npm run dev:mp-weixin` | `npm run build:mp-weixin` | `dist/dev/mp-weixin` / `dist/build/mp-weixin` |
| 支付宝小程序 | `npm run dev:mp-alipay` | `npm run build:mp-alipay` | `dist/dev/mp-alipay` / `dist/build/mp-alipay` |
| H5 | `npm run dev:h5` | `npm run build:h5` | `dist/dev/h5` / `dist/build/h5` |

预览方式：微信开发者工具「导入项目」选择 `dist/dev/mp-weixin`；支付宝开发者工具选择 `dist/dev/mp-alipay`。

## 后端地址配置

集中在 `src/common/config.js`：

```js
const config = {
  baseURL: 'http://localhost:8080', // ← 改为你的后端地址
  uploadPath: '/common/upload',
  mockLoginEnabled: true,
  timeout: 15000
}
```

- 真机调试时把 `baseURL` 改为局域网/公网地址，并在小程序管理后台配置 **request 合法域名** 与 **uploadFile 合法域名**。
- 接口契约：AjaxResult `{code,msg,data}`（`code=200` 成功、`401` 未授权）；登录/资料接口的 `token`、`member` 在响应体顶层；分页接口返回 TableDataInfo `{code,msg,rows,total}`；文件上传 `POST /common/upload` 返回 `{url,fileName,newFileName}`，请求头统一携带 `Authorization: Bearer {token}`（封装在 `src/common/request.js`）。

## 平台 appid 配置

在 `src/manifest.json`：

```json
"mp-weixin": { "appid": "wx-placeholder" },
"mp-alipay": { "appid": "ali-placeholder" }
```

- `wx-placeholder` 替换为微信小程序真实 appid；`ali-placeholder` 替换为支付宝小程序真实 appid。
- 顶层 `appid: "__UNI__IIP001"` 为 uni-app 应用标识（DCloud 侧），打包 App 时才需要重新生成。

## 云闪付（unionpay）接入说明

云闪付小程序容器兼容支付宝小程序语法，本期**复用 mp-alipay 编译产物**接入，无需单独编译目标：

1. **适配层已预留**：`src/common/platform.js` 的 `getPlatform()` 支持 `'unionpay'`，登录请求的 `platform` 字段即以此为准；`doLogin()` 内 unionpay 分支当前使用 `provider: 'alipay'`。
2. **生产接入步骤**：
   - 将 `manifest.json` 中 `mp-alipay.appid` 替换为云闪付小程序分配的 appid（或按云闪付容器要求补充配置）；
   - 将 `platform.js` 中 unionpay 分支的 `provider` 替换为云闪付容器要求的值；
   - 后端 `MiniappLoginService` 配置云闪付 code2session 的 appid/密钥（未配置时自动走 mock：`mock_unionpay_{code}`）。
3. **开发期演示**：登录页「开发模式」区可点选平台标识（微信/支付宝/云闪付），通过 `setPlatformOverride('unionpay')` 把登录请求的 platform 切为 `unionpay`，无需重新编译即可演示云闪付链路。

## mock 登录说明

未配置真实平台 appid/密钥时，登录走**开发模拟**模式：

- 登录页提供「mock code 登录」输入框，输入任意字符串作为 code；
- 后端将该 code 作为 mock openid（`mock_{platform}_{code}`）自动注册/登录用户，返回真实 token 与用户资料；
- 同一 mock code 反复登录会命中同一用户，便于演示「上传发票 → 审核 → 积分 → 兑换 → 核销」全链路；
- 生产环境配置真实平台密钥后，`uni.login` 返回真实 code，`POST /miniapp/auth/login` 自动切换为真实 code2session 通道，前端代码无需改动。关闭 mock 入口可将 `src/common/config.js` 中 `mockLoginEnabled` 置为 `false`。

## 目录结构

```
ui-miniapp/
├── package.json            # 依赖与 dev/build 脚本
├── vite.config.js          # vite + @dcloudio/vite-plugin-uni
├── index.html              # H5 入口模板
└── src/
    ├── main.js             # createSSRApp + pinia
    ├── App.vue             # 全局样式（暖白底/主橙/深棕文字 设计变量与通用类）
    ├── manifest.json       # 应用与平台配置（appid 占位）
    ├── pages.json          # 页面注册 + tabBar（首页/商城/我的）
    ├── common/
    │   ├── config.js       # baseURL 等集中配置 + resolveFileUrl
    │   ├── request.js      # uni.request/uni.uploadFile 封装（token 头、AjaxResult、401、错误 toast）
    │   ├── platform.js     # 平台适配层（wechat/alipay/unionpay + doLogin + 云闪付预留）
    │   ├── format.js       # 日期/券类型/积分来源 文案格式化
    │   └── icons.js        # 手绘 inline SVG 图标（data URI 背景图，三端可渲染）
    ├── store/
    │   └── user.js         # pinia user store（token/member 持久化 storage）
    ├── api/                # 接口模块（auth/activity/invoice/points/coupon/merchant）
    └── pages/              # 13 个页面（详见 pages.json）
```

## 页面与接口对照

| 页面 | 调用接口 |
| --- | --- |
| pages/index/index | GET /miniapp/activity/current、GET /miniapp/member/profile、GET /miniapp/coupon/mall |
| pages/login/index | POST /miniapp/auth/login |
| pages/invoice/upload | POST /common/upload、POST /miniapp/invoice/submit |
| pages/invoice/list | GET /miniapp/invoice/list |
| pages/points/records | GET /miniapp/member/profile、GET /miniapp/points/records |
| pages/coupon/mall | GET /miniapp/coupon/mall |
| pages/coupon/detail | GET /miniapp/coupon/{id}、POST /miniapp/coupon/exchange、GET /miniapp/member/profile |
| pages/coupon/mine | GET /miniapp/coupon/mine |
| pages/merchant/center | GET /miniapp/merchant/info |
| pages/merchant/apply | POST /common/upload、POST /miniapp/merchant/apply |
| pages/merchant/verify | POST /miniapp/merchant/verify |
| pages/merchant/records | GET /miniapp/merchant/verify/records |
| pages/mine/index | GET /miniapp/member/profile |
