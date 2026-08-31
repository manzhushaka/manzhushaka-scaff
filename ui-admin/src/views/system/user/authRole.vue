<template>
   <div class="app-container">
      <section class="ui-panel-card auth-role-panel">
      <h4 class="form-header h4">基本信息</h4>
      <a-form :model="form" :label-col-props="{ flex: '80px' }">
         <a-row>
            <a-col :span="8" :offset="2">
               <a-form-item label="用户昵称" field="nickName">
                  <a-input v-model="form.nickName" disabled />
               </a-form-item>
            </a-col>
            <a-col :span="8" :offset="2">
               <a-form-item label="登录账号" field="userName">
                  <a-input v-model="form.userName" disabled />
               </a-form-item>
            </a-col>
         </a-row>
      </a-form>
      </section>

      <section class="ui-table-card auth-role-table">
      <div class="auth-role-table__title">角色信息</div>
      <a-table
         ref="roleRef"
         v-model:selected-keys="roleIds"
         :loading="loading"
         :row-key="getRowKey"
         :row-selection="{ type: 'checkbox', showCheckedAll: true, onlyCurrent: false }"
         :data="roles.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
         @row-click="clickRow"
       :pagination="false">
         <a-table-column title="序号" width="55" align="center">
            <template #cell="{ record, rowIndex }">
               <span>{{ (pageNum - 1) * pageSize + rowIndex + 1 }}</span>
            </template>
         </a-table-column>
         <a-table-column title="角色编号" align="center" data-index="roleId" />
         <a-table-column title="角色名称" align="center" data-index="roleName" />
         <a-table-column title="权限字符" align="center" data-index="roleKey" />
         <a-table-column title="创建时间" align="center" data-index="createTime" width="180">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.createTime) }}</span>
            </template>
         </a-table-column>
      </a-table>

      <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />
      </section>

      <div class="auth-role-footer">
         <a-button type="primary" @click="submitForm()">提交</a-button>
         <a-button @click="close()">返回</a-button>
      </div>
   </div>
</template>

<script setup name="AuthRole">
import { getAuthRole, updateAuthRole } from "@/api/system/user"

const route = useRoute()
const { proxy } = getCurrentInstance()

const loading = ref(true)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const roleIds = ref([])
const roles = ref([])
const form = ref({
  nickName: undefined,
  userName: undefined,
  userId: undefined
})

/** 单击选中行数据 */
function clickRow(row) {
  if (checkSelectable(row)) {
    proxy.$refs["roleRef"].select(row.roleId, !roleIds.value.includes(row.roleId))
  }
}

/** 保存选中的数据编号 */
function getRowKey(row) {
  return row.roleId
}

// 检查角色状态
function checkSelectable(row) {
  return row.status === "0" ? true : false
}

/** 关闭按钮 */
function close() {
  const obj = { path: "/userAuth/user" }
  proxy.$tab.closeOpenPage(obj)
}

/** 提交按钮 */
function submitForm() {
  const userId = form.value.userId
  const rIds = roleIds.value.join(",")
  updateAuthRole({ userId: userId, roleIds: rIds }).then(() => {
    proxy.$modal.msgSuccess("授权成功")
    close()
  })
}

(() => {
  const userId = route.params && route.params.userId
  if (userId) {
    loading.value = true
    getAuthRole(userId).then(response => {
      form.value = response.user
      roles.value = response.roles.map(row => ({ ...row, disabled: row.status !== '0' }))
      total.value = roles.value.length
      roleIds.value = roles.value.filter(row => row.flag).map(row => row.roleId)
      loading.value = false
    })
  }
})()
</script>

<style lang="scss" scoped>
.auth-role-panel {
  padding: 16px;
  margin-bottom: 12px;
}

.auth-role-table {
  overflow: hidden;
}

.auth-role-table__title {
  min-height: 44px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: var(--ui-text-primary);
  font-size: 14px;
  font-weight: 700;
  border-bottom: 1px solid var(--ui-border);
  background: var(--ui-bg-panel-muted);
}

.auth-role-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;
}
</style>
