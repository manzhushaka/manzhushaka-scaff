<template>
   <div class="app-container ui-list-page">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px" class="ui-filter-card">
         <el-form-item label="发票号码" prop="invoiceNo">
            <el-input
               v-model="queryParams.invoiceNo"
               placeholder="请输入发票号码"
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
            <el-select v-model="queryParams.status" placeholder="发票状态" clearable style="width: 240px">
               <el-option
                  v-for="item in INVOICE_STATUS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
               />
            </el-select>
         </el-form-item>
         <el-form-item label="上传时间" style="width: 308px;">
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
               type="warning"
               plain
               icon="Download"
               @click="handleExport"
               v-hasPermi="['iip:invoice:export']"
            >导出</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <div class="ui-table-card">
      <el-table v-loading="loading" :data="invoiceList">
         <el-table-column label="发票号码" align="center" prop="invoiceNo" width="120" />
         <el-table-column label="发票代码" align="center" prop="invoiceCode" width="120">
            <template #default="scope">
               <span>{{ scope.row.invoiceCode || "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="商户名称" align="center" prop="merchantName" :show-overflow-tooltip="true" />
         <el-table-column label="金额(元)" align="center" prop="amount" width="110" />
         <el-table-column label="开票日期" align="center" prop="invoiceDate" width="110">
            <template #default="scope">
               <span>{{ parseTime(scope.row.invoiceDate, "{y}-{m}-{d}") }}</span>
            </template>
         </el-table-column>
         <el-table-column label="发放积分" align="center" prop="points" width="90">
            <template #default="scope">
               <span>{{ scope.row.status === "1" && scope.row.points != null ? scope.row.points : "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="状态" align="center" prop="status" width="100">
            <template #default="scope">
               <el-tag :type="formatInvoiceStatus(scope.row.status).type">
                  {{ formatInvoiceStatus(scope.row.status).label }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="审核人" align="center" prop="auditBy" width="100">
            <template #default="scope">
               <span>{{ scope.row.auditBy || "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="审核时间" align="center" prop="auditTime" width="180">
            <template #default="scope">
               <span>{{ scope.row.auditTime ? parseTime(scope.row.auditTime) : "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="审核备注" align="center" prop="auditRemark" :show-overflow-tooltip="true">
            <template #default="scope">
               <span>{{ scope.row.auditRemark || "-" }}</span>
            </template>
         </el-table-column>
         <el-table-column label="上传时间" align="center" prop="createTime" width="180">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width" fixed="right">
            <template #default="scope">
               <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['iip:invoice:query']">查看</el-button>
               <el-button link type="warning" icon="Stamp" @click="handleAudit(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['iip:invoice:audit']">审核</el-button>
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

      <!-- 查看发票对话框 -->
      <el-dialog title="发票详情" v-model="viewOpen" width="640px" append-to-body>
         <el-descriptions :column="2" border>
            <el-descriptions-item label="发票号码">{{ viewForm.invoiceNo }}</el-descriptions-item>
            <el-descriptions-item label="发票代码">{{ viewForm.invoiceCode || "-" }}</el-descriptions-item>
            <el-descriptions-item label="商户名称">{{ viewForm.merchantName }}</el-descriptions-item>
            <el-descriptions-item label="发票金额">{{ viewForm.amount }} 元</el-descriptions-item>
            <el-descriptions-item label="开票日期">{{ parseTime(viewForm.invoiceDate, "{y}-{m}-{d}") }}</el-descriptions-item>
            <el-descriptions-item label="发放积分">{{ viewForm.status === "1" && viewForm.points != null ? viewForm.points : "-" }}</el-descriptions-item>
            <el-descriptions-item label="状态">
               <el-tag :type="formatInvoiceStatus(viewForm.status).type">
                  {{ formatInvoiceStatus(viewForm.status).label }}
               </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="上传时间">{{ parseTime(viewForm.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核人">{{ viewForm.auditBy || "-" }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ viewForm.auditTime ? parseTime(viewForm.auditTime) : "-" }}</el-descriptions-item>
            <el-descriptions-item label="审核备注" :span="2">{{ viewForm.auditRemark || "-" }}</el-descriptions-item>
            <el-descriptions-item label="发票图片" :span="2">
               <image-preview :src="viewForm.imageUrl" :width="'100%'" :height="'360px'" />
            </el-descriptions-item>
         </el-descriptions>
         <template #footer>
            <div class="dialog-footer">
               <el-button @click="viewOpen = false">关 闭</el-button>
            </div>
         </template>
      </el-dialog>

      <!-- 审核发票对话框 -->
      <el-dialog title="发票审核" v-model="auditOpen" width="500px" append-to-body>
         <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="90px">
            <el-form-item label="发票号码">
               <span>{{ auditForm.invoiceNo }}</span>
            </el-form-item>
            <el-form-item label="发票金额">
               <span>{{ auditForm.amount }} 元</span>
            </el-form-item>
            <el-form-item label="审核结果" prop="pass">
               <el-radio-group v-model="auditForm.pass">
                  <el-radio :value="true">通过</el-radio>
                  <el-radio :value="false">驳回</el-radio>
               </el-radio-group>
            </el-form-item>
            <el-form-item v-if="auditForm.pass === true">
               <el-alert
                  title="审核通过后将按当前生效活动的积分比例折算并发放积分（发票金额 × 活动比例）"
                  type="info"
                  :closable="false"
                  show-icon
               />
            </el-form-item>
            <el-form-item label="审核备注" prop="auditRemark">
               <el-input
                  v-model="auditForm.auditRemark"
                  type="textarea"
                  :placeholder="auditForm.pass === false ? '驳回时必须填写驳回原因' : '请输入审核备注（选填）'"
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

<script setup name="Invoice">
import { ElLoading, ElMessage } from "element-plus"
import { saveAs } from "file-saver"
import { blobValidate } from "@/utils/manzhushaka"
import { listInvoice, getInvoice, auditInvoice, exportInvoice } from "@/api/iip/invoice"

const { proxy } = getCurrentInstance()

/**
 * 发票状态映射（0待审核 1已通过 2已驳回，与商户状态含义不同，以 InvoiceResult 为准），
 * iip 业务状态不使用系统字典
 */
const INVOICE_STATUS_MAP = {
  "0": { label: "待审核", type: "warning" },
  "1": { label: "已通过", type: "success" },
  "2": { label: "已驳回", type: "danger" }
}
const INVOICE_STATUS_OPTIONS = Object.keys(INVOICE_STATUS_MAP).map(value => ({
  value,
  label: INVOICE_STATUS_MAP[value].label
}))

const invoiceList = ref([])
const viewOpen = ref(false)
const auditOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

const data = reactive({
  viewForm: {},
  auditForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    invoiceNo: undefined,
    merchantName: undefined,
    status: undefined
  },
  auditRules: {
    pass: [{ required: true, message: "审核结果不能为空", trigger: "change" }],
    auditRemark: [{
      validator: (rule, value, callback) => {
        if (auditForm.value.pass === false && !value) {
          callback(new Error("驳回时必须填写驳回原因"))
        } else {
          callback()
        }
      },
      trigger: "blur"
    }]
  }
})

const { queryParams, viewForm, auditForm, auditRules } = toRefs(data)

/** 格式化发票状态 */
function formatInvoiceStatus(status) {
  return INVOICE_STATUS_MAP[status] || { label: "未知", type: "info" }
}

/** 查询发票列表 */
function getList() {
  loading.value = true
  listInvoice(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    invoiceList.value = response.rows
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

/** 查看按钮操作 */
function handleView(row) {
  getInvoice(row.invoiceId).then(response => {
    viewForm.value = response.data
    viewOpen.value = true
  })
}

/** 审核按钮操作 */
function handleAudit(row) {
  auditForm.value = {
    invoiceId: row.invoiceId,
    invoiceNo: row.invoiceNo,
    amount: row.amount,
    pass: true,
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

/** 提交审核（通过按当前活动比例发放积分，驳回必须填写原因） */
function submitAudit() {
  proxy.$refs["auditRef"].validate(valid => {
    if (valid) {
      auditInvoice({
        invoiceId: auditForm.value.invoiceId,
        pass: auditForm.value.pass,
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
  exportInvoice(proxy.addDateRange(queryParams.value, dateRange.value)).then(async data => {
    const isBlob = blobValidate(data)
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, `invoice_${new Date().getTime()}.xlsx`)
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
