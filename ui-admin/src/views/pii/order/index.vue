<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px" class="ui-filter-card">
      <el-form-item label="商户ID" prop="merchantId">
        <el-input-number v-model="queryParams.merchantId" :min="1" controls-position="right" style="width: 160px" />
      </el-form-item>
      <el-form-item label="订单号" prop="outTradeNo">
        <el-input v-model="queryParams.outTradeNo" placeholder="请输入订单号" clearable style="width: 240px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="支付状态" clearable style="width: 150px">
          <el-option label="待支付" value="PENDING" />
          <el-option label="已支付" value="PAID" />
          <el-option label="退款中" value="REFUNDING" />
          <el-option label="已退款" value="REFUNDED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
      </el-form-item>
      <el-form-item label="发票状态" prop="invoiceStatus">
        <el-select v-model="queryParams.invoiceStatus" placeholder="发票状态" clearable style="width: 150px">
          <el-option label="未开票" value="NONE" />
          <el-option label="开票中" value="OPENING" />
          <el-option label="已开票" value="ISSUED" />
          <el-option label="红冲中" value="REVERSING" />
          <el-option label="已红冲" value="REVERSED" />
          <el-option label="失败" value="FAILED" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付时间">
        <el-date-picker
          v-model="payTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          range-separator="-"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 ui-action-bar">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['biz:order:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <div class="ui-table-card">
      <el-table v-loading="loading" :data="orderList">
        <el-table-column label="订单号" prop="outTradeNo" min-width="190" :show-overflow-tooltip="true" />
        <el-table-column label="商户ID" prop="merchantId" width="100" />
        <el-table-column label="金额" align="right" width="120">
          <template #default="scope">{{ formatCentAmount(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column label="退款金额" align="right" width="120">
          <template #default="scope">{{ formatCentAmount(scope.row.refundAmount) }}</template>
        </el-table-column>
        <el-table-column label="购方名称" prop="buyerName" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column label="支付状态" align="center" width="110">
          <template #default="scope">
            <el-tag :type="payStatusType(scope.row.payStatus)">{{ payStatusText(scope.row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发票状态" align="center" width="110">
          <template #default="scope">
            <el-tag :type="invoiceStatusType(scope.row.invoiceStatus)">{{ invoiceStatusText(scope.row.invoiceStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" align="center" prop="payTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.payTime) || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="180">
          <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['biz:order:query']">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-drawer v-model="detailOpen" title="订单详情" size="720px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单号" :span="2">{{ detail.outTradeNo }}</el-descriptions-item>
        <el-descriptions-item label="商户ID">{{ detail.merchantId }}</el-descriptions-item>
        <el-descriptions-item label="二维码ID">{{ detail.qrcodeId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="税目ID">{{ detail.taxItemId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="银联订单日">{{ detail.umsMerOrderDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">{{ formatCentAmount(detail.amount) }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">{{ formatCentAmount(detail.refundAmount) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ payStatusText(detail.payStatus) }}</el-descriptions-item>
        <el-descriptions-item label="支付流水">{{ detail.payTradeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ parseTime(detail.payTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票状态">{{ invoiceStatusText(detail.invoiceStatus) }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ detail.invoiceNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票代码">{{ detail.invoiceCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开票时间">{{ parseTime(detail.invoiceIssueTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="红冲时间">{{ parseTime(detail.invoiceReverseTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购方名称">{{ detail.buyerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购方税号">{{ detail.buyerTaxCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购方邮箱">{{ detail.buyerEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购方手机">{{ detail.buyerMobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="PDF地址" :span="2">{{ detail.invoicePdfUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup name="PiiOrder">
import { listOrder, getOrder } from '@/api/pii/order'

const { proxy } = getCurrentInstance()
const orderList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const payTimeRange = ref([])
const detailOpen = ref(false)
const detail = ref(null)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    merchantId: undefined,
    outTradeNo: undefined,
    payStatus: undefined,
    invoiceStatus: undefined,
    payTimeBegin: undefined,
    payTimeEnd: undefined
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  syncPayTimeRange()
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  payTimeRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleDetail(row) {
  getOrder(row.id).then(response => {
    detail.value = response.data
    detailOpen.value = true
  })
}

function handleExport() {
  syncPayTimeRange()
  proxy.download('pii/order/export', { ...queryParams.value }, `pii_order_${new Date().getTime()}.xlsx`)
}

function syncPayTimeRange() {
  queryParams.value.payTimeBegin = payTimeRange.value && payTimeRange.value.length === 2 ? payTimeRange.value[0] : undefined
  queryParams.value.payTimeEnd = payTimeRange.value && payTimeRange.value.length === 2 ? payTimeRange.value[1] : undefined
}

function formatCentAmount(amount) {
  return `${(Number(amount || 0) / 100).toFixed(2)} 元`
}

function payStatusText(status) {
  return {
    PENDING: '待支付',
    PAID: '已支付',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
    CLOSED: '已关闭'
  }[status] || status || '-'
}

function payStatusType(status) {
  return {
    PENDING: 'warning',
    PAID: 'success',
    REFUNDING: 'warning',
    REFUNDED: 'info',
    CLOSED: 'info'
  }[status] || 'info'
}

function invoiceStatusText(status) {
  return {
    NONE: '未开票',
    OPENING: '开票中',
    ISSUED: '已开票',
    REVERSING: '红冲中',
    REVERSED: '已红冲',
    FAILED: '失败'
  }[status] || status || '-'
}

function invoiceStatusType(status) {
  return {
    NONE: 'info',
    OPENING: 'warning',
    ISSUED: 'success',
    REVERSING: 'warning',
    REVERSED: 'info',
    FAILED: 'danger'
  }[status] || 'info'
}

getList()
</script>
