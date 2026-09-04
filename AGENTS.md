# Repository Guidelines

每次回答问题前，都先说问候语：“你好，我是 manzhushaka (≧▽≦)”，并在说完问候语后换行。

## 适用范围与优先级

本文件是本仓库 AI Agent 协作规范的入口。详细规则集中维护在 `agent-rules/` 目录；后端 Java 代码以 P3C《Java 开发手册》（嵩山版）为基线，前端代码遵循 `ui-admin` 现有 Vue 3 + Arco Design Vue 风格。

用户明确指令优先于仓库规则。专项规则优先于通用规则；同时命中多个专项领域时，必须组合执行，不得只选其中一个。P3C 规则按约束力理解为【强制】、【推荐】、【参考】；除非有充分理由并在评审中说明，否则新增和修改代码默认遵守相关规则。

## 规则加载

`agent-rules/` 中的普通 Markdown 不会被工具原生自动加载。Agent 必须先根据任务涉及的文件路径和业务内容判断范围，再主动完整读取对应文件，不得只依赖文件名猜测规则。

任何任务都必须读取：

- `agent-rules/common.md`

按任务范围继续读取：

| 触发范围 | 必须读取 |
| --- | --- |
| 修改 `ui-admin/**`，或涉及 Vue、TypeScript、Arco、页面、组件、样式、路由和前端状态 | `agent-rules/frontend.md` |
| 修改 Maven 后端模块、`pom.xml`、Java 源码、Java 测试或后端资源配置 | `agent-rules/java.md` |
| 修改 `sql/**`、表结构、索引、初始化数据、查询 SQL、Mapper XML 或 ORM 映射 | `agent-rules/database.md` |
| 涉及 HTTP API、Controller、DTO/VO、前端 API、菜单、按钮、路由权限、`@PreAuthorize`、`@Anonymous` 或 `v-hasPermi` | `agent-rules/api-permission.md` |
| 涉及 Redis Stream、`RedisStream*`、消息 Handler、消息重试、死信或消息台账 | `agent-rules/redis-stream-mq.md` |
| 执行依赖安装、构建、测试、本地启动、服务器配置，或准备提交、PR、发布 | `agent-rules/release.md` |

## 加载要求

- 任务同时涉及多个范围时，读取所有命中的规则文件。
- 阅读或评审任务也按涉及内容加载规则，不以“未修改文件”为由跳过。
- 开始实质分析或修改前，先向用户说明本次加载了哪些规则文件。
- 执行过程中范围扩大时，先补读新增领域的规则，再继续工作。
- 规则冲突或需求存在歧义时，说明冲突与可选解释，并优先采取风险最低、改动最小、最符合现有实现的方案。
