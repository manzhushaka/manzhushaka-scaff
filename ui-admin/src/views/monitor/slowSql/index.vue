<template>
  <div class="app-container ui-list-page">
    <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '96px' }" class="ui-filter-card slow-sql-filter">
      <a-form-item label="SQL关键字" field="sqlText">
        <a-input v-model="queryParams.sqlText" placeholder="请输入 SQL 关键字" allow-clear style="width: 240px" @keyup.enter="handleQuery" />
      </a-form-item>
      <a-form-item label="Mapper" field="mapperId">
        <a-input v-model="queryParams.mapperId" placeholder="请输入 Mapper" allow-clear style="width: 240px" @keyup.enter="handleQuery" />
      </a-form-item>
      <a-form-item label="最小耗时" field="costTime">
        <a-input-number v-model="queryParams.costTime" :min="0" :step="100" controls-position="right" style="width: 160px" />
      </a-form-item>
      <a-form-item label="执行时间" style="width: 308px">
        <a-range-picker v-model="dateRange" value-format="YYYY-MM-DD HH:mm:ss" separator="-"
          :placeholder="['开始日期', '结束日期']"
          :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
        <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
      </a-form-item>
    </a-form>

    <a-row :gutter="10" class="mb8 ui-action-bar">
      <a-col :span="1.5">
        <a-button status="danger" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:slowsql:remove']" type="outline"><template #icon><Delete /></template>删除</a-button>
      </a-col>
      <a-col :span="1.5">
        <a-button status="danger" @click="handleClean" v-hasPermi="['monitor:slowsql:remove']" type="outline"><template #icon><Delete /></template>清空</a-button>
      </a-col>
      <a-col :span="1.5">
        <a-button status="warning" @click="handleExport" v-hasPermi="['monitor:slowsql:export']" type="outline"><template #icon><Download /></template>导出</a-button>
      </a-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </a-row>

    <div class="ui-table-card">
      <a-table :loading="loading" :data="slowSqlList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.slowSqlId" :pagination="false" @selection-change="handleSelectionChange">
        <a-table-column title="编号" align="center" data-index="slowSqlId" width="90" />
        <a-table-column title="Mapper" data-index="mapperId" ellipsis min-width="260" tooltip />
        <a-table-column title="SQL" data-index="sqlText" ellipsis min-width="280" tooltip />
        <a-table-column title="数据源" align="center" data-index="dataSourceName" width="100" />
        <a-table-column title="耗时" align="center" data-index="costTime" width="110">
          <template #cell="{ record, rowIndex }">{{ record.costTime }}毫秒</template>
        </a-table-column>
        <a-table-column title="执行时间" align="center" data-index="executeTime" width="180">
          <template #cell="{ record, rowIndex }">{{ parseTime(record.executeTime) }}</template>
        </a-table-column>
        <a-table-column title="操作" align="center" width="90">
          <template #cell="{ record, rowIndex }">
            <a-button @click="handleDetail(record)" v-hasPermi="['monitor:slowsql:query']"><template #icon><View /></template>详细</a-button>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <a-modal title="慢 SQL 详情" v-model:visible="detailVisible" width="820px" render-to-body :footer="false">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="Mapper" :span="2">{{ detailRow.mapperId }}</a-descriptions-item>
        <a-descriptions-item label="数据源">{{ detailRow.dataSourceName }}</a-descriptions-item>
        <a-descriptions-item label="耗时">{{ detailRow.costTime }}毫秒</a-descriptions-item>
        <a-descriptions-item label="执行时间" :span="2">{{ parseTime(detailRow.executeTime) }}</a-descriptions-item>
        <a-descriptions-item label="SQL" :span="2"><pre class="sql-pre">{{ detailRow.sqlText }}</pre></a-descriptions-item>
        <a-descriptions-item label="错误消息" :span="2"><pre class="sql-pre">{{ detailRow.errorMsg }}</pre></a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup name="SlowSql">
import { listSlowSql, delSlowSql, cleanSlowSql } from '@/api/monitor/slowSql'

const { proxy } = getCurrentInstance()

const slowSqlList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const detailRow = ref({})

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  sqlText: undefined,
  mapperId: undefined,
  costTime: undefined
})

function getList() {
  loading.value = true
  listSlowSql(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    slowSqlList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  multiple.value = !selectedKeys.length
}

function handleDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function handleDelete(row) {
  const slowSqlIds = row.slowSqlId || ids.value
  proxy.$modal.confirm('是否确认删除慢 SQL 日志编号为"' + slowSqlIds + '"的数据项?').then(function () {
    return delSlowSql(slowSqlIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleClean() {
  proxy.$modal.confirm('是否确认清空所有慢 SQL 日志数据项?').then(function () {
    return cleanSlowSql()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('清空成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('monitor/slowSql/export', { ...queryParams.value }, `slow_sql_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.slow-sql-filter :deep(.arco-form-item-label) {
  white-space: nowrap;
}

.sql-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  line-height: 1.6;
}
</style>
