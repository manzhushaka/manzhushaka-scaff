<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
         <a-form-item label="登录地址" field="ipaddr">
            <a-input
               v-model="queryParams.ipaddr"
               placeholder="请输入登录地址"
               allow-clear
               style="width: 240px;"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="用户名称" field="userName">
            <a-input
               v-model="queryParams.userName"
               placeholder="请输入用户名称"
               allow-clear
               style="width: 240px;"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="状态" field="status">
            <a-select
               v-model="queryParams.status"
               placeholder="登录状态"
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
         <a-form-item label="登录时间" style="width: 308px">
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
               v-hasPermi="['monitor:logininfor:remove']"
             type="outline"><template #icon><Delete /></template>删除</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               @click="handleClean"
               v-hasPermi="['monitor:logininfor:remove']"
             type="outline"><template #icon><Delete /></template>清空</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button



               :disabled="single"
               @click="handleUnlock"
               v-hasPermi="['monitor:logininfor:unlock']"
             type="outline"><template #icon><Unlock /></template>解锁</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleExport"
               v-hasPermi="['monitor:logininfor:export']"
             type="outline"><template #icon><Download /></template>导出</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table :loading="loading" :data="logininforList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.infoId" :pagination="false" @selection-change="handleSelectionChange" @sorter-change="handleSortChange">
         <a-table-column title="访问编号" align="center" data-index="infoId" />
         <a-table-column title="用户名称" align="center" data-index="userName" ellipsis :sortable="{ sortDirections: ['descend', 'ascend'], sorter: true }" tooltip />
         <a-table-column title="地址" align="center" data-index="ipaddr" ellipsis tooltip />
         <a-table-column title="登录地点" align="center" data-index="loginLocation" ellipsis tooltip />
         <a-table-column title="操作系统" align="center" data-index="os" ellipsis tooltip />
         <a-table-column title="浏览器" align="center" data-index="browser" ellipsis tooltip />
         <a-table-column title="登录状态" align="center" data-index="status">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_common_status" :value="record.status" />
            </template>
         </a-table-column>
         <a-table-column title="描述" align="center" data-index="msg" ellipsis tooltip />
         <a-table-column title="访问时间" align="center" data-index="loginTime" :sortable="{ sortDirections: ['descend', 'ascend'], sorter: true, defaultSortOrder: 'descend' }" width="180">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.loginTime) }}</span>
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
   </div>
</template>

<script setup name="Logininfor">
import { list, delLogininfor, cleanLogininfor, unlockLogininfor } from "@/api/monitor/logininfor"

const { proxy } = getCurrentInstance()
const { sys_common_status } = useDict("sys_common_status")

const logininforList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const selectName = ref("")
const total = ref(0)
const dateRange = ref([])
const defaultSort = ref({ prop: "loginTime", order: "descending" })

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  ipaddr: undefined,
  userName: undefined,
  status: undefined,
  orderByColumn: undefined,
  isAsc: undefined
})

/** 查询登录日志列表 */
function getList() {
  loading.value = true
  list(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    logininforList.value = response.rows
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
  single.value = selectedKeys.length != 1
  selectName.value = logininforList.value
    .filter(item => selectedKeys.includes(item.infoId))
    .map(item => item.userName)
}

/** 排序触发事件 */
function handleSortChange(dataIndex, direction) {
  queryParams.value.orderByColumn = dataIndex
  queryParams.value.isAsc = direction === 'ascend' ? 'ascending' : 'descending'
  getList()
}

/** 删除按钮操作 */
function handleDelete(row) {
  const infoIds = row.infoId || ids.value
  proxy.$modal.confirm('是否确认删除访问编号为"' + infoIds + '"的数据项?').then(function () {
    return delLogininfor(infoIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 清空按钮操作 */
function handleClean() {
  proxy.$modal.confirm("是否确认清空所有登录日志数据项?").then(function () {
    return cleanLogininfor()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("清空成功")
  }).catch(() => {})
}

/** 解锁按钮操作 */
function handleUnlock() {
  const username = selectName.value
  proxy.$modal.confirm('是否确认解锁用户"' + username + '"数据项?').then(function () {
    return unlockLogininfor(username)
  }).then(() => {
    proxy.$modal.msgSuccess("用户" + username + "解锁成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/logininfor/export", {
    ...queryParams.value,
  }, `logininfor_${new Date().getTime()}.xlsx`)
}

getList()
</script>
