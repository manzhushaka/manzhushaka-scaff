<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
         <a-form-item label="操作地址" field="operIp">
            <a-input
               v-model="queryParams.operIp"
               placeholder="请输入操作地址"
               allow-clear
               style="width: 240px;"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="系统模块" field="title">
            <a-input
               v-model="queryParams.title"
               placeholder="请输入系统模块"
               allow-clear
               style="width: 240px;"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="操作人员" field="operName">
            <a-input
               v-model="queryParams.operName"
               placeholder="请输入操作人员"
               allow-clear
               style="width: 240px;"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="类型" field="businessType">
            <a-select
               v-model="queryParams.businessType"
               placeholder="操作类型"
               allow-clear
               style="width: 240px"
            >
               <a-option
                  v-for="dict in sys_oper_type"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item label="状态" field="status">
            <a-select
               v-model="queryParams.status"
               placeholder="操作状态"
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
         <a-form-item label="操作时间" style="width: 308px">
            <a-range-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD HH:mm:ss"

               separator="-"
               :placeholder="['开始日期', '结束日期']"
               :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
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
               v-hasPermi="['monitor:operlog:remove']"
             type="outline"><template #icon><Delete /></template>删除</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               @click="handleClean"
               v-hasPermi="['monitor:operlog:remove']"
             type="outline"><template #icon><Delete /></template>清空</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleExport"
               v-hasPermi="['monitor:operlog:export']"
             type="outline"><template #icon><Download /></template>导出</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table :loading="loading" :data="operlogList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.operId" :pagination="false" @selection-change="handleSelectionChange" @sorter-change="handleSortChange">
         <a-table-column title="日志编号" align="center" data-index="operId" />
         <a-table-column title="系统模块" align="center" data-index="title" ellipsis tooltip />
         <a-table-column title="操作类型" align="center" data-index="businessType">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_oper_type" :value="record.businessType" />
            </template>
         </a-table-column>
         <a-table-column title="操作人员" align="center" width="110" data-index="operName" ellipsis :sortable="{ sortDirections: ['descend', 'ascend'], sorter: true }" tooltip />
         <a-table-column title="操作地址" align="center" data-index="operIp" width="130" ellipsis tooltip />
         <a-table-column title="操作状态" align="center" data-index="status">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_common_status" :value="record.status" />
            </template>
         </a-table-column>
         <a-table-column title="操作日期" align="center" data-index="operTime" width="180" :sortable="{ sortDirections: ['descend', 'ascend'], sorter: true, defaultSortOrder: 'descend' }">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.operTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="消耗时间" align="center" data-index="costTime" width="110" ellipsis :sortable="{ sortDirections: ['descend', 'ascend'], sorter: true }" tooltip>
            <template #cell="{ record, rowIndex }">
               <span>{{ record.costTime }}毫秒</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button @click="handleDetail(record, scope.index)" v-hasPermi="['monitor:operlog:query']"><template #icon><View /></template>详细</a-button>
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

      <operlog-detail v-model:visible="detailVisible" :row="detailRow" />
   </div>
</template>

<script setup name="Operlog">
import OperlogDetail from './detail'
import { list, delOperlog, cleanOperlog } from "@/api/monitor/operlog"

const { proxy } = getCurrentInstance()
const { sys_oper_type, sys_common_status } = useDict("sys_oper_type", "sys_common_status")

const operlogList = ref([])
const detailVisible = ref(false)
const loading = ref(true)
const detailRow = ref({})
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const defaultSort = ref({ prop: "operTime", order: "descending" })

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    operIp: undefined,
    title: undefined,
    operName: undefined,
    businessType: undefined,
    status: undefined
  }
})

const { queryParams, form } = toRefs(data)

/** 查询登录日志 */
function getList() {
  loading.value = true
  list(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    operlogList.value = response.rows
    total.value = response.total
    loading.value = false
  })
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
  queryParams.value.pageNum = 1
  queryParams.value.orderByColumn = defaultSort.value.prop
  queryParams.value.isAsc = defaultSort.value.order
  getList()
}

/** 多选框选中数据 */
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  multiple.value = !selectedKeys.length
}

/** 排序触发事件 */
function handleSortChange(dataIndex, direction) {
  queryParams.value.orderByColumn = dataIndex
  queryParams.value.isAsc = direction === 'ascend' ? 'ascending' : 'descending'
  getList()
}

/** 详细按钮操作 */
function handleDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

/** 删除按钮操作 */
function handleDelete(row) {
  const operIds = row.operId || ids.value
  proxy.$modal.confirm('是否确认删除日志编号为"' + operIds + '"的数据项?').then(function () {
    return delOperlog(operIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有操作日志数据项?").then(function () {
    return cleanOperlog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/operlog/export",{
    ...queryParams.value,
  }, `config_${new Date().getTime()}.xlsx`)
}

getList()
</script>
