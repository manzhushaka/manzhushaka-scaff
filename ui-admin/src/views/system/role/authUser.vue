
<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" v-show="showSearch" layout="inline" class="ui-filter-card">
         <a-form-item label="用户名称" field="userName">
            <a-input
               v-model="queryParams.userName"
               placeholder="请输入用户名称"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="手机号码" field="phonenumber">
            <a-input
               v-model="queryParams.phonenumber"
               placeholder="请输入手机号码"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item>
            <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
            <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
         </a-form-item>
      </a-form>

      <a-row class="ui-action-bar">
         <a-col :span="1.5">
            <a-button



               @click="openSelectUser"
               v-hasPermi="['system:role:add']"
             type="outline"><template #icon><Plus /></template>添加用户</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               :disabled="multiple"
               @click="cancelAuthUserAll"
               v-hasPermi="['system:role:remove']"
             type="outline"><template #icon><CircleClose /></template>批量取消授权</a-button>
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
      <a-table :loading="loading" :data="userList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.userId" :pagination="false" @selection-change="handleSelectionChange">
         <a-table-column title="用户名称" data-index="userName" ellipsis tooltip />
         <a-table-column title="用户昵称" data-index="nickName" ellipsis tooltip />
         <a-table-column title="邮箱" data-index="email" ellipsis tooltip />
         <a-table-column title="手机" data-index="phonenumber" ellipsis tooltip />
         <a-table-column title="状态" align="center" data-index="status">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_normal_disable" :value="record.status" />
            </template>
         </a-table-column>
         <a-table-column title="创建时间" align="center" data-index="createTime" width="180">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.createTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button @click="cancelAuthUser(record)" v-hasPermi="['system:role:remove']"><template #icon><CircleClose /></template>取消授权</a-button>
            </template>
         </a-table-column>
      </a-table>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />
      </div>
      <select-user ref="selectRef" :roleId="queryParams.roleId" @ok="handleQuery" />
   </div>
</template>

<script setup name="AuthUser">
import selectUser from "./selectUser"
import { allocatedUserList, authUserCancel, authUserCancelAll } from "@/api/system/role"

const route = useRoute()
const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const userIds = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: route.params.roleId,
  userName: undefined,
  phonenumber: undefined,
})

/** 查询授权用户列表 */
function getList() {
  loading.value = true
  allocatedUserList(queryParams).then(response => {
    userList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 返回按钮 */
function handleClose() {
  const obj = { path: "/userAuth/role" }
  proxy.$tab.closeOpenPage(obj)
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selectedKeys) {
  userIds.value = selectedKeys
  multiple.value = !selectedKeys.length
}

/** 打开授权用户表弹窗 */
function openSelectUser() {
  proxy.$refs["selectRef"].show()
}

/** 取消授权按钮操作 */
function cancelAuthUser(row) {
  proxy.$modal.confirm('确认要取消该用户"' + row.userName + '"角色吗？').then(function () {
    return authUserCancel({ userId: row.userId, roleId: queryParams.roleId })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("取消授权成功")
  }).catch(() => {})
}

/** 批量取消授权按钮操作 */
function cancelAuthUserAll() {
  const roleId = queryParams.roleId
  const uIds = userIds.value.join(",")
  proxy.$modal.confirm("是否取消选中用户授权数据项?").then(function () {
    return authUserCancelAll({ roleId: roleId, userIds: uIds })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("取消授权成功")
  }).catch(() => {})
}

getList()
</script>
