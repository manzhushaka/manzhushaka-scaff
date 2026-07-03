<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px" class="ui-filter-card">
      <el-form-item label="商户ID" prop="merchantId">
        <el-input-number v-model="queryParams.merchantId" :min="1" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 ui-action-bar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:qrcode:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:qrcode:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:qrcode:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="ui-table-card">
      <el-table v-loading="loading" :data="qrcodeList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="名称" prop="name" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="商户ID" prop="merchantId" width="100" />
        <el-table-column label="编码" prop="qrcodeCode" min-width="140" />
        <el-table-column label="绑定税目" min-width="220">
          <template #default="scope">
            <div class="tax-item-tags">
              <el-tag v-for="item in normalizeTaxItems(scope.row.taxItems)" :key="item.taxItemId" size="small" effect="plain">
                {{ item.taxItemName }}
                <span v-if="item.defaultAmount"> / {{ formatCentAmount(item.defaultAmount) }}元</span>
              </el-tag>
              <span v-if="!scope.row.taxItems || !scope.row.taxItems.length">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:qrcode:edit']">修改</el-button>
            <el-button link type="primary" icon="Switch" @click="handleChangeStatus(scope.row)" v-hasPermi="['biz:qrcode:changeStatus']">
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:qrcode:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="800px" append-to-body class="pii-form-dialog">
      <el-form ref="qrcodeRef" :model="form" :rules="rules" label-width="110px" class="pii-dialog-form">
        <div class="pii-dialog-section">
          <div class="pii-dialog-section__title">二维码信息</div>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <el-form-item label="商户ID" prop="merchantId">
                <el-input-number v-model="form.merchantId" :min="1" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="二维码编码" prop="qrcodeCode">
                <el-input v-model="form.qrcodeCode" maxlength="64" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="二维码名称" prop="name">
                <el-input v-model="form.name" maxlength="64" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio :value="1">正常</el-radio>
                  <el-radio :value="0">停用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="过期时间">
                <el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="请选择过期时间" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="绑定税目">
                <el-select v-model="selectedTaxItemIds" multiple filterable style="width: 100%" placeholder="请选择税目">
                  <el-option v-for="item in taxItemOptions" :key="item.id" :label="item.taxItemName" :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col v-if="selectedTaxItems.length" :span="24">
              <el-form-item label="税目详情">
                <el-table :data="selectedTaxItems" size="small" border>
                  <el-table-column label="税目编码" prop="taxItemCode" min-width="170" />
                  <el-table-column label="税目名称" prop="taxItemName" min-width="150" />
                  <el-table-column label="税率" width="100">
                    <template #default="scope">{{ formatTaxRate(scope.row.taxRate) }}</template>
                  </el-table-column>
                  <el-table-column label="默认金额" width="150">
                    <template #default="scope">
                      <el-input-number v-model="scope.row.defaultAmount" :min="0" :precision="0" controls-position="right" style="width: 120px" />
                    </template>
                  </el-table-column>
                </el-table>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="form.remark" type="textarea" maxlength="255" :rows="3" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
      <template #footer>
        <div class="pii-dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Qrcode">
import { listQrcode, getQrcode, addQrcode, updateQrcode, delQrcode, changeQrcodeStatus } from '@/api/pii/qrcode'
import { listTaxItem } from '@/api/pii/taxItem'

const { proxy } = getCurrentInstance()
const qrcodeList = ref([])
const taxItemOptions = ref([])
const selectedTaxItemIds = ref([])
const selectedTaxItems = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, merchantId: undefined },
  rules: {
    merchantId: [{ required: true, message: '商户ID不能为空', trigger: 'blur' }],
    qrcodeCode: [{ required: true, message: '二维码编码不能为空', trigger: 'blur' }],
    name: [{ required: true, message: '二维码名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

watch(selectedTaxItemIds, syncSelectedTaxItems)

function getList() {
  loading.value = true
  listQrcode(queryParams.value).then(response => {
    qrcodeList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function loadTaxItems() {
  listTaxItem({ pageNum: 1, pageSize: 200, status: 1 }).then(response => {
    taxItemOptions.value = response.rows || []
    syncSelectedTaxItems()
  })
}

function reset() {
  form.value = { id: undefined, merchantId: queryParams.value.merchantId, qrcodeCode: undefined, name: undefined, status: 1, expireTime: undefined, remark: undefined, taxItems: [] }
  selectedTaxItemIds.value = []
  selectedTaxItems.value = []
  proxy.resetForm('qrcodeRef')
}

function cancel() {
  open.value = false
  reset()
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
  title.value = '添加二维码'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getQrcode(id).then(response => {
    form.value = response.data
    selectedTaxItemIds.value = (response.data.taxItems || []).map(item => item.taxItemId)
    syncSelectedTaxItems()
    open.value = true
    title.value = '修改二维码'
  })
}

function submitForm() {
  proxy.$refs.qrcodeRef.validate(valid => {
    if (!valid) return
    form.value.taxItems = selectedTaxItems.value.map(item => ({ taxItemId: item.taxItemId, defaultAmount: item.defaultAmount }))
    const action = form.value.id ? updateQrcode(form.value) : addQrcode(form.value)
    action.then(() => {
      proxy.$modal.msgSuccess(form.value.id ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const qrcodeIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的二维码？').then(() => delQrcode(qrcodeIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleChangeStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const text = nextStatus === 1 ? '启用' : '停用'
  proxy.$modal.confirm(`确认要${text}二维码"${row.name}"吗？`).then(() => changeQrcodeStatus(row.id, nextStatus)).then(() => {
    row.status = nextStatus
    proxy.$modal.msgSuccess(`${text}成功`)
  }).catch(() => {})
}

function normalizeTaxItems(taxItems) {
  if (!taxItems || !taxItems.length) return []
  return taxItems.map(item => {
    const taxItem = taxItemOptions.value.find(option => option.id === item.taxItemId)
    return {
      ...item,
      taxItemName: taxItem ? taxItem.taxItemName : item.taxItemId
    }
  })
}

function formatTaxRate(rate) {
  if (rate === undefined || rate === null || rate === '') return '-'
  return `${rate}%`
}

function formatCentAmount(amount) {
  return (Number(amount || 0) / 100).toFixed(2)
}

function syncSelectedTaxItems() {
  const currentMap = new Map(selectedTaxItems.value.map(item => [item.taxItemId, item]))
  selectedTaxItems.value = selectedTaxItemIds.value.map(id => {
    const existing = currentMap.get(id)
    const option = taxItemOptions.value.find(item => item.id === id) || {}
    const relation = (form.value.taxItems || []).find(item => item.taxItemId === id) || {}
    return {
      taxItemId: id,
      taxItemCode: option.taxItemCode || id,
      taxItemName: option.taxItemName || id,
      taxRate: option.taxRate,
      defaultAmount: existing ? existing.defaultAmount : (relation.defaultAmount || 0)
    }
  })
}

loadTaxItems()
getList()
</script>

<style scoped>
.tax-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
