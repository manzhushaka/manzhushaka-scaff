<template>
  <div class="app-container ui-list-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px" class="ui-filter-card">
      <el-form-item label="消息类型" prop="messageType">
        <el-input v-model="queryParams.messageType" placeholder="请输入消息类型" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="Stream" prop="streamKey">
        <el-input v-model="queryParams.streamKey" placeholder="请输入 Stream Key" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务Key" prop="businessKey">
        <el-input v-model="queryParams.businessKey" placeholder="请输入业务 Key" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="消息状态" clearable style="width: 140px">
          <el-option v-for="dict in mqStatusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="消费时间" style="width: 308px">
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
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:mqlog:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" @click="handleClean" v-hasPermi="['monitor:mqlog:remove']">清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['monitor:mqlog:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <div class="ui-table-card">
      <el-table v-loading="loading" :data="mqLogList" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="编号" align="center" prop="messageLogId" width="90" />
        <el-table-column label="消息类型" prop="messageType" min-width="170" :show-overflow-tooltip="true" />
        <el-table-column label="Stream" prop="streamKey" min-width="260" :show-overflow-tooltip="true" />
        <el-table-column label="业务Key" prop="businessKey" min-width="220" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <dict-tag :options="mqStatusOptions" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="重试次数" align="center" prop="retryTimes" width="90" />
        <el-table-column label="首次消费" align="center" prop="firstConsumeTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.firstConsumeTime) }}</template>
        </el-table-column>
        <el-table-column label="最后消费" align="center" prop="lastConsumeTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.lastConsumeTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="140">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['monitor:mqlog:query']">详细</el-button>
            <el-button link type="primary" icon="Tickets" @click="handleDetailList(scope.row)" v-hasPermi="['monitor:mqlog:query']">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 消息详情对话框 -->
    <el-dialog title="消息详情" v-model="detailVisible" width="820px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="消息类型" :span="2">{{ detailRow.messageType }}</el-descriptions-item>
        <el-descriptions-item label="Stream">{{ detailRow.streamKey }}</el-descriptions-item>
        <el-descriptions-item label="消息ID">{{ detailRow.messageId }}</el-descriptions-item>
        <el-descriptions-item label="消费者组">{{ detailRow.consumerGroup }}</el-descriptions-item>
        <el-descriptions-item label="业务Key">{{ detailRow.businessKey }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <dict-tag :options="mqStatusOptions" :value="detailRow.status" />
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ detailRow.retryTimes }} / {{ detailRow.maxRetryTimes }}</el-descriptions-item>
        <el-descriptions-item label="首次消费">{{ parseTime(detailRow.firstConsumeTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后消费">{{ parseTime(detailRow.lastConsumeTime) }}</el-descriptions-item>
        <el-descriptions-item label="成功时间">{{ parseTime(detailRow.successTime) }}</el-descriptions-item>
        <el-descriptions-item label="死信时间">{{ parseTime(detailRow.deadLetterTime) }}</el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2"><pre class="msg-pre">{{ detailRow.payload }}</pre></el-descriptions-item>
        <el-descriptions-item label="错误消息" :span="2"><pre class="msg-pre">{{ detailRow.lastErrorMsg || '无' }}</pre></el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 执行明细对话框 -->
    <el-dialog title="执行明细" v-model="detailListVisible" width="960px" append-to-body>
      <el-table v-loading="detailLoading" :data="detailList" border>
        <el-table-column label="序号" align="center" type="index" width="60" />
        <el-table-column label="尝试次数" align="center" prop="attemptNo" width="90" />
        <el-table-column label="消费者" prop="consumerName" width="160" :show-overflow-tooltip="true" />
        <el-table-column label="状态" align="center" prop="status" width="80">
          <template #default="scope">
            <dict-tag :options="detailStatusOptions" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="开始时间" align="center" prop="startTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="耗时" align="center" prop="costTime" width="90">
          <template #default="scope">{{ scope.row.costTime }}ms</template>
        </el-table-column>
        <el-table-column label="错误消息" prop="errorMsg" min-width="200" :show-overflow-tooltip="true" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="MqLog">
import { listMqLog, getMqLog, listMqLogDetail, delMqLog, cleanMqLog } from '@/api/monitor/mqLog'

const { proxy } = getCurrentInstance()

const mqLogList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref([])

// 主台账详情
const detailVisible = ref(false)
const detailRow = ref({})

// 执行明细
const detailListVisible = ref(false)
const detailLoading = ref(false)
const detailList = ref([])

// 状态字典
const mqStatusOptions = ref([
  { value: '0', label: '处理中', type: 'warning' },
  { value: '1', label: '成功', type: 'success' },
  { value: '2', label: '失败', type: 'danger' },
  { value: '3', label: '跳过', type: 'info' },
  { value: '4', label: '死信', type: 'danger' }
])

const detailStatusOptions = ref([
  { value: '0', label: '处理中', type: 'warning' },
  { value: '1', label: '成功', type: 'success' },
  { value: '2', label: '失败', type: 'danger' },
  { value: '3', label: '跳过', type: 'info' }
])

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  messageType: undefined,
  streamKey: undefined,
  businessKey: undefined,
  status: undefined
})

function getList() {
  loading.value = true
  listMqLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    mqLogList.value = response.rows
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
  ids.value = selection.map(item => item.messageLogId)
  multiple.value = !selection.length
}

function handleDetail(row) {
  getMqLog(row.messageLogId).then(response => {
    detailRow.value = response.data
    detailVisible.value = true
  })
}

function handleDetailList(row) {
  detailLoading.value = true
  detailListVisible.value = true
  listMqLogDetail(row.messageLogId).then(response => {
    detailList.value = response.data
    detailLoading.value = false
  })
}

function handleDelete(row) {
  const messageLogIds = row.messageLogId || ids.value
  proxy.$modal.confirm('是否确认删除消息队列台账编号为"' + messageLogIds + '"的数据项?').then(function () {
    return delMqLog(messageLogIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleClean() {
  proxy.$modal.confirm('是否确认清空所有消息队列台账数据项?').then(function () {
    return cleanMqLog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('清空成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('monitor/mqLog/export', { ...queryParams.value }, `mq_log_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
.msg-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  line-height: 1.6;
  max-height: 300px;
  overflow-y: auto;
}
</style>
