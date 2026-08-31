<template>
   <!-- 授权用户 -->
   <a-modal title="选择用户" v-model:visible="visible" width="800px" top="5vh" render-to-body>
      <a-form :model="queryParams" ref="queryRef" layout="inline" class="ui-filter-card">
         <a-form-item label="用户名称" field="userName">
            <a-input
               v-model="queryParams.userName"
               placeholder="请输入用户名称"
               allow-clear
               style="width: 180px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="手机号码" field="phonenumber">
            <a-input
               v-model="queryParams.phonenumber"
               placeholder="请输入手机号码"
               allow-clear
               style="width: 180px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item>
            <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
            <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
         </a-form-item>
      </a-form>
      <div class="ui-table-card" style="margin-bottom: 12px;">
         <a-table
            ref="refTable"
            v-model:selected-keys="userIds"
            :data="userList"
            :row-key="record => record.userId" :pagination="false"
            :row-selection="{ type: 'checkbox', showCheckedAll: true }"
            :scroll="{ y: 260 }"
            @row-click="clickRow"
         >
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
         </a-table>
         <pagination
            v-show="total > 0"
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
         />
     </div>
      <template #footer>
         <div class="dialog-footer">
            <a-button type="primary" @click="handleSelectUser">确 定</a-button>
            <a-button @click="visible = false">取 消</a-button>
         </div>
      </template>
   </a-modal>
</template>

<script setup name="SelectUser">
import { authUserSelectAll, unallocatedUserList } from "@/api/system/role"

const props = defineProps({
  roleId: {
    type: [Number, String]
  }
})

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const userList = ref([])
const visible = ref(false)
const total = ref(0)
const userIds = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: undefined,
  userName: undefined,
  phonenumber: undefined
})

// 显示弹框
function show() {
  queryParams.roleId = props.roleId
  getList()
  visible.value = true
}

/**选择行 */
function clickRow(row) {
  proxy.$refs["refTable"].select(row.userId, !userIds.value.includes(row.userId))
}

// 查询表数据
function getList() {
  unallocatedUserList(queryParams).then(res => {
    userList.value = res.rows
    total.value = res.total
  })
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

const emit = defineEmits(["ok"])
/** 选择授权用户操作 */
function handleSelectUser() {
  const roleId = queryParams.roleId
  const uIds = userIds.value.join(",")
  if (uIds == "") {
    proxy.$modal.msgError("请选择要分配的用户")
    return
  }
  authUserSelectAll({ roleId: roleId, userIds: uIds }).then(res => {
    proxy.$modal.msgSuccess(res.msg)
    visible.value = false
    emit("ok")
  })
}

defineExpose({
  show,
})
</script>
