<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" class="ui-filter-card">
         <a-form-item label="字典名称" field="dictType">
            <a-select v-model="queryParams.dictType" style="width: 200px">
               <a-option
                  v-for="item in typeOptions"
                  :key="item.dictId"
                  :label="item.dictName"
                  :value="item.dictType"
               />
            </a-select>
         </a-form-item>
         <a-form-item label="字典标签" field="dictLabel">
            <a-input
               v-model="queryParams.dictLabel"
               placeholder="请输入字典标签"
               allow-clear
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="状态" field="status">
            <a-select v-model="queryParams.status" placeholder="数据状态" allow-clear style="width: 200px">
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

      <a-row :gutter="10" class="mb8 ui-action-bar">
         <a-col :span="1.5">
            <a-button



               @click="handleAdd"
               v-hasPermi="['system:dict:add']"
             type="outline"><template #icon><Plus /></template>新增</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="success"


               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['system:dict:edit']"
             type="outline"><template #icon><Edit /></template>修改</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="danger"


               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['system:dict:remove']"
             type="outline"><template #icon><Delete /></template>删除</a-button>
         </a-col>
         <a-col :span="1.5">
            <a-button
               status="warning"


               @click="handleExport"
               v-hasPermi="['system:dict:export']"
             type="outline"><template #icon><Download /></template>导出</a-button>
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
      <a-table :loading="loading" :data="dataList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.dictCode" :pagination="false" @selection-change="handleSelectionChange">
         <a-table-column title="字典编码" align="center" data-index="dictCode" />
         <a-table-column title="字典标签" align="center" data-index="dictLabel">
            <template #cell="{ record, rowIndex }">
               <span v-if="(record.listClass == '' || record.listClass == 'default') && (record.cssClass == '' || record.cssClass == null)">{{ record.dictLabel }}</span>
               <a-tag v-else :color="getTagColor(record.listClass)" :class="record.cssClass">{{ record.dictLabel }}</a-tag>
            </template>
         </a-table-column>
         <a-table-column title="字典键值" align="center" data-index="dictValue" />
         <a-table-column title="字典排序" align="center" data-index="dictSort" />
         <a-table-column title="状态" align="center" data-index="status">
            <template #cell="{ record, rowIndex }">
               <dict-tag :options="sys_normal_disable" :value="record.status" />
            </template>
         </a-table-column>
         <a-table-column title="备注" align="center" data-index="remark" ellipsis tooltip />
         <a-table-column title="创建时间" align="center" data-index="createTime" width="180">
            <template #cell="{ record, rowIndex }">
               <span>{{ parseTime(record.createTime) }}</span>
            </template>
         </a-table-column>
         <a-table-column title="操作" align="center" width="160" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button @click="handleUpdate(record)" v-hasPermi="['system:dict:edit']"><template #icon><Edit /></template>修改</a-button>
               <a-button @click="handleDelete(record)" v-hasPermi="['system:dict:remove']"><template #icon><Delete /></template>删除</a-button>
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

      <!-- 添加或修改参数配置对话框 -->
      <a-modal :title="title" v-model:visible="open" width="500px" render-to-body>
         <a-form ref="dataRef" :model="form" :rules="rules" :label-col-props="{ flex: '80px' }">
            <a-form-item label="字典类型">
               <a-input v-model="form.dictType" :disabled="true" />
            </a-form-item>
            <a-form-item label="数据标签" field="dictLabel">
               <a-input v-model="form.dictLabel" placeholder="请输入数据标签" />
            </a-form-item>
            <a-form-item label="数据键值" field="dictValue">
               <a-input v-model="form.dictValue" placeholder="请输入数据键值" />
            </a-form-item>
            <a-form-item label="样式属性" field="cssClass">
               <a-input v-model="form.cssClass" placeholder="请输入样式属性" />
            </a-form-item>
            <a-form-item label="显示排序" field="dictSort">
               <a-input-number v-model="form.dictSort" controls-position="right" :min="0" />
            </a-form-item>
            <a-form-item label="回显样式" field="listClass">
               <a-select v-model="form.listClass">
                  <a-option
                     v-for="item in listClassOptions"
                     :key="item.value"
                     :label="item.label + '(' + item.value + ')'"
                     :value="item.value"
                  ></a-option>
               </a-select>
            </a-form-item>
            <a-form-item label="状态" field="status">
               <a-radio-group v-model="form.status">
                  <a-radio
                     v-for="dict in sys_normal_disable"
                     :key="dict.value"
                     :value="dict.value"
                  >{{ dict.label }}</a-radio>
               </a-radio-group>
            </a-form-item>
            <a-form-item label="备注" field="remark">
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
   </div>
