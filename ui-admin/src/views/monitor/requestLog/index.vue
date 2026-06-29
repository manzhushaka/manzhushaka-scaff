<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
      <el-form-item label="请求地址" prop="requestUri">
        <el-input v-model="queryParams.requestUri" placeholder="请输入请求地址" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="请求方式" prop="requestMethod">
        <el-select v-model="queryParams.requestMethod" placeholder="请求方式" clearable style="width: 160px">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户账号" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户账号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请求状态" clearable style="width: 160px">
          <el-option v-for="dict in sys_common_status" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="请求时间" style="width: 308px">
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
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:requestlog:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" @click="handleClean" v-hasPermi="['monitor:requestlog:remove']">清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['monitor:requestlog:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <div class="ui-table-card">
      <el-table v-loading="loading" :data="requestLogList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="编号" align="center" prop="requestId" width="90" />
        <el-table-column label="请求地址" prop="requestUri" :show-overflow-tooltip="true" min-width="220" />
        <el-table-column label="方式" align="center" prop="requestMethod" width="90" />
        <el-table-column label="用户账号" align="center" prop="userName" width="120" :show-overflow-tooltip="true" />
        <el-table-column label="状态码" align="center" prop="statusCode" width="90" />
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template #default="scope">
            <dict-tag :options="sys_common_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="耗时" align="center" prop="costTime" width="100">
          <template #default="scope">{{ scope.row.costTime }}毫秒</template>
        </el-table-column>
        <el-table-column label="请求时间" align="center" prop="requestTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.requestTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="90">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['monitor:requestlog:query']">详细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="请求日志详情" v-model="detailVisible" width="760px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="请求地址" :span="2">{{ detailRow.requestUri }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailRow.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="状态码">{{ detailRow.statusCode }}</el-descriptions-item>
        <el-descriptions-item label="控制器方法" :span="2">{{ detailRow.controllerMethod }}</el-descriptions-item>
        <el-descriptions-item label="用户账号">{{ detailRow.userName }}</el-descriptions-item>
        <el-descriptions-item label="请求 IP">{{ detailRow.ipaddr }}</el-descriptions-item>
        <el-descriptions-item label="查询参数" :span="2">{{ detailRow.queryString }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2"><pre class="log-pre">{{ detailRow.requestParams }}</pre></el-descriptions-item>
        <el-descriptions-item label="错误消息" :span="2"><pre class="log-pre">{{ detailRow.errorMsg }}</pre></el-descriptions-item>
        <el-descriptions-item label="User-Agent" :span="2">{{ detailRow.userAgent }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="RequestLog">
import { listRequestLog, delRequestLog, cleanRequestLog } from '@/api/monitor/requestLog'

const { proxy } = getCurrentInstance()
const { sys_common_status } = useDict('sys_common_status')

const requestLogList = ref([])
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
  requestUri: undefined,
  requestMethod: undefined,
  userName: undefined,
  status: undefined
})

function getList() {
  loading.value = true
  listRequestLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    requestLogList.value = response.rows
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
  ids.value = selection.map(item => item.requestId)
  multiple.value = !selection.length
}

function handleDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

function handleDelete(row) {
  const requestIds = row.requestId || ids.value
  proxy.$modal.confirm('是否确认删除请求日志编号为"' + requestIds + '"的数据项?').then(function () {
    return delRequestLog(requestIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleClean() {
  proxy.$modal.confirm('是否确认清空所有请求日志数据项?').then(function () {
    return cleanRequestLog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('清空成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('monitor/requestLog/export', { ...queryParams.value }, `request_log_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.log-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  line-height: 1.6;
}
</style>
