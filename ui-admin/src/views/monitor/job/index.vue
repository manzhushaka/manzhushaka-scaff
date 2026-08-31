<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" class="ui-filter-card">
         <a-form-item label="任务名称" field="jobName">
            <a-input
               v-model="queryParams.jobName"
               placeholder="请输入任务名称"
               allow-clear
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="任务组名" field="jobGroup">
            <a-select v-model="queryParams.jobGroup" placeholder="请选择任务组名" allow-clear style="width: 200px">
               <a-option
                  v-for="dict in sys_job_group"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item label="任务状态" field="status">
            <a-select v-model="queryParams.status" placeholder="请选择任务状态" allow-clear style="width: 200px">
               <a-option
                  v-for="dict in sys_job_status"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item>
            <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
            <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
         </a-form-item>
      </a-form>

      <a-row :gutter="10" class="mb8 ui-action-bar">
         <a-col :span="1.5">
            <a-button



               @click="handleAdd"
               v-hasPermi="['monitor:job:add']"
             type="outline"><template #icon><Plus /></template>新增</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="success"


               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['monitor:job:edit']"
             type="outline"><template #icon><Edit /></template>修改</a-button>
         </a-col>
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
               status="warning"


               @click="handleExport"
               v-hasPermi="['monitor:job:export']"
             type="outline"><template #icon><Download /></template>导出</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button



               @click="handleJobLog"
               v-hasPermi="['monitor:job:query']"
             type="outline"><template #icon><Operation /></template>日志</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table :loading="loading" :data="jobList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.jobId" :pagination="false" @selection-change="handleSelectionChange">
         <a-table-column title="任务编号" width="100" align="center" data-index="jobId" />
         <a-table-column title="任务名称" align="center" ellipsis tooltip>
            <template #cell="{ record, rowIndex }">
               <a class="link-type" style="cursor:pointer" @click="handleView(record)">{{ record.jobName }}</a>
            </template>
         </a-table-column>
         <a-table-column title="任务组名" align="center" data-index="jobGroup">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_job_group" :value="record.jobGroup" />
            </template>
         </a-table-column>
         <a-table-column title="调用目标字符串" align="center" data-index="invokeTarget" ellipsis tooltip />
         <a-table-column title="cron执行表达式" align="center" data-index="cronExpression" ellipsis tooltip />
         <a-table-column title="状态" align="center">
            <template #cell="{ record, rowIndex }">
               <a-switch
                  v-model="record.status"
                  checked-value="0"
                  unchecked-value="1"
                  @change="handleStatusChange(record)"
               ></a-switch>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" width="200" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-tooltip content="修改" position="top">
                  <a-button @click="handleUpdate(record)" v-hasPermi="['monitor:job:edit']"><template #icon><Edit /></template></a-button>
               </a-tooltip>
               <a-tooltip content="删除" position="top">
                  <a-button @click="handleDelete(record)" v-hasPermi="['monitor:job:remove']"><template #icon><Delete /></template></a-button>
               </a-tooltip>
               <a-tooltip content="执行一次" position="top">
                  <a-button @click="handleRun(record)" v-hasPermi="['monitor:job:changeStatus']"><template #icon><CaretRight /></template></a-button>
               </a-tooltip>
               <a-tooltip content="调度日志" position="top">
                  <a-button @click="handleJobLog(record)" v-hasPermi="['monitor:job:query']"><template #icon><Operation /></template></a-button>
               </a-tooltip>
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

      <!-- 添加或修改定时任务对话框 -->
      <a-modal :title="title" v-model:visible="open" width="820px" render-to-body>
         <a-form ref="jobRef" :model="form" :rules="rules" :label-col-props="{ flex: '120px' }">
            <a-row>
               <a-col :span="12">
                  <a-form-item label="任务名称" field="jobName">
                     <a-input v-model="form.jobName" placeholder="请输入任务名称" />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="任务分组" field="jobGroup">
                     <a-select v-model="form.jobGroup" placeholder="请选择">
                        <a-option
                           v-for="dict in sys_job_group"
                           :key="dict.value"
                           :label="dict.label"
                           :value="dict.value"
                        ></a-option>
                     </a-select>
                  </a-form-item>
               </a-col>
               <a-col :span="24">
                  <a-form-item field="invokeTarget">
                     <template #label>
                        <span>
                           调用方法
                           <a-tooltip position="top">
                              <template #content>
                                 <div>
                                    Bean调用示例：scaffTask.scaffParams('scaff')
                                    <br />Class类调用示例：com.manzhushaka.quartz.task.ManzhushakaScaffTask.scaffParams('scaff')
                                    <br />参数说明：支持字符串，布尔类型，长整型，浮点型，整型
                                 </div>
                              </template>
                              <span><question-filled /></span>
                           </a-tooltip>
                        </span>
                     </template>
                     <a-input v-model="form.invokeTarget" placeholder="请输入调用目标字符串" />
                  </a-form-item>
               </a-col>
               <a-col :span="24">
                  <a-form-item label="cron表达式" field="cronExpression">
                     <a-input v-model="form.cronExpression" placeholder="请输入cron执行表达式">
                        <template #append>
                           <a-button type="primary" @click="handleShowCron">
                              <template #icon><Clock /></template>
                              生成表达式
                           </a-button>
                        </template>
                     </a-input>
                  </a-form-item>
               </a-col>
               <a-col :span="24" v-if="form.jobId !== undefined">
                  <a-form-item label="状态">
                     <a-radio-group v-model="form.status">
                        <a-radio
                           v-for="dict in sys_job_status"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="执行策略" field="misfirePolicy">
                     <a-radio-group v-model="form.misfirePolicy">
                        <a-radio value="1">立即执行</a-radio>
                        <a-radio value="2">执行一次</a-radio>
                        <a-radio value="3">放弃执行</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="是否并发" field="concurrent">
                     <a-radio-group v-model="form.concurrent">
                        <a-radio value="0">允许</a-radio>
                        <a-radio value="1">禁止</a-radio>
                     </a-radio-group>
                  </a-form-item>
               </a-col>
            </a-row>
         </a-form>
         <template #footer>
            <div class="dialog-footer">
               <a-button type="primary" @click="submitForm">确 定</a-button>
               <a-button @click="cancel">取 消</a-button>
            </div>
         </template>
      </a-modal>

     <a-modal title="Cron表达式生成器" v-model:visible="openCron" render-to-body unmount-on-close :footer="false">
       <crontab ref="crontabRef" @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
     </a-modal>

      <!-- 任务详细 -->
      <job-detail v-model:visible="openView" :row="form" type="job" />
   </div>
