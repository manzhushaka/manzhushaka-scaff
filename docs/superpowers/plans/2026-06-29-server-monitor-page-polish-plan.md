# 服务监控页面体验优化实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 修复 `ui-admin` 服务监控页面打开后全屏转圈体验差、接口失败时遮罩不关闭、页面视觉仍停留在旧若依表格结构的问题。

**架构：** 保持后端 `/monitor/server` 与 OSHI 采样逻辑不变，因为 CPU 使用率采样里固定等待 1 秒是当前数据精度的一部分。本轮只改前端页面：用页面内加载态替代全屏遮罩，用 `finally` 保证加载态收敛，用统一的主题 token 重构信息面板和表格样式。页面继续消费现有 `getServer()` 返回结构，不新增接口、不改权限、不改 SQL。

**技术栈：** Vue 3、Element Plus、Vite、SCSS、现有 `--ui-*` 主题 token、现有 `@element-plus/icons-vue` 自动导入。

---

## 1. 背景与根因

当前页面位于 `ui-admin/src/views/monitor/server/index.vue`。页面加载时调用：

```js
proxy.$modal.loading("正在加载服务监控数据，请稍候！")
getServer().then(response => {
  server.value = response.data
  proxy.$modal.closeLoading()
})
```

问题有 3 个：

- 前端使用全屏 loading，接口正常耗时也会阻塞用户操作。
- 只有成功分支关闭 loading，接口失败、超时或权限异常时可能留下遮罩。
- 模板仍使用 `el-card + 原生 table + el-table 内部 class + inline style`，视觉没有完全接入当前项目的 `ui-panel-card`、`ui-table-card` 和 `--ui-*` token。

后端 `manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/domain/Server.java` 中 `setCpuInfo()` 存在：

```java
long[] prevTicks = processor.getSystemCpuLoadTicks();
Util.sleep(OSHI_WAIT_SECOND);
long[] ticks = processor.getSystemCpuLoadTicks();
```

这意味着接口天然至少有约 1 秒等待。本计划不删除这段等待，避免改变监控数据语义。

## 2. 文件结构与职责

- 修改：`ui-admin/src/views/monitor/server/index.vue`
  - 负责页面结构、加载态、错误态、刷新入口、服务监控数据展示和页面级 scoped 样式。
- 可选修改：`ui-admin/src/assets/styles/theme-tokens.scss`
  - 仅当页面内 scoped 样式无法复用现有 token 时，补充极少量全局可复用样式。本轮推荐优先不改全局样式，避免影响其他页面。

## 3. 成功标准

- 打开「系统监控 / 服务监控」时不再出现全屏黑色遮罩。
- `/monitor/server` 正常慢 1 秒左右时，页面内显示加载状态，侧栏、顶部栏和页面滚动不被锁死。
- `/monitor/server` 失败、超时或返回非 200 业务码时，加载状态一定关闭，并展示可重试的错误态。
- CPU、内存、服务器信息、JVM 信息、磁盘状态完整展示，字段来源仍使用现有 `response.data`。
- 390 x 844 移动端下不出现页面级横向滚动；磁盘表格可在表格容器内部横向滚动。
- A/B/C 主题下卡片、表头、边框、危险状态、长路径文本均清晰可读。
- `cd ui-admin && npm run build:prod` 退出码为 0。

## 4. 实施任务

### 任务 1：记录当前行为并定义前端验收基线

**文件：**

