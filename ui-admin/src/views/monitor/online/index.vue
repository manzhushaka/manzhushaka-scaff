<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" class="ui-filter-card">
         <a-form-item label="登录地址" field="ipaddr">
            <a-input
               v-model="queryParams.ipaddr"
               placeholder="请输入登录地址"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="用户名称" field="userName">
            <a-input
               v-model="queryParams.userName"
               placeholder="请输入用户名称"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item>
            <a-space>
               <a-button type="primary" @click="handleQuery"><template #icon><icon-search /></template>搜索</a-button>
               <a-button @click="resetQuery"><template #icon><icon-refresh /></template>重置</a-button>
            </a-space>
         </a-form-item>
      </a-form>
      <div class="ui-table-card">
      <a-table
         :loading="loading"
         :data="onlineList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
         :pagination="false"
         row-key="tokenId"
         :scroll="{ x: 1180 }"
      >
         <template #columns>
         <a-table-column title="序号" :width="64" align="center">
            <template #cell="{ rowIndex }">
               <span>{{ (pageNum - 1) * pageSize + rowIndex + 1 }}</span>
            </template>
         </a-table-column>
         <a-table-column title="会话编号" align="center" data-index="tokenId" tooltip />
         <a-table-column title="登录名称" align="center" data-index="userName" tooltip />
         <a-table-column title="所属部门" align="center" data-index="deptName" tooltip />
         <a-table-column title="主机" align="center" data-index="ipaddr" tooltip />
         <a-table-column title="登录地点" align="center" data-index="loginLocation" tooltip />
         <a-table-column title="操作系统" align="center" data-index="os" tooltip />
         <a-table-column title="浏览器" align="center" data-index="browser" tooltip />
         <a-table-column title="登录时间" align="center" data-index="loginTime" :width="180">
            <template #cell="{ record }">
               <span>{{ parseTime(record.loginTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" :width="96" fixed="right">
            <template #cell="{ record }">
               <a-button type="text" status="danger" @click="handleForceLogout(record)" v-hasPermi="['monitor:online:forceLogout']">
                  <template #icon><icon-export /></template>强退
               </a-button>
            </template>
         </a-table-column>
         </template>
      </a-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />
   </div>
</template>

<script setup name="Online">
import { IconExport, IconRefresh, IconSearch } from '@arco-design/web-vue/es/icon'
import { forceLogout, list as initData } from "@/api/monitor/online"

const { proxy } = getCurrentInstance()

const onlineList = ref([])
const loading = ref(true)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryParams = ref({
  ipaddr: undefined,
  userName: undefined
})

/** 查询登录日志列表 */
function getList() {
  loading.value = true
  initData(queryParams.value).then(response => {
    onlineList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  pageNum.value = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 强退按钮操作 */
function handleForceLogout(row) {
  proxy.$modal.confirm('是否确认强退名称为"' + row.userName + '"的用户?').then(function () {
    return forceLogout(row.tokenId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
