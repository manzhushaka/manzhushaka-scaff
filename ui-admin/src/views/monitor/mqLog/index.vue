<template>
  <div class="app-container ui-list-page">
    <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '90px' }" class="ui-filter-card">
      <a-form-item label="消息类型" field="messageType">
        <a-input v-model="queryParams.messageType" placeholder="请输入消息类型" allow-clear style="width: 200px" @keyup.enter="handleQuery" />
      </a-form-item>
      <a-form-item label="Stream" field="streamKey">
        <a-input v-model="queryParams.streamKey" placeholder="请输入 Stream Key" allow-clear style="width: 200px" @keyup.enter="handleQuery" />
      </a-form-item>
      <a-form-item label="业务Key" field="businessKey">
        <a-input v-model="queryParams.businessKey" placeholder="请输入业务 Key" allow-clear style="width: 200px" @keyup.enter="handleQuery" />
      </a-form-item>
      <a-form-item label="状态" field="status">
        <a-select v-model="queryParams.status" placeholder="消息状态" allow-clear style="width: 140px">
          <a-option v-for="dict in mqStatusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
        </a-select>
      </a-form-item>
      <a-form-item label="消费时间" style="width: 308px">
        <a-range-picker v-model="dateRange" value-format="YYYY-MM-DD HH:mm:ss" separator="-"
          :placeholder="['开始日期', '结束日期']"
          :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
        <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
      </a-form-item>
    </a-form>

    <a-row class="ui-action-bar">
      <a-col :span="1.5">
        <a-button status="danger" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:mqlog:remove']" type="outline"><template #icon><Delete /></template>删除</a-button>
      </a-col>
      <a-col :span="1.5">
        <a-button status="danger" @click="handleClean" v-hasPermi="['monitor:mqlog:remove']" type="outline"><template #icon><Delete /></template>清空</a-button>
      </a-col>
      <a-col :span="1.5">
        <a-button status="warning" @click="handleExport" v-hasPermi="['monitor:mqlog:export']" type="outline"><template #icon><Download /></template>导出</a-button>
      </a-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </a-row>

    <div class="ui-table-card">
      <a-table :loading="loading" :data="mqLogList" style="width: 100%" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.messageLogId" :pagination="false" @selection-change="handleSelectionChange">
        <a-table-column title="编号" align="center" data-index="messageLogId" width="90" />
        <a-table-column title="消息类型" data-index="messageType" min-width="170" ellipsis tooltip />
        <a-table-column title="Stream" data-index="streamKey" min-width="260" ellipsis tooltip />
        <a-table-column title="业务Key" data-index="businessKey" min-width="220" ellipsis tooltip />
        <a-table-column title="状态" align="center" data-index="status" width="100">
          <template #cell="{ record, rowIndex }">
            <dict-tag :options="mqStatusOptions" :value="record.status" />
          </template>
        </a-table-column>
        <a-table-column title="重试次数" align="center" data-index="retryTimes" width="90" />
        <a-table-column title="首次消费" align="center" data-index="firstConsumeTime" width="180">
          <template #cell="{ record, rowIndex }">{{ parseTime(record.firstConsumeTime) }}</template>
        </a-table-column>
        <a-table-column title="最后消费" align="center" data-index="lastConsumeTime" width="180">
          <template #cell="{ record, rowIndex }">{{ parseTime(record.lastConsumeTime) }}</template>
        </a-table-column>
        <a-table-column title="操作" align="center" width="140">
          <template #cell="{ record, rowIndex }">
            <a-button @click="handleDetail(record)" v-hasPermi="['monitor:mqlog:query']"><template #icon><View /></template>详细</a-button>
            <a-button @click="handleDetailList(record)" v-hasPermi="['monitor:mqlog:query']"><template #icon><Tickets /></template>明细</a-button>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 消息详情对话框 -->
    <a-modal title="消息详情" v-model:visible="detailVisible" width="820px" render-to-body :footer="false">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="消息类型" :span="2">{{ detailRow.messageType }}</a-descriptions-item>
        <a-descriptions-item label="Stream">{{ detailRow.streamKey }}</a-descriptions-item>
        <a-descriptions-item label="消息ID">{{ detailRow.messageId }}</a-descriptions-item>
        <a-descriptions-item label="消费者组">{{ detailRow.consumerGroup }}</a-descriptions-item>
        <a-descriptions-item label="业务Key">{{ detailRow.businessKey }}</a-descriptions-item>
        <a-descriptions-item label="状态" :span="2">
          <dict-tag :options="mqStatusOptions" :value="detailRow.status" />
        </a-descriptions-item>
        <a-descriptions-item label="重试次数">{{ detailRow.retryTimes }} / {{ detailRow.maxRetryTimes }}</a-descriptions-item>
        <a-descriptions-item label="首次消费">{{ parseTime(detailRow.firstConsumeTime) }}</a-descriptions-item>
        <a-descriptions-item label="最后消费">{{ parseTime(detailRow.lastConsumeTime) }}</a-descriptions-item>
        <a-descriptions-item label="成功时间">{{ parseTime(detailRow.successTime) }}</a-descriptions-item>
        <a-descriptions-item label="死信时间">{{ parseTime(detailRow.deadLetterTime) }}</a-descriptions-item>
        <a-descriptions-item label="消息内容" :span="2"><pre class="msg-pre">{{ detailRow.payload }}</pre></a-descriptions-item>
        <a-descriptions-item label="错误消息" :span="2"><pre class="msg-pre">{{ detailRow.lastErrorMsg || '无' }}</pre></a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 执行明细对话框 -->
    <a-modal title="执行明细" v-model:visible="detailListVisible" width="960px" render-to-body :footer="false">
      <a-table :loading="detailLoading" :data="detailList" bordered :pagination="false">
        <a-table-column title="序号" align="center" width="60">
          <template #cell="{ rowIndex }">{{ rowIndex + 1 }}</template>
        </a-table-column>
        <a-table-column title="尝试次数" align="center" data-index="attemptNo" width="90" />
        <a-table-column title="消费者" data-index="consumerName" width="160" ellipsis tooltip />
        <a-table-column title="状态" align="center" data-index="status" width="80">
          <template #cell="{ record, rowIndex }">
            <dict-tag :options="detailStatusOptions" :value="record.status" />
          </template>
        </a-table-column>
        <a-table-column title="开始时间" align="center" data-index="startTime" width="180">
          <template #cell="{ record, rowIndex }">{{ parseTime(record.startTime) }}</template>
        </a-table-column>
        <a-table-column title="耗时" align="center" data-index="costTime" width="90">
          <template #cell="{ record, rowIndex }">{{ record.costTime }}ms</template>
        </a-table-column>
        <a-table-column title="错误消息" data-index="errorMsg" min-width="200" ellipsis tooltip />
      </a-table>
    </a-modal>
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

function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  multiple.value = !selectedKeys.length
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
