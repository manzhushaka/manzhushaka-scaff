<template>
   <div class="app-container ui-list-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
         <el-form-item label="商户编号" prop="merchantNo">
            <el-input
               v-model="queryParams.merchantNo"
               placeholder="请输入商户编号"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="商户名称" prop="merchantName">
            <el-input
               v-model="queryParams.merchantName"
               placeholder="请输入商户名称"
               clearable
               style="width: 240px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="商户状态" clearable style="width: 240px">
               <el-option
                  v-for="item in MERCHANT_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="创建时间" style="width: 308px;">
            <el-date-picker
               v-model="dateRange"
               value-format="YYYY-MM-DD"
               type="daterange"
               range-separator="-"
               start-placeholder="开始日期"
               end-placeholder="结束日期"
            ></el-date-picker>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8 ui-action-bar">
         <el-col :span="1.5">
            <el-button
               type="primary"
               plain
               icon="Plus"
               @click="handleAdd"
               v-hasPermi="['iip:merchant:add']"
            >新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="success"
               plain
               icon="Edit"
               :disabled="single"
               @click="handleUpdate"
               v-hasPermi="['iip:merchant:edit']"
            >修改</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="danger"
               plain
               icon="Delete"
               :disabled="multiple"
               @click="handleDelete"
               v-hasPermi="['iip:merchant:remove']"
            >删除</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['iip:merchant:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div class="ui-table-card">
      <el-table v-loading="loading" :data="merchantList" @selection-change="handleSelectionChange">
         <el-table-column type="selection" width="55" align="center" />
         <el-table-column label="商户编号" align="center" prop="merchantNo" width="130" />
         <el-table-column label="商户名称" align="center" prop="merchantName" :show-overflow-tooltip="true" />
         <el-table-column label="分类" align="center" prop="category" width="100">
            <template #default="scope">
               <span>{{ scope.row.category || "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="联系人" align="center" prop="contactName" width="100">
            <template #default="scope">
               <span>{{ scope.row.contactName || "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="联系电话" align="center" prop="contactPhone" width="130" />
         <el-table-column label="地址" align="center" prop="address" :show-overflow-tooltip="true" />
         <el-table-column label="状态" align="center" prop="status" width="100">
            <template #default="scope">
               <el-tag :type="formatMerchantStatus(scope.row.status).type">
                  {{ formatMerchantStatus(scope.row.status).label }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['iip:merchant:edit']">修改</el-button>
               <el-button link type="warning" icon="Stamp" @click="handleAudit(scope.row)" v-if="scope.row.status === '2'" v-hasPermi="['iip:merchant:audit']">审核</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iip:merchant:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>
      </div>

      <pagination
         v-show="total > 0"
         :total="total"
         v-model:page="queryParams.pageNum"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
      />

      <!-- 添加或修改商户对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" append-to-body>
         <el-form ref="merchantRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="商户名称" prop="merchantName">
               <el-input v-model="form.merchantName" placeholder="请输入商户名称" maxlength="128" />
            </el-form-item>
            <el-form-item label="商户分类" prop="category">
               <el-select v-model="form.category" placeholder="请选择或输入分类" clearable filterable allow-create style="width: 100%">
                  <el-option
                     v-for="item in CATEGORY_OPTIONS"
                     :key="item"
                     :label="item"
                     :value="item"
                  />
               </el-select>
            </el-form-item>
            <el-form-item label="所在市县" prop="city">
               <el-input v-model="form.city" placeholder="请输入所在市县" maxlength="64" />
            </el-form-item>
            <el-form-item label="联系人" prop="contactName">
               <el-input v-model="form.contactName" placeholder="请输入联系人" maxlength="64" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
               <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" />
            </el-form-item>
            <el-form-item label="商户地址" prop="address">
               <el-input v-model="form.address" placeholder="请输入商户地址" maxlength="255" />
            </el-form-item>
            <el-form-item label="营业时间" prop="businessHours">
               <el-input v-model="form.businessHours" placeholder="如 09:00-22:00" maxlength="64" />
            </el-form-item>
            <el-row :gutter="16">
               <el-col :span="12">
                  <el-form-item label="经度" prop="longitude">
                     <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" controls-position="right" placeholder="如 116.404128" style="width: 100%" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="纬度" prop="latitude">
                     <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" controls-position="right" placeholder="如 39.904989" style="width: 100%" />
                  </el-form-item>
               </el-col>
            </el-row>
            <el-form-item label="营业执照" prop="businessLicense">
               <image-upload v-model="form.businessLicense" :limit="1" />
            </el-form-item>
            <el-form-item label="商户logo" prop="logo">
               <image-upload v-model="form.logo" :limit="1" />
            </el-form-item>
            <el-form-item label="商家介绍" prop="description">
               <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商家介绍" maxlength="500" />
            </el-form-item>
            <el-form-item label="绑定会员ID" prop="memberId">
               <el-input-number v-model="form.memberId" :min="1" :precision="0" controls-position="right" placeholder="绑定后该会员可在小程序端核销" style="width: 100%" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
               <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" maxlength="500" />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 审核商户对话框 -->
      <el-dialog title="商户审核" v-model="auditOpen" width="500px" append-to-body>
         <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="90px">
            <el-form-item label="商户名称">
               <span>{{ auditForm.merchantName }}</span>
            </el-form-item>
            <el-form-item label="审核结论" prop="approve">
               <el-radio-group v-model="auditForm.approve">
                  <el-radio :value="true">通过</el-radio>
                  <el-radio :value="false">驳回</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item label="审核备注" prop="auditRemark">
               <el-input
                  v-model="auditForm.auditRemark"
                  type="textarea"
                  :placeholder="auditForm.approve === false ? '驳回时必须填写驳回原因' : '请输入审核备注（选填）'"
                  maxlength="255"
               />
            </el-form-item>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitAudit">确 定</el-button>
               <el-button @click="cancelAudit">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Merchant">
import { ElLoading, ElMessage } from "element-plus"
import { saveAs } from "file-saver"
import { blobValidate } from "@/utils/manzhushaka"
import { listMerchant, getMerchant, addMerchant, updateMerchant, delMerchant, auditMerchant, exportMerchant } from "@/api/iip/merchant"

const { proxy } = getCurrentInstance()

/**
 * 商户状态映射（0正常 1停用 2待审核，与发票状态含义不同，以 MerchantResult 为准），
 * iip 业务状态不使用系统字典
 */
const MERCHANT_STATUS_MAP = {
  "0": { label: "正常", type: "success" },
  "1": { label: "停用", type: "info" },
  "2": { label: "待审核", type: "warning" }
}
const MERCHANT_STATUS_OPTIONS = Object.keys(MERCHANT_STATUS_MAP).map(value => ({
  value,
  label: MERCHANT_STATUS_MAP[value].label
}))
/** 商户分类常用选项（允许自定义输入） */
const CATEGORY_OPTIONS = ["餐饮", "住宿", "加油", "景区", "零售", "其他"]

const merchantList = ref([])
const open = ref(false)
const auditOpen = ref(false)
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
  auditForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    merchantNo: undefined,
    merchantName: undefined,
    status: undefined
  },
  rules: {
    merchantName: [{ required: true, message: "商户名称不能为空", trigger: "blur" }]
  },
  auditRules: {
    approve: [{ required: true, message: "审核结论不能为空", trigger: "change" }],
    auditRemark: [{
      validator: (rule, value, callback) => {
        if (auditForm.value.approve === false && !value) {
          callback(new Error("驳回时必须填写审核备注"))
        } else {
          callback()
        }
      },
      trigger: "blur"
    }]
  }
})

const { queryParams, form, auditForm, rules, auditRules } = toRefs(data)

/** 格式化商户状态 */
function formatMerchantStatus(status) {
  return MERCHANT_STATUS_MAP[status] || { label: "未知", type: "info" }
}

/** 查询商户列表 */
function getList() {
  loading.value = true
  listMerchant(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    merchantList.value = response.rows
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
    merchantId: undefined,
    merchantName: undefined,
    category: undefined,
    city: undefined,
    contactName: undefined,
    contactPhone: undefined,
    address: undefined,
    businessHours: undefined,
    longitude: undefined,
    latitude: undefined,
    businessLicense: undefined,
    logo: undefined,
    description: undefined,
    memberId: undefined,
    remark: undefined
  }
  proxy.resetForm("merchantRef")
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
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.merchantId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加商户"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const merchantId = row.merchantId || ids.value
  getMerchant(merchantId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改商户"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["merchantRef"].validate(valid => {
    if (valid) {
      if (form.value.merchantId != undefined) {
        updateMerchant(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMerchant(form.value).then(response => {
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
  const merchantIds = row.merchantId || ids.value
  proxy.$modal.confirm('是否确认删除商户编号为"' + merchantIds + '"的数据项？').then(function () {
    return delMerchant(merchantIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 审核按钮操作 */
function handleAudit(row) {
  auditForm.value = {
    merchantId: row.merchantId,
    merchantName: row.merchantName,
    approve: true,
    auditRemark: undefined
  }
  proxy.resetForm("auditRef")
  auditOpen.value = true
}

/** 取消审核 */
function cancelAudit() {
  auditOpen.value = false
  proxy.resetForm("auditRef")
}

/** 提交审核（通过置正常、驳回置停用，驳回必须填写备注） */
function submitAudit() {
  proxy.$refs["auditRef"].validate(valid => {
    if (valid) {
      auditMerchant({
        merchantId: auditForm.value.merchantId,
        approve: auditForm.value.approve,
        auditRemark: auditForm.value.auditRemark
      }).then(() => {
        proxy.$modal.msgSuccess("审核成功")
        auditOpen.value = false
        getList()
      })
    }
  })
}

/** 导出按钮操作（后端导出为 GET 接口，使用 request 的 GET + blob 方式下载保存） */
function handleExport() {
  const loadingInstance = ElLoading.service({ text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)" })
  exportMerchant(proxy.addDateRange(queryParams.value, dateRange.value)).then(async data => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, `merchant_${new Date().getTime()}.xlsx`)
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      ElMessage.error(rspObj.msg || "下载文件出现错误")
    }
  }).catch(() => {
    ElMessage.error("下载文件出现错误，请联系管理员！")
  }).finally(() => {
    loadingInstance.close()
  })
}

getList()
</script>
