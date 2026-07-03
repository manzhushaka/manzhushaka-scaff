<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px" class="ui-filter-card">
      <el-form-item label="税目编码" prop="taxItemCode">
        <el-input v-model="queryParams.taxItemCode" placeholder="请输入税目编码" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="税目名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入税目名称" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="税目状态" clearable style="width: 160px">
          <el-option label="正常" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 ui-action-bar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:taxItem:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:taxItem:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:taxItem:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="ui-table-card">
      <el-table v-loading="loading" :data="taxItemList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="税目编码" align="center" prop="taxItemCode" min-width="170" :show-overflow-tooltip="true" />
        <el-table-column label="名称" align="center" prop="name" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="税率" align="center" prop="taxRate" width="100">
          <template #default="scope">{{ scope.row.taxRate }}%</template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:taxItem:edit']">修改</el-button>
            <el-button link type="primary" icon="Switch" @click="handleChangeStatus(scope.row)" v-hasPermi="['biz:taxItem:changeStatus']">
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:taxItem:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="620px" append-to-body>
      <el-form ref="taxItemRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="税目编码" prop="taxItemCode">
          <el-input v-model="form.taxItemCode" placeholder="请输入税目编码" maxlength="32" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入税目名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类" maxlength="64" />
        </el-form-item>
        <el-form-item label="税率" prop="taxRate">
          <el-input-number v-model="form.taxRate" :min="0" :max="100" :precision="2" :step="0.01" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TaxItem">
import { listTaxItem, getTaxItem, addTaxItem, updateTaxItem, delTaxItem, changeTaxItemStatus } from '@/api/pii/taxItem'

const { proxy } = getCurrentInstance()

const taxItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    taxItemCode: undefined,
    name: undefined,
    status: undefined
  },
  rules: {
    taxItemCode: [{ required: true, message: '税目编码不能为空', trigger: 'blur' }],
    name: [{ required: true, message: '税目名称不能为空', trigger: 'blur' }],
    taxRate: [{ required: true, message: '税率不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listTaxItem(queryParams.value).then(response => {
    taxItemList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    id: undefined,
    taxItemCode: undefined,
    name: undefined,
    category: undefined,
    taxRate: 0,
    sort: 0,
    status: 1,
    remark: undefined
  }
  proxy.resetForm('taxItemRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加税目'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getTaxItem(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改税目'
  })
}

function submitForm() {
  proxy.$refs.taxItemRef.validate(valid => {
    if (!valid) {
      return
    }
    const action = form.value.id ? updateTaxItem(form.value) : addTaxItem(form.value)
    action.then(() => {
      proxy.$modal.msgSuccess(form.value.id ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const taxItemIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的税目？').then(() => delTaxItem(taxItemIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleChangeStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const text = nextStatus === 1 ? '启用' : '停用'
  proxy.$modal.confirm(`确认要${text}税目"${row.name}"吗？`).then(() => changeTaxItemStatus(row.id, nextStatus)).then(() => {
    row.status = nextStatus
    proxy.$modal.msgSuccess(`${text}成功`)
  }).catch(() => {})
}

getList()
</script>
