<template>
  <div class="app-container runtime-log-page">
    <section class="runtime-log-card">
      <a-tabs v-model:active-key="activeLevelTab" class="runtime-level-tabs" @change="handleLevelTabChange">
        <a-tab-pane title="全部" key="ALL" />
        <a-tab-pane title="INFO" key="INFO" />
        <a-tab-pane title="WARN" key="WARN" />
        <a-tab-pane title="ERROR" key="ERROR" />
      </a-tabs>

      <div class="runtime-log-panel">
        <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
          <a-form-item label="关键字" field="keyword">
            <a-input v-model="queryParams.keyword" placeholder="请输入关键字" allow-clear style="width: 240px" @keyup.enter="handleQuery" />
          </a-form-item>
          <a-form-item label="读取行数" field="lineCount">
            <a-input-number v-model="queryParams.lineCount" :min="50" :max="5000" :step="100" controls-position="right" style="width: 160px" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
            <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
          </a-form-item>
        </a-form>

        <a-row class="ui-action-bar">
          <a-col :span="1.5">
            <a-button status="warning" @click="handleDownload" v-hasPermi="['monitor:runtimelog:download']" type="outline"><template #icon><Download /></template>下载</a-button>
          </a-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </a-row>

        <div class="ui-table-card runtime-log-table-card">
          <a-table :loading="loading" :data="runtimeLogList" style="width: 100%" :pagination="false">
            <a-table-column title="行号" align="center" data-index="lineNumber" width="90" />
            <a-table-column title="时间" align="center" data-index="time" width="130" />
            <a-table-column title="级别" align="center" data-index="level" width="90">
              <template #cell="{ record, rowIndex }">
                <a-tag :color="levelTagColor(record.level)">{{ record.level }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column title="内容" data-index="content" ellipsis min-width="420" tooltip />
            <a-table-column title="堆栈" align="center" width="90">
              <template #cell="{ record, rowIndex }">
                <a-button @click="handleDetail(record)" v-hasPermi="['monitor:runtimelog:query']"><template #icon><View /></template>查看</a-button>
              </template>
            </a-table-column>
          </a-table>
        </div>
      </div>
    </section>

    <a-modal title="运行日志详情" v-model:visible="detailVisible" width="860px" render-to-body :footer="false">
      <pre class="runtime-log-pre">{{ detailContent }}</pre>
    </a-modal>
  </div>
</template>

<script setup name="RuntimeLog">
import { listRuntimeLog } from '@/api/monitor/runtimeLog'

const { proxy } = getCurrentInstance()

const runtimeLogList = ref([])
const loading = ref(false)
const showSearch = ref(true)
const detailVisible = ref(false)
const detailContent = ref('')
const activeLevelTab = ref('ALL')

const queryParams = ref({
  fileName: 'sys-error.log',
  level: undefined,
  keyword: undefined,
  lineCount: 500
})

function getList() {
  loading.value = true
  listRuntimeLog(queryParams.value).then(response => {
    runtimeLogList.value = response.data || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  getList()
}

function handleLevelTabChange(level) {
  queryParams.value.level = level === 'ALL' ? undefined : level
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.fileName = 'sys-error.log'
  queryParams.value.level = undefined
  queryParams.value.lineCount = 500
  activeLevelTab.value = 'ALL'
  getList()
}

function handleDetail(row) {
  detailContent.value = [row.content, row.stackTraceBlock].filter(Boolean).join('\n')
  detailVisible.value = true
}

function handleDownload() {
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  window.open(baseUrl + '/monitor/runtimeLog/download?fileName=' + encodeURIComponent(queryParams.value.fileName), '_blank')
}

function levelTagColor(level) {
  if (level === 'ERROR') {
    return 'red'
  }
  if (level === 'WARN') {
    return 'orange'
  }
  return 'green'
}

getList()
</script>

<style lang="scss" scoped>
.runtime-log-page {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.runtime-log-card {
  overflow: hidden;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: var(--ui-shadow-panel);
}

.runtime-log-panel {
  padding: 18px 20px 20px;
}

.runtime-level-tabs {
  :deep(.arco-tabs-nav) {
    margin: 0;
    padding: 0 20px;
    border-bottom: 1px solid var(--ui-border);
    background: var(--ui-bg-panel-muted);
  }

  :deep(.arco-tabs-nav::before) {
    display: none;
  }

  :deep(.arco-tabs-tab) {
    height: 52px;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.arco-tabs-content) {
    display: none;
  }
}

.runtime-log-table-card {
  margin-bottom: 0;
}

.runtime-log-pre {
  max-height: 62vh;
  overflow: auto;
  margin: 0;
  padding: 12px;
  border-radius: 6px;
  background: #111827;
  color: #e5e7eb;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .runtime-level-tabs {
    :deep(.arco-tabs-nav) {
      padding: 0 14px;
    }
  }

  .runtime-log-panel {
    padding: 14px;
  }
}
</style>
