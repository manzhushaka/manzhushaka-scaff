<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" v-show="showSearch" layout="inline" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
         <a-form-item label="角色名称" field="roleName">
            <a-input
               v-model="queryParams.roleName"
               placeholder="请输入角色名称"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="权限字符" field="roleKey">
            <a-input
               v-model="queryParams.roleKey"
               placeholder="请输入权限字符"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="状态" field="status">
            <a-select
               v-model="queryParams.status"
               placeholder="角色状态"
               allow-clear
               style="width: 240px"
            >
               <a-option
                  v-for="dict in sys_normal_disable"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item label="创建时间" style="width: 308px">
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



               @click="handleAdd"
               v-hasPermi="['system:role:add']"
             type="outline"><template #icon><Plus /></template>新增</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="success"


               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['system:role:edit']"
             type="outline"><template #icon><Edit /></template>修改</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['system:role:remove']"
             type="outline"><template #icon><Delete /></template>删除</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleExport"
               v-hasPermi="['system:role:export']"
             type="outline"><template #icon><Download /></template>导出</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <!-- 表格数据 -->
      <div class="ui-table-card">
      <a-table :loading="loading" :data="roleList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.roleId" :pagination="false" @selection-change="handleSelectionChange">
         <a-table-column title="角色编号" data-index="roleId" width="120" />
         <a-table-column title="角色名称" data-index="roleName" ellipsis width="150" tooltip />
         <a-table-column title="权限字符" data-index="roleKey" ellipsis width="150" tooltip />
         <a-table-column title="显示顺序" data-index="roleSort" width="100" />
         <a-table-column title="状态" align="center" width="100">
            <template #cell="{ record, rowIndex }">
               <a-switch
                  v-model="record.status"
                  checked-value="0"
                  unchecked-value="1"
                  @change="handleStatusChange(record)"
               ></a-switch>
            </template>
         </a-table-column>
         <a-table-column title="创建时间" align="center" data-index="createTime">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.createTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
              <a-tooltip content="修改" position="top" v-if="record.roleId !== 1">
                <a-button @click="handleUpdate(record)" v-hasPermi="['system:role:edit']"><template #icon><Edit /></template></a-button>
              </a-tooltip>
              <a-tooltip content="删除" position="top" v-if="record.roleId !== 1">
                <a-button @click="handleDelete(record)" v-hasPermi="['system:role:remove']"><template #icon><Delete /></template></a-button>
              </a-tooltip>
              <a-tooltip content="数据权限" position="top" v-if="record.roleId !== 1">
                <a-button @click="handleDataScope(record)" v-hasPermi="['system:role:edit']"><template #icon><CircleCheck /></template></a-button>
              </a-tooltip>
              <a-tooltip content="分配用户" position="top" v-if="record.roleId !== 1">
                <a-button @click="handleAuthUser(record)" v-hasPermi="['system:role:edit']"><template #icon><User /></template></a-button>
              </a-tooltip>
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

      <!-- 添加或修改角色配置对话框 -->
      <a-modal :title="title" v-model:visible="open" width="500px" render-to-body>
         <a-form ref="roleRef" :model="form" :rules="rules" :label-col-props="{ flex: '100px' }">
            <a-form-item label="角色名称" field="roleName">
               <a-input v-model="form.roleName" placeholder="请输入角色名称" />
            </a-form-item>
            <a-form-item field="roleKey">
               <template #label>
                  <span>
                     <a-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasRole('admin')`)" position="top">
                        <span><question-filled /></span>
                     </a-tooltip>
                     权限字符
                  </span>
               </template>
               <a-input v-model="form.roleKey" placeholder="请输入权限字符" />
            </a-form-item>
            <a-form-item label="角色顺序" field="roleSort">
               <a-input-number v-model="form.roleSort" controls-position="right" :min="0" />
            </a-form-item>
            <a-form-item label="状态">
               <a-radio-group v-model="form.status">
                  <a-radio
                     v-for="dict in sys_normal_disable"
                     :key="dict.value"
                     :value="dict.value"
                  >{{ dict.label }}</a-radio>
               </a-radio-group>
            </a-form-item>
            <a-form-item label="菜单权限">
               <a-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠</a-checkbox>
               <a-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选</a-checkbox>
               <a-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">父子联动</a-checkbox>
               <a-tree
                  class="tree-border"
                  v-model:checked-keys="menuCheckedKeys"
                  v-model:expanded-keys="menuExpandedKeys"
                  :data="menuOptions"
                  checkable
                  ref="menuRef"
                  :check-strictly="!form.menuCheckStrictly"
                  :field-names="treeFieldNames"
               ></a-tree>
            </a-form-item>
            <a-form-item label="备注">
               <a-textarea v-model="form.remark" placeholder="请输入内容"></a-textarea>
            </a-form-item>
         </a-form>
         <template #footer>
            <div class="dialog-footer">
               <a-button type="primary" @click="submitForm">确 定</a-button>
               <a-button @click="cancel">取 消</a-button>
            </div>
         </template>
      </a-modal>

      <!-- 分配角色数据权限抽屉 -->
      <a-drawer :title="title" v-model:visible="openDataScope" size="520px" render-to-body>
         <template #default>
            <a-form :model="form" :label-col-props="{ flex: '80px' }">
               <a-form-item label="角色名称">
                  <a-input v-model="form.roleName" :disabled="true" />
               </a-form-item>
               <a-form-item label="权限字符">
                  <a-input v-model="form.roleKey" :disabled="true" />
               </a-form-item>
               <a-form-item label="权限范围">
                  <a-select v-model="form.dataScope" @change="dataScopeSelectChange">
                     <a-option
                        v-for="item in dataScopeOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                     ></a-option>
                  </a-select>
               </a-form-item>
               <a-form-item label="数据权限" v-show="form.dataScope == 2">
                  <a-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">展开/折叠</a-checkbox>
                  <a-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">全选/全不选</a-checkbox>
                  <a-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">父子联动</a-checkbox>
                  <a-tree
                     class="tree-border"
                     v-model:checked-keys="deptCheckedKeys"
                     v-model:expanded-keys="deptExpandedKeys"
                     :data="deptOptions"
                     checkable
                     default-expand-all
                     ref="deptRef"
                     :check-strictly="!form.deptCheckStrictly"
                     :field-names="treeFieldNames"
                  ></a-tree>
               </a-form-item>
            </a-form>
         </template>
         <template #footer>
            <div class="dialog-footer">
               <a-button type="primary" @click="submitDataScope">确 定</a-button>
               <a-button @click="cancelDataScope">取 消</a-button>
            </div>
         </template>
      </a-drawer>
   </div>
</template>

<script setup name="Role">
import { addRole, changeRoleStatus, dataScope, delRole, getRole, listRole, updateRole, deptTreeSelect } from "@/api/system/role"
import { roleMenuTreeselect, treeselect as menuTreeselect } from "@/api/system/menu"

const router = useRouter()
const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const roleList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const menuOptions = ref([])
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const deptExpand = ref(true)
const deptNodeAll = ref(false)
const deptOptions = ref([])
const openDataScope = ref(false)
const menuRef = ref(null)
const deptRef = ref(null)
const menuCheckedKeys = ref([])
const menuExpandedKeys = ref([])
const deptCheckedKeys = ref([])
const deptExpandedKeys = ref([])
const treeFieldNames = { key: 'id', title: 'label', children: 'children' }

/** 数据范围选项*/
const dataScopeOptions = ref([
  { value: "1", label: "全部数据权限" },
  { value: "2", label: "自定数据权限" },
  { value: "3", label: "本部门数据权限" },
  { value: "4", label: "本部门及以下数据权限" },
  { value: "5", label: "仅本人数据权限" }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    roleName: undefined,
    roleKey: undefined,
    status: undefined
  },
  rules: {
    roleName: [{ required: true, message: "角色名称不能为空", trigger: "blur" }],
    roleKey: [{ required: true, message: "权限字符不能为空", trigger: "blur" }],
    roleSort: [{ required: true, message: "角色顺序不能为空", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

/** 查询角色列表 */
function getList() {
  loading.value = true
  listRole(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    roleList.value = response.rows
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
  handleQuery()
}

/** 删除按钮操作 */
function handleDelete(row) {
  const roleIds = row.roleId || ids.value
  proxy.$modal.confirm('是否确认删除角色编号为"' + roleIds + '"的数据项?').then(function () {
    return delRole(roleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/role/export", {
    ...queryParams.value,
  }, `role_${new Date().getTime()}.xlsx`)
}

/** 多选框选中数据 */
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  single.value = selectedKeys.length != 1
  multiple.value = !selectedKeys.length
}

/** 角色状态修改 */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + row.roleName + '"角色吗?').then(function () {
    return changeRoleStatus(row.roleId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/** 更多操作 */
function handleCommand(command, row) {
  switch (command) {
    case "handleDataScope":
      handleDataScope(row)
      break
    case "handleAuthUser":
      handleAuthUser(row)
      break
    default:
      break
  }
}

/** 分配用户 */
function handleAuthUser(row) {
  router.push("/system/role-auth/user/" + row.roleId)
}

/** 查询菜单树结构 */
function getMenuTreeselect() {
  menuTreeselect().then(response => {
    menuOptions.value = response.data
  })
}

/** 所有部门节点数据 */
function getDeptAllCheckedKeys() {
  const halfCheckedKeys = deptRef.value?.getHalfCheckedNodes().map(node => node.id) || []
  return [...new Set([...deptCheckedKeys.value, ...halfCheckedKeys])]
}

/** 重置新增的表单以及其他数据  */
function reset() {
  menuCheckedKeys.value = []
  deptCheckedKeys.value = []
  menuExpand.value = false
  menuNodeAll.value = false
  deptExpand.value = true
  deptNodeAll.value = false
  form.value = {
    roleId: undefined,
    roleName: undefined,
    roleKey: undefined,
    roleSort: 0,
    status: "0",
    menuIds: [],
    deptIds: [],
    menuCheckStrictly: true,
    deptCheckStrictly: true,
    remark: undefined
  }
  proxy.resetForm("roleRef")
}

/** 添加角色 */
function handleAdd() {
  reset()
  getMenuTreeselect()
  open.value = true
  title.value = "添加角色"
}

/** 修改角色 */
function handleUpdate(row) {
  reset()
  const roleId = row.roleId || ids.value
  const roleMenu = getRoleMenuTreeselect(roleId)
  getRole(roleId).then(response => {
    form.value = response.data
    form.value.roleSort = Number(form.value.roleSort)
    open.value = true
    roleMenu.then(res => {
      menuCheckedKeys.value = res.checkedKeys
    })
  })
  title.value = "修改角色"
}

/** 根据角色ID查询菜单树结构 */
function getRoleMenuTreeselect(roleId) {
  return roleMenuTreeselect(roleId).then(response => {
    menuOptions.value = response.menus
    return response
  })
}

/** 根据角色ID查询部门树结构 */
function getDeptTree(roleId) {
  return deptTreeSelect(roleId).then(response => {
    deptOptions.value = response.depts
    return response
  })
}

/** 树权限（展开/折叠）*/
function handleCheckedTreeExpand(value, type) {
  if (type == "menu") {
    menuExpandedKeys.value = value ? getTreeKeys(menuOptions.value, true) : []
  } else if (type == "dept") {
    deptExpandedKeys.value = value ? getTreeKeys(deptOptions.value, true) : []
  }
}

/** 树权限（全选/全不选） */
function handleCheckedTreeNodeAll(value, type) {
  if (type == "menu") {
    menuCheckedKeys.value = value ? getTreeKeys(menuOptions.value) : []
  } else if (type == "dept") {
    deptCheckedKeys.value = value ? getTreeKeys(deptOptions.value) : []
  }
}

/** 树权限（父子联动） */
function handleCheckedTreeConnect(value, type) {
  if (type == "menu") {
    form.value.menuCheckStrictly = value ? true : false
  } else if (type == "dept") {
    form.value.deptCheckStrictly = value ? true : false
  }
}

/** 所有菜单节点数据 */
function getMenuAllCheckedKeys() {
  const halfCheckedKeys = menuRef.value?.getHalfCheckedNodes().map(node => node.id) || []
  return [...new Set([...menuCheckedKeys.value, ...halfCheckedKeys])]
}

function getTreeKeys(nodes, parentsOnly = false) {
  return nodes.flatMap(node => {
    const children = node.children || []
    const current = parentsOnly && children.length === 0 ? [] : [node.id]
    return [...current, ...getTreeKeys(children, parentsOnly)]
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["roleRef"].validate(errors => {
    if (!errors) {
      if (form.value.roleId != undefined) {
        form.value.menuIds = getMenuAllCheckedKeys()
        updateRole(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        form.value.menuIds = getMenuAllCheckedKeys()
        addRole(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 选择角色权限范围触发 */
function dataScopeSelectChange(value) {
  if (value !== "2") {
    deptCheckedKeys.value = []
  }
}

/** 分配数据权限操作 */
function handleDataScope(row) {
  reset()
  const deptTreeSelect = getDeptTree(row.roleId)
  getRole(row.roleId).then(response => {
    form.value = response.data
    openDataScope.value = true
    deptTreeSelect.then(res => {
      deptCheckedKeys.value = res.checkedKeys
    })
  })
  title.value = "分配数据权限"
}

/** 提交按钮（数据权限） */
function submitDataScope() {
  if (form.value.roleId != undefined) {
    form.value.deptIds = getDeptAllCheckedKeys()
    dataScope(form.value).then(() => {
      proxy.$modal.msgSuccess("修改成功")
      openDataScope.value = false
      getList()
    })
  }
}

/** 取消按钮（数据权限）*/
function cancelDataScope() {
  openDataScope.value = false
  reset()
}

getList()
</script>
