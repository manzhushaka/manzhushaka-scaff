<template>
  <div class="app-container ui-list-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="96px" class="ui-filter-card slow-sql-filter">
      <el-form-item label="SQL关键字" prop="sqlText">
        <el-input v-model="queryParams.sqlText" placeholder="请输入 SQL 关键字" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="Mapper" prop="mapperId">
        <el-input v-model="queryParams.mapperId" placeholder="请输入 Mapper" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="最小耗时" prop="costTime">
        <el-input-number v-model="queryParams.costTime" :min="0" :step="100" controls-position="right" style="width: 160px" />
      </el-form-item>
      <el-form-item label="执行时间" style="width: 308px">
        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD HH:mm:ss" type="daterange" range-separator="-"
          start-placeholder="开始日期" end-placeholder="结束日期"
          :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 ui-action-bar">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:slowsql:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" @click="handleClean" v-hasPermi="['monitor:slowsql:remove']">清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['monitor:slowsql:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <div class="ui-table-card">
      <el-table v-loading="loading" :data="slowSqlList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="编号" align="center" prop="slowSqlId" width="90" />
        <el-table-column label="Mapper" prop="mapperId" :show-overflow-tooltip="true" min-width="260" />
        <el-table-column label="SQL" prop="sqlText" :show-overflow-tooltip="true" min-width="280" />
        <el-table-column label="数据源" align="center" prop="dataSourceName" width="100" />
        <el-table-column label="耗时" align="center" prop="costTime" width="110">
          <template #default="scope">{{ scope.row.costTime }}毫秒</template>
        </el-table-column>
        <el-table-column label="执行时间" align="center" prop="executeTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.executeTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="90">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['monitor:slowsql:query']">详细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="慢 SQL 详情" v-model="detailVisible" width="820px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Mapper" :span="2">{{ detailRow.mapperId }}</el-descriptions-item>
        <el-descriptions-item label="数据源">{{ detailRow.dataSourceName }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailRow.costTime }}毫秒</el-descriptions-item>
        <el-descriptions-item label="执行时间" :span="2">{{ parseTime(detailRow.executeTime) }}</el-descriptions-item>
        <el-descriptions-item label="SQL" :span="2"><pre class="sql-pre">{{ detailRow.sqlText }}</pre></el-descriptions-item>
        <el-descriptions-item label="错误消息" :span="2"><pre class="sql-pre">{{ detailRow.errorMsg }}</pre></el-descriptions-item>
      </el-descriptions>
    </el-dialog>
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

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.slowSqlId)
  multiple.value = !selection.length
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
.slow-sql-filter :deep(.el-form-item__label) {
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
