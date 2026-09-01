<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
         <a-form-item label="任务名称" field="jobName">
            <a-input
               v-model="queryParams.jobName"
               placeholder="请输入任务名称"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="任务组名" field="jobGroup">
            <a-select
               v-model="queryParams.jobGroup"
               placeholder="请选择任务组名"
               allow-clear
               style="width: 240px"
            >
               <a-option
                  v-for="dict in sys_job_group"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item label="执行状态" field="status">
            <a-select
               v-model="queryParams.status"
               placeholder="请选择执行状态"
               allow-clear
               style="width: 240px"
            >
               <a-option
                  v-for="dict in sys_common_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item label="执行时间" style="width: 308px">
            <a-range-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD"

               separator="-"
               :placeholder="['开始日期', '结束日期']"
            ></a-range-picker>
         </a-form-item>
         <a-form-item>
            <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
            <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
         </a-form-item>
      </a-form>

      <a-row class="ui-action-bar">
         <a-col :span="1.5">
            <a-button
               status="danger"


               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['monitor:job:remove']"
             type="outline"><template #icon><Delete /></template>删除</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               @click="handleClean"
               v-hasPermi="['monitor:job:remove']"
             type="outline"><template #icon><Delete /></template>清空</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleExport"
               v-hasPermi="['monitor:job:export']"
             type="outline"><template #icon><Download /></template>导出</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleClose"
             type="outline"><template #icon><Close /></template>关闭</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table :loading="loading" :data="jobLogList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.jobLogId" :pagination="false" @selection-change="handleSelectionChange">
         <a-table-column title="日志编号" width="80" align="center" data-index="jobLogId" />
         <a-table-column title="任务名称" align="center" data-index="jobName" ellipsis tooltip />
         <a-table-column title="任务组名" align="center" data-index="jobGroup" ellipsis tooltip>
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_job_group" :value="record.jobGroup" />
            </template>
         </a-table-column>
         <a-table-column title="调用目标字符串" align="center" data-index="invokeTarget" ellipsis tooltip />
         <a-table-column title="日志信息" align="center" data-index="jobMessage" ellipsis tooltip />
         <a-table-column title="执行状态" align="center" data-index="status">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_common_status" :value="record.status" />
            </template>
         </a-table-column>
         <a-table-column title="执行时间" align="center" data-index="createTime" width="180">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.createTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" width="100" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button type="text" @click="handleView(record)" v-hasPermi="['monitor:job:query']"><template #icon><View /></template>详细</a-button>
            </template>
         </a-table-column>
      </a-table>
      </div>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <!-- 调度日志详细 -->
      <job-detail v-model:visible="open" :row="form" type="log" />
   </div>
</template>

<script setup name="JobLog">
import JobDetail from './detail'
import { getJob } from "@/api/monitor/job"
import { listJobLog, delJobLog, cleanJobLog } from "@/api/monitor/jobLog"

const { proxy } = getCurrentInstance()
const { sys_common_status, sys_job_group } = useDict("sys_common_status", "sys_job_group")

const jobLogList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref([])
const route = useRoute()

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: undefined,
    dictType: undefined,
    status: undefined
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询调度日志列表 */
function getList() {
  loading.value = true
  listJobLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    jobLogList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 返回按钮
function handleClose() {
  const obj = { path: "/monitor/job" }
  proxy.$tab.closeOpenPage(obj)
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  multiple.value = !selectedKeys.length
}

/** 详细按钮操作 */
function handleView(row) {
  open.value = true
  form.value = row
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除调度日志编号为"' + ids.value + '"的数据项?').then(function () {
    return delJobLog(ids.value)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有调度日志数据项?").then(function () {
    return cleanJobLog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/jobLog/export", {
    ...queryParams.value,
  }, `job_log_${new Date().getTime()}.xlsx`)
}

(() => {
  const jobId = route.params && route.params.jobId
  if (jobId !== undefined && jobId != 0) {
    getJob(jobId).then(response => {
      queryParams.value.jobName = response.data.jobName
      queryParams.value.jobGroup = response.data.jobGroup
      getList()
    })
  } else {
    getList()
  }
})()
</script>