</template>

<script setup name="Data">
import useDictStore from '@/store/modules/dict'
import { optionselect as getDictOptionselect, getType } from "@/api/system/dict/type"
import { listData, getData, delData, addData, updateData } from "@/api/system/dict/data"

const { proxy } = getCurrentInstance()

function getTagColor(type) {
  return { primary: 'arcoblue', success: 'green', warning: 'orange', danger: 'red', info: 'gray' }[type]
}
const { sys_normal_disable } = useDict("sys_normal_disable")

const dataList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const defaultDictType = ref("")
const typeOptions = ref([])
const route = useRoute()
// 数据标签回显样式
const listClassOptions = ref([
  { value: "default", label: "默认" },
  { value: "primary", label: "主要" },
  { value: "success", label: "成功" },
  { value: "info", label: "信息" },
  { value: "warning", label: "警告" },
  { value: "danger", label: "危险" }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictType: undefined,
    dictLabel: undefined,
    status: undefined
  },
  rules: {
    dictLabel: [{ required: true, message: "数据标签不能为空", trigger: "blur" }],
    dictValue: [{ required: true, message: "数据键值不能为空", trigger: "blur" }],
    dictSort: [{ required: true, message: "数据顺序不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询字典类型详细 */
function getTypes(dictId) {
  getType(dictId).then(response => {
    queryParams.value.dictType = response.data.dictType
    defaultDictType.value = response.data.dictType
    getList()
  })
}

/** 查询字典类型列表 */
function getTypeList() {
  getDictOptionselect().then(response => {
    typeOptions.value = response.data
  })
}

/** 查询字典数据列表 */
function getList() {
  loading.value = true
  listData(queryParams.value).then(response => {
    dataList.value = response.rows
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
    dictCode: undefined,
    dictLabel: undefined,
    dictValue: undefined,
    cssClass: undefined,
    listClass: "default",
    dictSort: 0,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("dataRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 返回按钮操作 */
function handleClose() {
  const obj = { path: "/system/dict" }
  proxy.$tab.closeOpenPage(obj)
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.dictType = defaultDictType.value
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加字典数据"
  form.value.dictType = queryParams.value.dictType
}

/** 多选框选中数据 */
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  single.value = selectedKeys.length != 1
  multiple.value = !selectedKeys.length
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const dictCode = row.dictCode || ids.value
  getData(dictCode).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改字典数据"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["dataRef"].validate(errors => {
    if (!errors) {
      if (form.value.dictCode != undefined) {
        updateData(form.value).then(response => {
          useDictStore().removeDict(queryParams.value.dictType)
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addData(form.value).then(response => {
          useDictStore().removeDict(queryParams.value.dictType)
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
  const dictCodes = row.dictCode || ids.value
  proxy.$modal.confirm('是否确认删除字典编码为"' + dictCodes + '"的数据项？').then(function() {
    return delData(dictCodes)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
    useDictStore().removeDict(queryParams.value.dictType)
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/dict/data/export", {
    ...queryParams.value
  }, `dict_data_${new Date().getTime()}.xlsx`)
}

getTypes(route.params && route.params.dictId)
getTypeList()
</script>
