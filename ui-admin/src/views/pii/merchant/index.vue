<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px" class="ui-filter-card">
      <el-form-item label="商户名称" prop="merchantName">
        <el-input v-model="queryParams.merchantName" placeholder="请输入商户名称" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="银商商户号" prop="umsMerchantId">
        <el-input v-model="queryParams.umsMerchantId" placeholder="请输入银商商户号" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="商户状态" clearable style="width: 160px">
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:merchant:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:merchant:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:merchant:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="ui-table-card">
      <el-table v-loading="loading" :data="merchantList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="商户名称" align="center" prop="merchantName" min-width="180" :show-overflow-tooltip="true" />
        <el-table-column label="所属部门" align="center" prop="deptId" width="110" />
        <el-table-column label="银商商户号" align="center" prop="umsMerchantId" min-width="150" />
        <el-table-column label="终端号" align="center" prop="umsTerminalId" min-width="120" />
        <el-table-column label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:merchant:edit']">修改</el-button>
            <el-button link type="primary" icon="Setting" @click="handleConfig(scope.row)" v-hasPermi="['biz:merchant:config']">参数</el-button>
            <el-button link type="primary" icon="Switch" @click="handleChangeStatus(scope.row)" v-hasPermi="['biz:merchant:changeStatus']">
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:merchant:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="merchantRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="上级部门ID" prop="parentDeptId" v-if="!form.id">
          <el-input-number v-model="form.parentDeptId" :min="1" />
        </el-form-item>
        <el-form-item label="商户名称" prop="merchantName">
          <el-input v-model="form.merchantName" placeholder="请输入商户名称" maxlength="64" />
        </el-form-item>
        <el-form-item label="管理员账号" prop="adminUserName" v-if="!form.id">
          <el-input v-model="form.adminUserName" placeholder="请输入管理员账号" maxlength="30" />
        </el-form-item>
        <el-form-item label="初始密码" prop="adminPassword" v-if="!form.id">
          <el-input v-model="form.adminPassword" type="password" show-password placeholder="请输入初始密码" maxlength="20" />
        </el-form-item>
        <el-form-item label="管理员手机" prop="adminPhone" v-if="!form.id">
          <el-input v-model="form.adminPhone" placeholder="请输入管理员手机" maxlength="11" />
        </el-form-item>
        <el-form-item label="管理员邮箱" prop="adminEmail" v-if="!form.id">
          <el-input v-model="form.adminEmail" placeholder="请输入管理员邮箱" maxlength="50" />
        </el-form-item>
        <el-form-item label="银商商户号" prop="umsMerchantId">
          <el-input v-model="form.umsMerchantId" placeholder="请输入银商商户号" maxlength="32" />
        </el-form-item>
        <el-form-item label="银商终端号" prop="umsTerminalId">
          <el-input v-model="form.umsTerminalId" placeholder="请输入银商终端号" maxlength="32" />
        </el-form-item>
        <el-form-item label="支付签名密钥">
          <el-input v-model="form.umsPaySignKey" type="password" show-password placeholder="留空则不修改" maxlength="128" />
        </el-form-item>
        <el-form-item label="发票签名密钥">
          <el-input v-model="form.umsInvoiceSignKey" type="password" show-password placeholder="留空则不修改" maxlength="128" />
        </el-form-item>
        <el-form-item label="发票来源">
          <el-input v-model="form.invoiceMsgSrc" placeholder="请输入 invoice msgSrc" maxlength="32" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
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

<script setup name="Merchant">
import { listMerchant, getMerchant, addMerchant, updateMerchant, delMerchant, changeMerchantStatus } from '@/api/pii/merchant'

const { proxy } = getCurrentInstance()
const router = useRouter()
const merchantList = ref([])
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
  queryParams: { pageNum: 1, pageSize: 10, merchantName: undefined, umsMerchantId: undefined, status: undefined },
  rules: {
    parentDeptId: [{ required: true, message: '上级部门不能为空', trigger: 'blur' }],
    merchantName: [{ required: true, message: '商户名称不能为空', trigger: 'blur' }],
    adminUserName: [{ required: true, message: '管理员账号不能为空', trigger: 'blur' }],
    adminPassword: [{ required: true, message: '初始密码不能为空', trigger: 'blur' }],
    umsMerchantId: [{ required: true, message: '银商商户号不能为空', trigger: 'blur' }],
    umsTerminalId: [{ required: true, message: '银商终端号不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMerchant(queryParams.value).then(response => {
    merchantList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function reset() {
  form.value = {
    id: undefined,
    parentDeptId: 1,
    merchantName: undefined,
    adminUserName: undefined,
    adminPassword: undefined,
    adminPhone: undefined,
    adminEmail: undefined,
    umsMerchantId: undefined,
    umsTerminalId: undefined,
    umsPaySignKey: undefined,
    umsInvoiceSignKey: undefined,
    invoiceMsgSrc: undefined,
    status: 1,
    remark: undefined
  }
  proxy.resetForm('merchantRef')
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
  title.value = '添加商户'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getMerchant(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改商户'
  })
}

function submitForm() {
  proxy.$refs.merchantRef.validate(valid => {
    if (!valid) return
    const action = form.value.id ? updateMerchant(form.value) : addMerchant(form.value)
    action.then(() => {
      proxy.$modal.msgSuccess(form.value.id ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const merchantIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的商户？').then(() => delMerchant(merchantIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleChangeStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const text = nextStatus === 1 ? '启用' : '停用'
  proxy.$modal.confirm(`确认要${text}商户"${row.merchantName}"吗？`).then(() => changeMerchantStatus(row.id, nextStatus)).then(() => {
    row.status = nextStatus
    proxy.$modal.msgSuccess(`${text}成功`)
  }).catch(() => {})
}

function handleConfig(row) {
  router.push('/pii/merchant/config/' + row.deptId)
}

getList()
</script>
