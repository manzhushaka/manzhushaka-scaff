# 构建、验证与发布规则

本文件适用于依赖安装、构建、测试、本地运行、服务器配置、提交和 Pull Request。

## 构建、测试与本地运行

- `mvn clean package`：构建全部后端模块。
- `mvn test`：运行单元测试和 ArchUnit 架构测试。
- `mvn -pl manzhushaka-admin -am spring-boot:run`：启动后端服务。
- `cd ui-admin && pnpm install --frozen-lockfile`：按 `pnpm-lock.yaml` 安装前端依赖。
- `cd ui-admin && pnpm run dev`：启动 Vite 开发服务。
- `cd ui-admin && pnpm run build`：构建生产前端包。
- 如果有代码变更，最好执行 `codegraph sync .` 更新索引。

## 服务器与运行规约

- 高并发服务器调小 TCP `time_wait` 超时时间，例如 `net.ipv4.tcp_fin_timeout = 30`。
- 调大最大文件句柄数，避免高并发下 `open too many files`。
- JVM 设置 `-XX:+HeapDumpOnOutOfMemoryError`，便于 OOM 排查。
- 生产环境 JVM `Xms` 和 `Xmx` 设置相同，避免 GC 后调整堆大小。
- 内部重定向用 `forward`；外部重定向使用统一代理生成。

## 提交与 Pull Request

- 提交信息遵循现有 Conventional Commit 风格，例如 `feat(ui-admin): add theme tokens`、`refactor: remove common coupling`、`test: add architecture guardrails`。
- PR 需说明变更范围、影响模块、验证命令和关联 Issue。
- 涉及 UI 时提供截图；涉及数据库时同步 `sql` 脚本和回滚说明；涉及配置时说明本地与生产差异。
- 合并前至少运行相关后端测试；架构边界、数据库、安全、前后端契约和异常日志规则无法完全依赖静态扫描，必须在评审中人工确认。
