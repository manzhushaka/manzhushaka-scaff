<template>
  <div class="pay-result-page">
    <main class="result-shell">
      <section class="result-header">
        <p class="pay-eyebrow">支付即开票</p>
        <h1>{{ resultTitle }}</h1>
        <p>{{ resultSubtitle }}</p>
      </section>

      <section class="status-panel">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商户订单号">{{ outTradeNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">{{ payStatusText }}</el-descriptions-item>
          <el-descriptions-item label="开票状态">{{ invoiceStatusText }}</el-descriptions-item>
        </el-descriptions>
        <div class="result-actions">
          <el-button type="primary" :loading="downloading" :disabled="!canDownload" @click="handleDownload">下载发票</el-button>
          <el-button :disabled="!polling" @click="stopPolling">取消轮询</el-button>
          <el-button @click="pollOrder">刷新</el-button>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup name="PayResult">
import { ElMessage } from 'element-plus'
import { getOrder, downloadInvoice } from '@/api/pii/pay'

const route = useRoute()
const outTradeNo = computed(() => route.query.outTradeNo)
const token = computed(() => route.query.token)
const order = ref({})
const polling = ref(false)
const downloading = ref(false)
let timer = null

const payStatusText = computed(() => statusText(order.value.payStatus, { PAID: '已支付', PENDING: '待支付', FAILED: '支付失败' }))
const invoiceStatusText = computed(() => statusText(order.value.invoiceStatus, { ISSUED: '已开票', PENDING: '待开票', FAILED: '开票失败', REVERSED: '已红冲' }))
const canDownload = computed(() => order.value.payStatus === 'PAID' && order.value.invoiceStatus === 'ISSUED')
const resultTitle = computed(() => canDownload.value ? '发票已开具' : '正在确认结果')
const resultSubtitle = computed(() => polling.value ? '系统每 3 秒自动刷新一次' : '自动刷新已停止')

onMounted(() => {
  pollOrder()
  startPolling()
})

onBeforeUnmount(stopPolling)

function statusText(value, mapping) {
  if (!value) return '-'
  return mapping[value] || value
}

function startPolling() {
  if (timer) return
  polling.value = true
  timer = window.setInterval(pollOrder, 3000)
}

function stopPolling() {
  polling.value = false
  if (timer) {
    window.clearInterval(timer)
    timer = null
  }
}

async function pollOrder() {
  if (!outTradeNo.value || !token.value) {
    ElMessage.error('缺少订单参数')
    stopPolling()
    return
  }
  const response = await getOrder(outTradeNo.value, token.value)
  order.value = response.data || response
  if (canDownload.value) {
    stopPolling()
  }
}

async function handleDownload() {
  downloading.value = true
  try {
    const blob = await downloadInvoice(outTradeNo.value, token.value)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${outTradeNo.value}.pdf`
    link.click()
    window.URL.revokeObjectURL(url)
  } finally {
    downloading.value = false
  }
}
</script>

<style scoped>
.pay-result-page {
  min-height: 100vh;
  background: #f6f7fb;
  color: #1f2937;
  padding: 24px 16px;
}

.result-shell {
  max-width: 520px;
  margin: 0 auto;
}

.result-header {
  padding: 12px 0 18px;
}

.pay-eyebrow {
  margin: 0 0 6px;
  color: #409eff;
  font-size: 14px;
}

.result-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
}

.result-header p {
  margin: 0;
  color: #667085;
}

.status-panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 18px;
}

.result-actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
  flex-wrap: wrap;
}

.result-actions .el-button {
  flex: 1 1 120px;
}
</style>
