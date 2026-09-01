<template>
  <div class="app-container tree-sidebar-manage-wrap">
    <tree-panel title="组织机构" :tree-data="deptOptions" search-placeholder="请输入部门名称" storage-key="dept-sidebar-width" :defaultExpandAll="true" @node-click="handleNodeClick" @refresh="getDeptTree" ref="deptTreeRef" />
    <div class="tree-sidebar-content">
      <div class="content-inner ui-list-page">
        <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
          <a-form-item label="用户名称" field="userName">
            <a-input v-model="queryParams.userName" placeholder="请输入用户名称" allow-clear style="width: 240px" @keyup.enter="handleQuery" />
          </a-form-item>
          <a-form-item label="手机号码" field="phonenumber">
            <a-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" allow-clear style="width: 240px" @keyup.enter="handleQuery" />
          </a-form-item>
          <a-form-item label="状态" field="status">
            <a-select v-model="queryParams.status" placeholder="用户状态" allow-clear style="width: 240px">
              <a-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
            </a-select>
          </a-form-item>
          <a-form-item label="创建时间" style="width: 308px">
            <a-range-picker v-model="dateRange" value-format="YYYY-MM-DD" separator="-" :placeholder="['开始日期', '结束日期']"></a-range-picker>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery"><template #icon><Search /></template>搜索</a-button>
            <a-button @click="resetQuery"><template #icon><Refresh /></template>重置</a-button>
          </a-form-item>
        </a-form>

        <a-row class="ui-action-bar">
          <a-col :span="1.5">
            <a-button @click="handleAdd" v-hasPermi="['system:user:add']" type="outline"><template #icon><Plus /></template>新增</a-button>
          </a-col>
          <a-col :span="1.5">
            <a-button status="success" :disabled="single" @click="handleUpdate" v-hasPermi="['system:user:edit']" type="outline"><template #icon><Edit /></template>修改</a-button>
          </a-col>
          <a-col :span="1.5">
            <a-button status="danger" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:user:remove']" type="outline"><template #icon><Delete /></template>删除</a-button>
          </a-col>
          <a-col :span="1.5">
            <a-button @click="handleImport" v-hasPermi="['system:user:import']" type="outline"><template #icon><Upload /></template>导入</a-button>
          </a-col>
          <a-col :span="1.5">
            <a-button status="warning" @click="handleExport" v-hasPermi="['system:user:export']" type="outline"><template #icon><Download /></template>导出</a-button>
          </a-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="system-user-columns"></right-toolbar>
        </a-row>

        <div class="ui-table-card">
        <a-table :loading="loading" :data="userList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.userId" :pagination="false" @selection-change="handleSelectionChange">
          <template #columns>
          <a-table-column title="用户编号" align="center" key="userId" data-index="userId" v-if="columns.userId.visible" />
          <a-table-column title="用户名称" align="center" key="userName" v-if="columns.userName.visible" ellipsis tooltip>
            <template #cell="{ record, rowIndex }">
              <a class="link-type" style="cursor:pointer" @click="handleViewData(record)">{{ record.userName }}</a>
            </template>
         </a-table-column>
          <a-table-column title="用户昵称" align="center" key="nickName" data-index="nickName" v-if="columns.nickName.visible" ellipsis tooltip />
          <a-table-column title="部门" align="center" key="deptName" data-index="dept.deptName" v-if="columns.deptName.visible" ellipsis tooltip />
          <a-table-column title="手机号码" align="center" key="phonenumber" data-index="phonenumber" v-if="columns.phonenumber.visible" width="120" />
          <a-table-column title="状态" align="center" key="status" v-if="columns.status.visible">
            <template #cell="{ record, rowIndex }">
              <a-switch
                v-model="record.status"
                checked-value="0"
                unchecked-value="1"
                @change="handleStatusChange(record)"
              ></a-switch>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" align="center" data-index="createTime" v-if="columns.createTime.visible" width="160">
            <template #cell="{ record, rowIndex }">
              <span>{{ parseTime(record.createTime) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="操作" align="center" width="150" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
              <a-tooltip content="修改" position="top" v-if="record.userId !== 1">
                <a-button @click="handleUpdate(record)" v-hasPermi="['system:user:edit']"><template #icon><Edit /></template></a-button>
              </a-tooltip>
              <a-tooltip content="删除" position="top" v-if="record.userId !== 1">
                <a-button @click="handleDelete(record)" v-hasPermi="['system:user:remove']"><template #icon><Delete /></template></a-button>
              </a-tooltip>
              <a-tooltip content="重置密码" position="top" v-if="record.userId !== 1">
                <a-button @click="handleResetPwd(record)" v-hasPermi="['system:user:resetPwd']"><template #icon><Key /></template></a-button>
              </a-tooltip>
              <a-tooltip content="分配角色" position="top" v-if="record.userId !== 1">
                <a-button @click="handleAuthRole(record)" v-hasPermi="['system:user:edit']"><template #icon><CircleCheck /></template></a-button>
              </a-tooltip>
            </template>
          </a-table-column>
          </template>
        </a-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
        </div>
      </div>
    </div>

    <!-- 添加或修改用户配置对话框 -->
    <a-modal :title="title" v-model:visible="open" width="600px" render-to-body>
      <a-form :model="form" :rules="rules" ref="userRef" :label-col-props="{ flex: '80px' }">
        <a-row>
          <a-col :span="12">
            <a-form-item label="用户昵称" field="nickName">
              <a-input v-model="form.nickName" placeholder="请输入用户昵称" maxlength="30" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="归属部门" field="deptId">
              <a-tree-select v-model="form.deptId" :data="enabledDeptOptions" :field-names="{ key: 'id', title: 'label', children: 'children' }" placeholder="请选择归属部门" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="手机号码" field="phonenumber">
              <a-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="邮箱" field="email">
              <a-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item v-if="form.userId == undefined" label="用户名称" field="userName">
              <a-input v-model="form.userName" placeholder="请输入用户名称" maxlength="30" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item v-if="form.userId == undefined" label="用户密码" field="password" :rules="pwdValidator">
              <a-input-password v-model="form.password" placeholder="请输入用户密码" maxlength="20"  />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form-item label="用户性别">
              <a-select v-model="form.sex" placeholder="请选择">
                <a-option v-for="dict in sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value"></a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-radio-group v-model="form.status">
                <a-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24">
            <a-form-item label="角色">
              <a-select v-model="form.roleIds" multiple placeholder="请选择">
                <a-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" :disabled="item.status == 1"></a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24">
            <a-form-item label="备注">
              <a-textarea v-model="form.remark" placeholder="请输入内容"></a-textarea>
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

    <!-- 用户详情抽屉 -->
    <user-view-drawer ref="userViewRef" />
    <!-- 用户导入对话框 -->
    <excel-import-dialog ref="importUserRef" title="用户导入" action="/system/user/importData" template-action="/system/user/importTemplate" template-file-name="user_template" update-support-label="是否更新已经存在的用户数据" @success="getList" />
  </div>
</template>

<script setup name="User">
import TreePanel from "@/components/TreePanel"
import ExcelImportDialog from "@/components/ExcelImportDialog"
import UserViewDrawer from "./view"
import { usePasswordRule } from "@/utils/passwordRule"
import { changeUserStatus, listUser, resetUserPwd, delUser, getUser, updateUser, addUser, deptTreeSelect } from "@/api/system/user"

const router = useRouter()
const { proxy } = getCurrentInstance()
const { pwdValidator, pwdPromptValidator } = usePasswordRule()
const { sys_normal_disable, sys_user_sex } = useDict("sys_normal_disable", "sys_user_sex")

const userList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const deptOptions = ref(undefined)
const enabledDeptOptions = ref(undefined)
const initPassword = ref(undefined)
const roleOptions = ref([])
// 列显隐信息
const columns = ref({
  userId: { label: '用户编号', visible: true },
  userName: { label: '用户名称', visible: true },
  nickName: { label: '用户昵称', visible: true },
  deptName: { label: '部门', visible: true },
  phonenumber: { label: '手机号码', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined,
    deptId: undefined
  },
  rules: {
    userName: [{ required: true, message: "用户名称不能为空", trigger: "blur" }, { min: 2, max: 20, message: "用户名称长度必须介于 2 和 20 之间", trigger: "blur" }],
    nickName: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
    email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
    phonenumber: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询用户列表 */
function getList() {
  loading.value = true
  listUser(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    loading.value = false
    userList.value = res.rows
    total.value = res.total
  })
}

/** 查询部门下拉树结构 */
function getDeptTree() {
  deptTreeSelect().then(response => {
    deptOptions.value = response.data
    enabledDeptOptions.value = filterDisabledDept(JSON.parse(JSON.stringify(response.data)))
  })
}

/** 过滤禁用的部门 */
function filterDisabledDept(deptList) {
  return deptList.filter(dept => {
    if (dept.disabled) {
      return false
    }
    if (dept.children && dept.children.length) {
      dept.children = filterDisabledDept(dept.children)
    }
    return true
  })
}

/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.deptId = data.id
  handleQuery()
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
  queryParams.value.deptId = undefined
  proxy.$refs.deptTreeRef.setCurrentKey(null)
  handleQuery()
}

/** 删除按钮操作 */
function handleDelete(row) {
  const userIds = row.userId || ids.value
  proxy.$modal.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？').then(function () {
    return delUser(userIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/user/export", {
    ...queryParams.value,
  },`user_${new Date().getTime()}.xlsx`)
}

/** 用户状态修改  */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗?').then(function () {
    return changeUserStatus(row.userId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/** 更多操作 */
function handleCommand(command, row) {
  switch (command) {
    case "handleResetPwd":
      handleResetPwd(row)
      break
    case "handleAuthRole":
      handleAuthRole(row)
      break
    default:
      break
  }
}

/** 跳转角色分配 */
function handleAuthRole(row) {
  const userId = row.userId
  router.push("/system/user-auth/role/" + userId)
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  proxy.$prompt(`请输入「${row.userName}」的新密码`, "重置密码", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputValidator: pwdPromptValidator
  }).then(({ value }) => {
    resetUserPwd(row.userId, value).then(() => {
      proxy.$modal.msgSuccess("修改成功，新密码是：" + value)
    })
  }).catch(() => {})
}

/** 选择条数  */
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  single.value = selectedKeys.length != 1
  multiple.value = !selectedKeys.length
}

/** 详情按钮操作 */
function handleViewData(row) {
  proxy.$refs["userViewRef"].open(row.userId)
}

/** 导入按钮操作 */
function handleImport() {
  proxy.$refs["importUserRef"].open()
}

/** 重置操作表单 */
function reset() {
  form.value = {
    userId: undefined,
    deptId: undefined,
    userName: undefined,
    nickName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: undefined,
    status: "0",
    remark: undefined,
    roleIds: []
  }
  proxy.resetForm("userRef")
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  getUser().then(response => {
    roleOptions.value = response.roles
    open.value = true
    title.value = "添加用户"
    form.value.password = initPassword.value
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const userId = row.userId || ids.value
  getUser(userId).then(response => {
    form.value = response.data
    roleOptions.value = response.roles
    form.value.roleIds = response.roleIds
    open.value = true
    title.value = "修改用户"
    form.value.password = ""
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["userRef"].validate(errors => {
    if (!errors) {
      if (form.value.userId != undefined) {
        updateUser(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addUser(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

onMounted(() => {
  getDeptTree()
  getList()
  proxy.getConfigKey("sys.user.initPassword").then(response => {
    initPassword.value = response.msg
  })
})
</script>