- 读取：`ui-admin/src/views/monitor/server/index.vue`
- 读取：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/domain/Server.java`

- [ ] **步骤 1：确认当前页面仍使用全屏 loading**

运行：

```bash
rg -n "\\$modal\\.loading|closeLoading|getServer\\(\\)" ui-admin/src/views/monitor/server/index.vue
```

预期输出包含：

```text
proxy.$modal.loading("正在加载服务监控数据，请稍候！")
getServer().then(response => {
proxy.$modal.closeLoading()
```

- [ ] **步骤 2：确认后端 CPU 采样存在固定等待**

运行：

```bash
rg -n "OSHI_WAIT_SECOND|Util\\.sleep" manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/domain/Server.java
```

预期输出包含：

```text
private static final int OSHI_WAIT_SECOND = 1000;
Util.sleep(OSHI_WAIT_SECOND);
```

- [ ] **步骤 3：明确本轮不改后端**

检查：

```bash
git diff -- manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/monitor/ServerController.java manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/domain/Server.java
```

预期：执行本计划前后，上述两个后端文件都不产生本轮 diff。

### 任务 2：把阻塞式全屏 loading 改为页面内加载态

**文件：**

- 修改：`ui-admin/src/views/monitor/server/index.vue`

- [ ] **步骤 1：在 script 中建立页面状态**

将当前 script：

```vue
<script setup>
import { getServer } from '@/api/monitor/server'

const server = ref([])
const { proxy } = getCurrentInstance()

function getList() {
  proxy.$modal.loading("正在加载服务监控数据，请稍候！")
  getServer().then(response => {
    server.value = response.data
    proxy.$modal.closeLoading()
  })
}

getList()
</script>
```

改为：

```vue
<script setup name="ServerMonitor">
import { getServer } from '@/api/monitor/server'

const server = ref({})
const loading = ref(false)
const loadError = ref(false)

function getList() {
  loading.value = true
  loadError.value = false
  getServer().then(response => {
    server.value = response.data || {}
  }).catch(() => {
    loadError.value = true
  }).finally(() => {
    loading.value = false
  })
}

getList()
</script>
```

说明：

- `server` 初始值从数组改为对象，匹配实际返回结构。
- 使用 `finally` 保证成功、失败、超时都关闭页面内 loading。
- 错误消息继续由 `ui-admin/src/utils/request.js` 的 axios 响应拦截器统一弹出，本页只展示可重试状态。

- [ ] **步骤 2：在页面根容器使用页面内加载态**

将根容器：

```vue
<div class="app-container server-monitor-card">
```

改为：

```vue
<div class="app-container server-monitor-page" v-loading="loading">
```

- [ ] **步骤 3：运行构建确认 script 可编译**

运行：

```bash
cd ui-admin && npm run build:prod
```

预期：退出码为 0。若出现未定义变量、模板引用错误或 Vue 编译错误，先修复本任务引入的问题。

### 任务 3：增加页面头部、刷新入口和错误态

**文件：**

- 修改：`ui-admin/src/views/monitor/server/index.vue`

- [ ] **步骤 1：在 `el-row` 前增加页面头部**

在根容器内部、`el-row` 前插入：

```vue
<div class="ui-page-head server-monitor-head">
  <div>
    <h2 class="ui-page-title">服务监控</h2>
    <p class="ui-page-desc">查看当前服务运行环境、JVM、CPU、内存与磁盘状态。</p>
  </div>
  <el-button type="primary" icon="Refresh" :loading="loading" @click="getList">刷新</el-button>
</div>
```

- [ ] **步骤 2：增加错误态**

在页面头部后、数据面板前插入：

```vue
<el-alert
  v-if="loadError"
  class="server-monitor-alert"
  title="服务监控数据加载失败"
  description="请检查后端服务、登录状态或网络连接后重试。"
  type="error"
  show-icon
  :closable="false"
/>
```

- [ ] **步骤 3：隐藏错误时的空数据误导**

将数据主体包一层：

```vue
<el-row v-if="!loadError" :gutter="12">
  ...
</el-row>
```

注意：只包现有监控卡片，不包页面头部和错误提示。

- [ ] **步骤 4：手工验证错误态**

运行前端开发服务：

```bash
cd ui-admin && npm run dev
```

通过浏览器 DevTools 临时阻断 `/monitor/server` 请求，或临时让后端不可达后打开页面。预期：

- 页面内 loading 会结束。
- 页面显示错误提示。
- 顶栏、侧栏不被黑色遮罩锁住。
- 点击「刷新」会重新请求。

### 任务 4：重构卡片结构，去掉旧表格皮肤依赖

**文件：**

- 修改：`ui-admin/src/views/monitor/server/index.vue`

- [ ] **步骤 1：新增展示数据数组**

在 `getList()` 下方、`getList()` 调用前增加：

```js
const cpuRows = computed(() => [
  { label: '核心数', value: server.value.cpu?.cpuNum },
  { label: '用户使用率', value: formatPercent(server.value.cpu?.used) },
  { label: '系统使用率', value: formatPercent(server.value.cpu?.sys) },
  { label: '当前空闲率', value: formatPercent(server.value.cpu?.free) }
])

const memoryRows = computed(() => [
  { label: '总内存', mem: formatSize(server.value.mem?.total, 'G'), jvm: formatSize(server.value.jvm?.total, 'M') },
  { label: '已用内存', mem: formatSize(server.value.mem?.used, 'G'), jvm: formatSize(server.value.jvm?.used, 'M') },
  { label: '剩余内存', mem: formatSize(server.value.mem?.free, 'G'), jvm: formatSize(server.value.jvm?.free, 'M') },
  {
    label: '使用率',
    mem: formatPercent(server.value.mem?.usage),
    jvm: formatPercent(server.value.jvm?.usage),
    memDanger: isDangerUsage(server.value.mem?.usage),
    jvmDanger: isDangerUsage(server.value.jvm?.usage)
  }
])

const systemRows = computed(() => [
  { label: '服务器名称', value: server.value.sys?.computerName },
  { label: '操作系统', value: server.value.sys?.osName },
  { label: '服务器 IP', value: server.value.sys?.computerIp },
  { label: '系统架构', value: server.value.sys?.osArch }
])

const jvmRows = computed(() => [
  { label: 'Java 名称', value: server.value.jvm?.name },
  { label: 'Java 版本', value: server.value.jvm?.version },
  { label: '启动时间', value: server.value.jvm?.startTime },
  { label: '运行时长', value: server.value.jvm?.runTime },
  { label: '安装路径', value: server.value.jvm?.home, wide: true },
  { label: '项目路径', value: server.value.sys?.userDir, wide: true },
  { label: '运行参数', value: server.value.jvm?.inputArgs, wide: true }
])

function formatPercent(value) {
  return value === undefined || value === null ? '-' : `${value}%`
}

function formatSize(value, unit) {
  return value === undefined || value === null ? '-' : `${value}${unit}`
}

function formatText(value) {
  return value === undefined || value === null || value === '' ? '-' : value
}

function isDangerUsage(value) {
  return Number(value) > 80
}
```

- [ ] **步骤 2：把 CPU 卡片改成主题化信息表**

用以下模板替换 CPU 卡片内部内容：

```vue
<el-col :xs="24" :sm="24" :md="12" class="server-monitor-col">
  <section class="ui-panel-card server-panel">
    <div class="server-panel__header">
      <Cpu class="server-panel__icon" />
      <span>CPU</span>
    </div>
    <div class="server-info-table">
      <div class="server-info-row server-info-row--head">
        <span>属性</span>
        <span>值</span>
      </div>
      <div v-for="row in cpuRows" :key="row.label" class="server-info-row">
        <span class="server-info-label">{{ row.label }}</span>
        <span class="server-info-value">{{ formatText(row.value) }}</span>
      </div>
    </div>
  </section>
</el-col>
```

- [ ] **步骤 3：把内存卡片改成主题化信息表**

用以下模板替换内存卡片内部内容：

```vue
<el-col :xs="24" :sm="24" :md="12" class="server-monitor-col">
  <section class="ui-panel-card server-panel">
    <div class="server-panel__header">
      <Tickets class="server-panel__icon" />
      <span>内存</span>
    </div>
    <div class="server-info-table server-info-table--three">
      <div class="server-info-row server-info-row--head">
        <span>属性</span>
        <span>内存</span>
        <span>JVM</span>
      </div>
      <div v-for="row in memoryRows" :key="row.label" class="server-info-row">
        <span class="server-info-label">{{ row.label }}</span>
        <span class="server-info-value" :class="{ 'is-danger': row.memDanger }">{{ row.mem }}</span>
        <span class="server-info-value" :class="{ 'is-danger': row.jvmDanger }">{{ row.jvm }}</span>
      </div>
    </div>
  </section>
</el-col>
```

- [ ] **步骤 4：把服务器信息和 JVM 信息改成描述网格**

服务器信息：

```vue
<el-col :span="24" class="server-monitor-col">
  <section class="ui-panel-card server-panel">
    <div class="server-panel__header">
      <Monitor class="server-panel__icon" />
      <span>服务器信息</span>
    </div>
    <div class="server-description-grid">
      <div v-for="row in systemRows" :key="row.label" class="server-description-item">
        <span class="server-info-label">{{ row.label }}</span>
        <span class="server-info-value">{{ formatText(row.value) }}</span>
      </div>
    </div>
  </section>
</el-col>
```

JVM 信息：

```vue
<el-col :span="24" class="server-monitor-col">
  <section class="ui-panel-card server-panel">
    <div class="server-panel__header">
      <CoffeeCup class="server-panel__icon" />
      <span>Java 虚拟机信息</span>
    </div>
    <div class="server-description-grid">
      <div
        v-for="row in jvmRows"
        :key="row.label"
        class="server-description-item"
        :class="{ 'server-description-item--wide': row.wide }"
      >
        <span class="server-info-label">{{ row.label }}</span>
        <span class="server-info-value server-info-value--wrap">{{ formatText(row.value) }}</span>
      </div>
    </div>
  </section>
</el-col>
```

- [ ] **步骤 5：把磁盘状态改成 Element Plus 表格**

用以下模板替换磁盘状态卡片：

```vue
<el-col :span="24" class="server-monitor-col">
  <section class="ui-panel-card server-panel">
    <div class="server-panel__header">
      <MessageBox class="server-panel__icon" />
      <span>磁盘状态</span>
    </div>
    <div class="server-disk-table">
      <el-table :data="server.sysFiles || []" style="width: 100%">
        <el-table-column label="盘符路径" prop="dirName" min-width="180" show-overflow-tooltip />
        <el-table-column label="文件系统" prop="sysTypeName" min-width="120" show-overflow-tooltip />
        <el-table-column label="盘符类型" prop="typeName" min-width="180" show-overflow-tooltip />
        <el-table-column label="总大小" prop="total" width="110" />
        <el-table-column label="可用大小" prop="free" width="110" />
        <el-table-column label="已用大小" prop="used" width="110" />
        <el-table-column label="已用百分比" width="120">
          <template #default="scope">
            <span class="server-info-value" :class="{ 'is-danger': isDangerUsage(scope.row.usage) }">
              {{ formatPercent(scope.row.usage) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</el-col>
```

- [ ] **步骤 6：运行构建确认模板可编译**

运行：

```bash
cd ui-admin && npm run build:prod
```

预期：退出码为 0。

### 任务 5：补齐页面级样式和响应式行为

**文件：**

- 修改：`ui-admin/src/views/monitor/server/index.vue`

- [ ] **步骤 1：替换 scoped style**

将当前 style：

```vue
<style scoped>
.server-monitor-table {
  width: 100%;
}

.cell-word-break .cell {
  word-break: break-all;
}
</style>
```

替换为：

```vue
<style scoped>
.server-monitor-page {
  min-height: 100%;
}

.server-monitor-head {
  align-items: center;
}

.server-monitor-alert {
  margin-bottom: 12px;
}

.server-monitor-col {
  margin-bottom: 12px;
}

.server-panel {
  height: 100%;
  overflow: hidden;
}

.server-panel__header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
  padding: 0 16px;
  color: var(--ui-text-primary);
  font-size: 14px;
  font-weight: 700;
  border-bottom: 1px solid var(--ui-border);
  background: var(--ui-bg-panel-muted);
}

.server-panel__icon {
  width: 16px;
  height: 16px;
  color: var(--ui-primary);
}

.server-info-table {
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) minmax(0, 1.2fr);
}

.server-info-table--three {
  grid-template-columns: minmax(110px, 0.8fr) minmax(0, 1fr) minmax(0, 1fr);
}

.server-info-row {
  display: contents;
}

.server-info-row > span {
  min-height: 42px;
  padding: 11px 14px;
  color: var(--ui-text-regular);
  line-height: 1.45;
  border-bottom: 1px solid var(--ui-table-border);
}

.server-info-row:last-child > span {
  border-bottom: 0;
}

.server-info-row--head > span {
  min-height: 40px;
  color: var(--ui-text-secondary);
  font-size: 13px;
  font-weight: 700;
  background: var(--ui-table-header-bg);
}

.server-info-label {
  color: var(--ui-text-secondary);
  font-weight: 600;
}

.server-info-value {
  color: var(--ui-text-primary);
  font-variant-numeric: tabular-nums;
}

.server-info-value.is-danger {
  color: var(--ui-danger);
  font-weight: 700;
}

.server-info-value--wrap {
  overflow-wrap: anywhere;
  word-break: break-word;
}

.server-description-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.server-description-item {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 12px;
  min-height: 44px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--ui-table-border);
}

.server-description-item:nth-child(odd) {
  border-right: 1px solid var(--ui-table-border);
}

.server-description-item--wide {
  grid-column: 1 / -1;
  border-right: 0;
}

.server-disk-table {
  overflow-x: auto;
}

:deep(.server-disk-table .el-table) {
  min-width: 900px;
}

@media (max-width: 768px) {
  .server-monitor-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .server-monitor-head .el-button {
    width: 100%;
  }

  .server-description-grid {
    grid-template-columns: 1fr;
  }

  .server-description-item {
    grid-template-columns: 96px minmax(0, 1fr);
  }

  .server-description-item:nth-child(odd) {
    border-right: 0;
  }
}
</style>
```

- [ ] **步骤 2：检查颜色全部来自 token**

运行：

```bash
rg -n "#[0-9a-fA-F]{3,8}|rgba?\\(" ui-admin/src/views/monitor/server/index.vue
```

预期：没有新增硬编码颜色。若出现颜色值，改为 `--ui-*` 或 Element Plus 语义 token。

- [ ] **步骤 3：运行构建**

运行：

```bash
cd ui-admin && npm run build:prod
```

预期：退出码为 0。

### 任务 6：浏览器验证与截图记录

**文件：**

- 不修改业务文件。
- 验证记录可写入本次工作最终回复；如团队要求留档，可新增 `docs/audit/YYYY-MM-DD-server-monitor-page-polish.md`。

- [ ] **步骤 1：启动前端开发服务**

运行：

```bash
cd ui-admin && npm run dev
```

预期：Vite 输出本地访问地址，例如：

```text
Local: http://localhost:80/
```

如果 80 端口被占用，使用 Vite 输出的实际端口。

- [ ] **步骤 2：启动后端服务**

运行：

```bash
mvn -pl manzhushaka-admin -am spring-boot:run
```

预期：后端启动完成，并能响应 `/monitor/server`。如果本地数据库、Redis 或配置缺失导致后端无法启动，在最终回复中明确说明未完成浏览器联调，并保留前端构建验证结果。

- [ ] **步骤 3：桌面端验证**

在浏览器打开服务监控页，使用 1440 x 900 视口验证：

- 页面打开时只有内容区显示 loading。
- 页面顶部有标题、说明和刷新按钮。
- CPU 与内存双列展示。
- 服务器信息、JVM 信息、磁盘状态层级清晰。
- 点击「刷新」按钮不会出现全屏黑色遮罩。

- [ ] **步骤 4：移动端验证**

使用 390 x 844 视口验证：

- CPU 与内存变成单列。
- 头部刷新按钮不挤压标题。
- 页面整体不出现横向滚动。
- 磁盘表格只在表格容器内部横向滚动。
- JVM 长路径和运行参数换行，不撑破页面。

- [ ] **步骤 5：主题验证**

分别切换 `cool-tower`、`amber-command`、`gold-ledger` 后验证：

- 卡片背景、边框、表头、正文颜色跟随主题。
- 危险状态使用 `--ui-danger`，在 3 套主题下可读。
- 表格 hover 和边框不出现旧若依蓝灰混搭。

- [ ] **步骤 6：失败态验证**

用浏览器 DevTools 阻断 `/monitor/server` 或停止后端后刷新页面。预期：

- loading 结束。
- 出现「服务监控数据加载失败」错误提示。
- 点击「刷新」可以重新请求。
- 页面没有残留全屏遮罩。

## 5. 回归检查

- [ ] **步骤 1：确认只修改服务监控页**

运行：

```bash
git diff --name-only
```

预期：业务实现阶段只出现：

```text
ui-admin/src/views/monitor/server/index.vue
```

如果确实修改了 `theme-tokens.scss`，需要在最终回复中说明原因和影响范围。

- [ ] **步骤 2：生产构建验证**

运行：

```bash
cd ui-admin && npm run build:prod
```

预期：退出码为 0。

- [ ] **步骤 3：权限与接口闭环检查**

运行：

```bash
rg -n "monitor:server:list|/monitor/server|monitor/server/index" sql/manzhushaka_db_init.sql manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/monitor/ServerController.java ui-admin/src/api/monitor/server.js ui-admin/src/views/monitor/server/index.vue
```

预期：

- `sql/manzhushaka_db_init.sql` 中仍有 `monitor:server:list` 和 `monitor/server/index`。
- `ServerController` 中仍有 `@PreAuthorize("@ss.hasPermi('monitor:server:list')")`。
- `ui-admin/src/api/monitor/server.js` 仍请求 `/monitor/server`。

## 6. 不做事项

- 不移除 `Server.java` 中的 `Util.sleep(OSHI_WAIT_SECOND)`。
- 不新增缓存接口或异步采样任务。
- 不修改 `ServerController` 权限、路径或返回结构。
- 不新增菜单、按钮权限或 SQL 初始化数据。
- 不重构其他监控页面，包括缓存监控、在线用户、操作日志和登录日志。

## 7. 交付说明模板

实现完成后的最终回复按以下格式说明：

```markdown
你好，我是 manzhushaka (≧▽≦)

已优化服务监控页：去掉全屏遮罩式等待，改为页面内加载；请求失败时会关闭加载态并显示可重试错误提示；页面结构已迁到主题化信息面板和 Element Plus 表格，移动端磁盘表格在容器内滚动。

验证：
- `cd ui-admin && npm run build:prod` 通过。
- 桌面端与 390 x 844 移动端已检查。
- `/monitor/server` 失败态已检查。
```
