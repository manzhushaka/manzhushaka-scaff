<template>
  <div class="app-container runtime-log-page">
    <section class="runtime-log-card">
      <el-tabs v-model="activeLevelTab" class="runtime-level-tabs" @tab-change="handleLevelTabChange">
        <el-tab-pane label="全部" name="ALL" />
        <el-tab-pane label="INFO" name="INFO" />
        <el-tab-pane label="WARN" name="WARN" />
        <el-tab-pane label="ERROR" name="ERROR" />
      </el-tabs>

      <div class="runtime-log-panel">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
          <el-form-item label="关键字" prop="keyword">
            <el-input v-model="queryParams.keyword" placeholder="请输入关键字" clearable style="width: 240px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="读取行数" prop="lineCount">
            <el-input-number v-model="queryParams.lineCount" :min="50" :max="5000" :step="100" controls-position="right" style="width: 160px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8 ui-action-bar">
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleDownload" v-hasPermi="['monitor:runtimelog:download']">下载</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </el-row>

        <div class="ui-table-card runtime-log-table-card">
          <el-table v-loading="loading" :data="runtimeLogList" style="width: 100%">
            <el-table-column label="行号" align="center" prop="lineNumber" width="90" />
            <el-table-column label="时间" align="center" prop="time" width="130" />
            <el-table-column label="级别" align="center" prop="level" width="90">
              <template #default="scope">
                <el-tag :type="levelTagType(scope.row.level)" effect="plain">{{ scope.row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="内容" prop="content" :show-overflow-tooltip="true" min-width="420" />
            <el-table-column label="堆栈" align="center" width="90">
              <template #default="scope">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['monitor:runtimelog:query']">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </section>

    <el-dialog title="运行日志详情" v-model="detailVisible" width="860px" append-to-body>
      <pre class="runtime-log-pre">{{ detailContent }}</pre>
    </el-dialog>
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

function levelTagType(level) {
  if (level === 'ERROR') {
    return 'danger'
  }
  if (level === 'WARN') {
    return 'warning'
  }
  return 'success'
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
  border: 1px solid var(--el-border-color-light);
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
}

.runtime-log-panel {
  padding: 18px 20px 20px;
}

.runtime-level-tabs {
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    background: linear-gradient(180deg, #f8fbff, #ffffff);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    height: 52px;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.el-tabs__content) {
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
    :deep(.el-tabs__header) {
      padding: 0 14px;
    }
  }

  .runtime-log-panel {
    padding: 14px;
  }
}
</style>
