<template>
   <div class="app-container ui-list-page">
      <a-form :model="queryParams" ref="queryRef" layout="inline" v-show="showSearch" :label-col-props="{ flex: '68px' }" class="ui-filter-card">
         <a-form-item label="字典名称" field="dictName">
            <a-input
               v-model="queryParams.dictName"
               placeholder="请输入字典名称"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="字典类型" field="dictType">
            <a-input
               v-model="queryParams.dictType"
               placeholder="请输入字典类型"
               allow-clear
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </a-form-item>
         <a-form-item label="状态" field="status">
            <a-select
               v-model="queryParams.status"
               placeholder="字典状态"
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
               status="danger"


               @click="handleRefreshCache"
               v-hasPermi="['system:dict:remove']"
             type="outline"><template #icon><Refresh /></template>刷新缓存</a-button>
         </a-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </a-row>

      <div class="ui-table-card">
      <a-table :loading="loading" :data="typeList" :row-selection="{ type: 'checkbox', showCheckedAll: true }" :row-key="record => record.dictId" :pagination="false" @selection-change="handleSelectionChange">
         <a-table-column title="字典编号" align="center" data-index="dictId" />
         <a-table-column title="字典名称" align="center" data-index="dictName" ellipsis tooltip />
         <a-table-column title="字典类型" align="center" ellipsis tooltip>
            <template #cell="{ record, rowIndex }">
               <a class="link-type" style="cursor:pointer" @click="handleViewData(record)">{{ record.dictType }}</a>
            </template>
         </a-table-column>
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
         <a-table-column title="操作" align="center" width="280" cell-class="small-padding fixed-width">
            <template #cell="{ record, rowIndex }">
               <a-button @click="handleUpdate(record)" v-hasPermi="['system:dict:edit']"><template #icon><Edit /></template>修改</a-button>
               <a-button @click="handleDataList(record)" v-hasPermi="['system:dict:edit']"><template #icon><Operation /></template>列表</a-button>
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
         <a-form ref="dictRef" :model="form" :rules="rules" :label-col-props="{ flex: '100px' }">
            <a-form-item label="字典名称" field="dictName">
               <a-input v-model="form.dictName" placeholder="请输入字典名称" />
            </a-form-item>
            <a-form-item field="dictType">
               <a-input v-model="form.dictType" placeholder="请输入字典类型" />
               <template #label>
                 <span>
                   <a-tooltip content='数据存储中的Key值，如：sys_user_sex' position="top">
                     <span><question-filled /></span>
                   </a-tooltip>
                   字典类型
                 </span>
               </template>
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

      <dict-data-drawer v-model:visible="drawerVisible" :row="drawerRow" />
   </div>
</template>

<script setup name="Dict">
import DictDataDrawer from './detail'
import useDictStore from '@/store/modules/dict'
import { listType, getType, delType, addType, updateType, refreshCache } from "@/api/system/dict/type"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const typeList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const drawerVisible = ref(false)
const drawerRow = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: undefined,
    dictType: undefined,
    status: undefined
  },
  rules: {
    dictName: [{ required: true, message: "字典名称不能为空", trigger: "blur" }],
    dictType: [{ required: true, message: "字典类型不能为空", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

/** 查询字典类型列表 */
function getList() {
  loading.value = true
  listType(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    typeList.value = response.rows
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
    dictId: undefined,
    dictName: undefined,
    dictType: undefined,
    status: "0",
    remark: undefined
  }
  proxy.resetForm("dictRef")
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

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加字典类型"
}

/** 多选框选中数据 */
function handleSelectionChange(selectedKeys) {
  ids.value = selectedKeys
  single.value = selectedKeys.length != 1
  multiple.value = !selectedKeys.length
}

/** 字典数据抽屉 */
function handleViewData(row) {
  drawerRow.value = row
  drawerVisible.value = true
}

/** 字典数据列表页面 */
function handleDataList(row) {
  proxy.$tab.openPage("字典数据", '/system/dict-data/index/' + row.dictId)
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const dictId = row.dictId || ids.value
  getType(dictId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改字典类型"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["dictRef"].validate(errors => {
    if (!errors) {
      if (form.value.dictId != undefined) {
        updateType(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addType(form.value).then(response => {
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
  const dictIds = row.dictId || ids.value
  proxy.$modal.confirm('是否确认删除字典编号为"' + dictIds + '"的数据项？').then(function() {
    return delType(dictIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/dict/type/export", {
    ...queryParams.value
  }, `dict_${new Date().getTime()}.xlsx`)
}

/** 刷新缓存按钮操作 */
function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess("刷新成功")
    useDictStore().cleanDict()
  })
}

getList()
</script>
