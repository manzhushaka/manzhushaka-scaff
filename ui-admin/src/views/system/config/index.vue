<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
         <a-form-item label="参数名称" field="configName">
            <a-input
               v-model="queryParams.configName"
               placeholder="请输入参数名称"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="参数键名" field="configKey">
            <a-input
               v-model="queryParams.configKey"
               placeholder="请输入参数键名"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="系统内置" field="configType">
            <a-select v-model="queryParams.configType" placeholder="系统内置" allow-clear style="width: 240px">
               <a-option
                  v-for="dict in sys_yes_no"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
               />
            </a-select>
         </a-form-item>
         <a-form-item label="创建时间" style="width: 308px;">
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
               v-hasPermi="['system:config:add']"
             type="outline"><template #icon><Plus /></template>新增</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="success"


               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['system:config:edit']"
             type="outline"><template #icon><Edit /></template>修改</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['system:config:remove']"
             type="outline"><template #icon><Delete /></template>删除</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleExport"
               v-hasPermi="['system:config:export']"
             type="outline"><template #icon><Download /></template>导出</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               @click="handleRefreshCache"
               v-hasPermi="['system:config:remove']"
             type="outline"><template #icon><Refresh /></template>刷新缓存</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table :loading="loading" :data="configList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.configId" :pagination="false" @selection-change="handleSelectionChange">
         <template #columns>
         <a-table-column title="参数主键" align="center" data-index="configId" />
         <a-table-column title="参数名称" align="center" data-index="configName" ellipsis tooltip />
         <a-table-column title="参数键名" align="center" data-index="configKey" ellipsis tooltip />
         <a-table-column title="参数键值" align="center" data-index="configValue" ellipsis tooltip />
         <a-table-column title="系统内置" align="center" data-index="configType">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_yes_no" :value="record.configType" />
            </template>
         </a-table-column>
         <a-table-column title="备注" align="center" data-index="remark" ellipsis tooltip />
         <a-table-column title="创建时间" align="center" data-index="createTime" width="180">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.createTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" width="150" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button @click="handleUpdate(record)" v-hasPermi="['system:config:edit']" ><template #icon><Edit /></template>修改</a-button>
               <a-button @click="handleDelete(record)" v-hasPermi="['system:config:remove']"><template #icon><Delete /></template>删除</a-button>
            </template>
         </a-table-column>
         </template>
      </a-table>
      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />
      </div>

      <!-- 添加或修改参数配置对话框 -->
      <a-modal :title="title" v-model:visible="open" width="500px" render-to-body>
         <a-form ref="configRef" :model="form" :rules="rules" :label-col-props="{ flex: '80px' }">
            <a-form-item label="参数名称" field="configName">
               <a-input v-model="form.configName" placeholder="请输入参数名称" />
            </a-form-item>
            <a-form-item label="参数键名" field="configKey">
               <a-input v-model="form.configKey" placeholder="请输入参数键名" />
            </a-form-item>
            <a-form-item label="参数键值" field="configValue">
               <a-textarea v-model="form.configValue" placeholder="请输入参数键值"  />
            </a-form-item>
            <a-form-item label="系统内置" field="configType">
               <a-radio-group v-model="form.configType">
                  <a-radio
                     v-for="dict in sys_yes_no"
                     :key="dict.value"
                     :value="dict.value"
                  >{{ dict.label }}</a-radio>
               </a-radio-group>
            </a-form-item>
            <a-form-item label="备注" field="remark">
               <a-textarea v-model="form.remark" placeholder="请输入内容"  />
            </a-form-item>
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

<script setup name="Config">
import { listConfig, getConfig, delConfig, addConfig, updateConfig, refreshCache } from "@/api/system/config"

const { proxy } = getCurrentInstance()
const { sys_yes_no } = useDict("sys_yes_no")

const configList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configName: undefined,
    configKey: undefined,
    configType: undefined
  },
  rules: {
    configName: [{ required: true, message: "参数名称不能为空", trigger: "blur" }],
    configKey: [{ required: true, message: "参数键名不能为空", trigger: "blur" }],
    configValue: [{ required: true, message: "参数键值不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询参数列表 */
function getList() {
  loading.value = true
  listConfig(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    configList.value = response.rows
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
    configId: undefined,
    configName: undefined,
    configKey: undefined,
    configValue: undefined,
    configType: "Y",
    remark: undefined
  }
  proxy.resetForm("configRef")
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

/** 多选框选中数据 */
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  single.value = selectedKeys.length != 1
  multiple.value = !selectedKeys.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加参数"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const configId = row.configId || ids.value
  getConfig(configId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改参数"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["configRef"].validate(errors => {
    if (!errors) {
      if (form.value.configId != undefined) {
        updateConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addConfig(form.value).then(response => {
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
  const configIds = row.configId || ids.value
  proxy.$modal.confirm('是否确认删除参数编号为"' + configIds + '"的数据项？').then(function () {
    return delConfig(configIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/config/export", {
    ...queryParams.value
  }, `config_${new Date().getTime()}.xlsx`)
}

/** 刷新缓存按钮操作 */
function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess("刷新缓存成功")
  })
}

getList()
</script>