</template>

<script setup name="Job">
import Crontab from '@/components/Crontab'
import JobDetail from './detail'
import { listJob, getJob, delJob, addJob, updateJob, runJob, changeJobStatus } from "@/api/monitor/job"

const router = useRouter()
const { proxy } = getCurrentInstance()
const { sys_job_group, sys_job_status } = useDict("sys_job_group", "sys_job_status")

const jobList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const openView = ref(false)
const openCron = ref(false)
const expression = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    jobName: undefined,
    jobGroup: undefined,
    status: undefined
  },
  rules: {
    jobName: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
    invokeTarget: [{ required: true, message: "调用目标字符串不能为空", trigger: "blur" }],
    cronExpression: [{ required: true, message: "cron执行表达式不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询定时任务列表 */
function getList() {
  loading.value = true
  listJob(queryParams.value).then(response => {
    jobList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    jobId: undefined,
    jobName: undefined,
    jobGroup: undefined,
    invokeTarget: undefined,
    cronExpression: undefined,
    misfirePolicy: '1',
    concurrent: '1',
    status: "0"
  }
  proxy.resetForm("jobRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  single.value = selectedKeys.length != 1
  multiple.value = !selectedKeys.length
}

// 任务状态修改
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + row.jobName + '"任务吗?').then(function () {
    return changeJobStatus(row.jobId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/* 立即执行一次 */
function handleRun(row) {
  proxy.$modal.confirm('确认要立即执行一次"' + row.jobName + '"任务吗?').then(function () {
    return runJob(row.jobId, row.jobGroup)
  }).then(() => {
    proxy.$modal.msgSuccess("执行成功")
  }).catch(() => {})
}

/** 任务详细信息 */
function handleView(row) {
  getJob(row.jobId).then(response => {
    form.value = response.data
    openView.value = true
  })
}

/** cron表达式按钮操作 */
function handleShowCron() {
  expression.value = form.value.cronExpression
  openCron.value = true
}

/** 确定后回传值 */
function crontabFill(value) {
  form.value.cronExpression = value
}

/** 任务日志列表查询 */
function handleJobLog(row) {
  const jobId = row.jobId || 0
  router.push('/monitor/job-log/index/' + jobId)
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加任务"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const jobId = row.jobId || ids.value
  getJob(jobId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改任务"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["jobRef"].validate(errors => {
    if (!errors) {
      if (form.value.jobId != undefined) {
        updateJob(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addJob(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const jobIds = row.jobId || ids.value
  proxy.$modal.confirm('是否确认删除定时任务编号为"' + jobIds + '"的数据项?').then(function () {
    return delJob(jobIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/job/export", {
    ...queryParams.value,
  }, `job_${new Date().getTime()}.xlsx`)
}

getList()
</script>
