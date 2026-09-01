<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" class="ui-filter-card">
         <a-form-item label="部门名称" field="deptName">
            <a-input
               v-model="queryParams.deptName"
               placeholder="请输入部门名称"
               allow-clear
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="状态" field="status">
            <a-select v-model="queryParams.status" placeholder="部门状态" allow-clear style="width: 200px">
               <a-option
                  v-for="dict in sys_normal_disable"
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

      <a-row class="ui-action-bar">
         <a-col :span="1.5">
            <a-button



               @click="handleAdd"
               v-hasPermi="['system:dept:add']"
             type="outline"><template #icon><Plus /></template>新增</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleSaveSort"
               v-hasPermi="['system:dept:edit']"
             type="outline"><template #icon><Check /></template>保存排序</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button



               @click="toggleExpandAll"
             type="outline"><template #icon><Sort /></template>展开/折叠</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table
         v-if="refreshTable"
         :loading="loading"
         :data="deptList"
         row-key="deptId"
         :default-expand-all-rows="isExpandAll"
       :pagination="false">
         <template #columns>
         <a-table-column data-index="deptName" title="部门名称" width="260"></a-table-column>
         <a-table-column data-index="orderNum" title="排序" width="200">
            <template #cell="{ record, rowIndex }">
               <a-input-number v-model="record.orderNum" controls-position="right" :min="0" style="width: 88px" />
            </template>
         </a-table-column>
         <a-table-column data-index="status" title="状态" width="100">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_normal_disable" :value="record.status" />
            </template>
         </a-table-column>
         <a-table-column title="创建时间" align="center" data-index="createTime" width="200">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.createTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" width="240" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button type="text" @click="handleUpdate(record)" v-hasPermi="['system:dept:edit']"><template #icon><Edit /></template>修改</a-button>
               <a-button type="text" @click="handleAdd(record)" v-hasPermi="['system:dept:add']"><template #icon><Plus /></template>新增</a-button>
               <a-button type="text" status="danger" v-if="record.parentId != 0" @click="handleDelete(record)" v-hasPermi="['system:dept:remove']"><template #icon><Delete /></template>删除</a-button>
            </template>
         </a-table-column>
         </template>
      </a-table>
      </div>

      <!-- 添加或修改部门对话框 -->
      <a-modal :title="title" v-model:visible="open" width="600px" render-to-body>
         <a-form ref="deptRef" :model="form" :rules="rules" :label-col-props="{ flex: '80px' }">
            <a-row>
               <a-col :span="24" v-if="form.parentId !== 0">
                  <a-form-item label="上级部门" field="parentId">
                     <a-tree-select
                        v-model="form.parentId"
                        :data="deptOptions"
                        :field-names="{ key: 'deptId', title: 'deptName', children: 'children' }"
                        placeholder="选择上级部门"
                     />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="部门名称" field="deptName">
                     <a-input v-model="form.deptName" placeholder="请输入部门名称" />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="显示排序" field="orderNum">
                     <a-input-number v-model="form.orderNum" controls-position="right" :min="0" />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="负责人" field="leader">
                     <a-input v-model="form.leader" placeholder="请输入负责人" maxlength="20" />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="联系电话" field="phone">
                     <a-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="邮箱" field="email">
                     <a-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
                  </a-form-item>
               </a-col>
               <a-col :span="12">
                  <a-form-item label="部门状态">
                     <a-radio-group v-model="form.status">
                        <a-radio
                           v-for="dict in sys_normal_disable"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</a-radio>
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
   </div>
</template>

<script setup name="Dept">
import { listDept, getDept, delDept, addDept, updateDept, updateDeptSort, listDeptExcludeChild } from "@/api/system/dept"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const deptList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref("")
const deptOptions = ref([])
const isExpandAll = ref(true)
const refreshTable = ref(true)
const originalOrders = ref({})

const data = reactive({
  form: {},
  queryParams: {
    deptName: undefined,
    status: undefined
  },
  rules: {
    parentId: [{ required: true, message: "上级部门不能为空", trigger: "blur" }],
    deptName: [{ required: true, message: "部门名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "显示排序不能为空", trigger: "blur" }],
    email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
    phone: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

/** 查询部门列表 */
function getList() {
  loading.value = true
  listDept(queryParams.value).then(response => {
    deptList.value = proxy.handleTree(response.data, "deptId")
    recordOriginalOrders(deptList.value)
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
    deptId: undefined,
    parentId: undefined,
    deptName: undefined,
    orderNum: 0,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: "0"
  }
  proxy.resetForm("deptRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset()
  listDept().then(response => {
    deptOptions.value = proxy.handleTree(response.data, "deptId")
  })
  if (row != undefined) {
    form.value.parentId = row.deptId
  }
  open.value = true
  title.value = "添加部门"
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  listDeptExcludeChild(row.deptId).then(response => {
    deptOptions.value = proxy.handleTree(response.data, "deptId")
  })
  getDept(row.deptId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改部门"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["deptRef"].validate(errors => {
    if (!errors) {
      if (form.value.deptId != undefined) {
        updateDept(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDept(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 递归记录原始排序 */
function recordOriginalOrders(list) {
  list.forEach(item => {
    originalOrders.value[item.deptId] = item.orderNum
    if (item.children && item.children.length) {
      recordOriginalOrders(item.children)
    }
  })
}

/** 保存排序 */
function handleSaveSort() {
  const changedDeptIds = []
  const changedOrderNums = []
  const collectChanged = (list) => {
    list.forEach(item => {
      if (String(originalOrders.value[item.deptId]) !== String(item.orderNum)) {
        changedDeptIds.push(item.deptId)
        changedOrderNums.push(item.orderNum)
      }
      if (item.children && item.children.length) {
        collectChanged(item.children)
      }
    })
  }
  collectChanged(deptList.value)
  if (changedDeptIds.length === 0) {
   proxy.$modal.msgWarning("未检测到排序修改")
    return
  }
  updateDeptSort({ deptIds: changedDeptIds.join(","), orderNums: changedOrderNums.join(",") }).then(() => {
   proxy.$modal.msgSuccess("排序保存成功")
    recordOriginalOrders(deptList.value)
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.deptName + '"的数据项?').then(function() {
    return delDept(row.deptId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
