<template>
  <div class="pay-page">
    <main class="pay-shell">
      <section class="pay-header">
        <p class="pay-eyebrow">支付即开票</p>
        <h1>扫码支付开票</h1>
        <p>{{ merchantName || '请选择税目并填写发票信息' }}</p>
      </section>

      <section v-if="loading" class="state-panel">
        <el-skeleton :rows="6" animated />
      </section>

      <section v-else-if="!config" class="state-panel">
        <el-result icon="error" title="二维码无效" :sub-title="errorMessage || '请确认二维码是否正确或是否已过期'" />
      </section>

      <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top" class="pay-form">
        <el-form-item label="税目" prop="taxItemId">
          <tax-item-selector v-model="form.taxItemId" :items="config.taxItems || []" @change="onTaxItemChange" />
        </el-form-item>
        <el-form-item label="金额（元）" prop="amountYuan">
          <el-input-number v-model="form.amountYuan" :min="0.01" :max="100000" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发票抬头" prop="buyerName">
          <el-input v-model="form.buyerName" placeholder="请填写发票抬头" maxlength="128" />
        </el-form-item>
        <el-form-item label="税号">
          <el-input v-model="form.buyerTaxCode" placeholder="选填，企业发票必填" maxlength="32" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.buyerEmail" placeholder="选填，填写后发送电子发票" maxlength="64" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.buyerMobile" placeholder="选填，填写后发送短信" maxlength="16" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="submitting" class="pay-submit" @click="onSubmit">立即支付</el-button>
      </el-form>
    </main>
  </div>
</template>

<script setup name="PayIndex">
import { ElMessage } from 'element-plus'
import TaxItemSelector from './components/TaxItemSelector.vue'
import { getQrcodeConfig, precreate } from '@/api/pii/pay'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(true)
const submitting = ref(false)
const config = ref()
const errorMessage = ref('')
const merchantName = computed(() => config.value && (config.value.merchantName || config.value.name))

const form = ref({
  taxItemId: undefined,
  amountYuan: undefined,
  buyerName: undefined,
  buyerTaxCode: undefined,
  buyerEmail: undefined,
  buyerMobile: undefined
})

const rules = {
  taxItemId: [{ required: true, message: '请选择税目', trigger: 'change' }],
  amountYuan: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  buyerName: [{ required: true, message: '请填写发票抬头', trigger: 'blur' }]
}

onMounted(loadConfig)

async function loadConfig() {
  loading.value = true
  errorMessage.value = ''
  try {
    const code = route.query.code
    if (!code) {
      errorMessage.value = '缺少二维码编码'
      config.value = undefined
      return
    }
    const response = await getQrcodeConfig(code)
    config.value = response.data || response
    applyDefaultTaxItem()
  } catch (error) {
    errorMessage.value = error && error.message ? error.message : '加载失败'
    config.value = undefined
  } finally {
    loading.value = false
  }
}

function applyDefaultTaxItem() {
  const taxItems = config.value && config.value.taxItems ? config.value.taxItems : []
  if (!taxItems.length) return
  const first = taxItems[0]
  form.value.taxItemId = first.id
  applyDefaultAmount(first)
}

function onTaxItemChange(taxItemId) {
  const taxItems = config.value && config.value.taxItems ? config.value.taxItems : []
  const selected = taxItems.find(item => item.id === taxItemId)
  applyDefaultAmount(selected)
}

function applyDefaultAmount(taxItem) {
  if (!taxItem) return
  const amount = taxItem.defaultAmount || taxItem.amount
  if (amount) {
    form.value.amountYuan = Number(amount) / 100
  }
}

async function onSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const amount = Math.round(Number(form.value.amountYuan) * 100)
    const response = await precreate({
      code: route.query.code,
      taxItemId: form.value.taxItemId,
      amount,
      buyerName: form.value.buyerName,
      buyerTaxCode: form.value.buyerTaxCode,
      buyerEmail: form.value.buyerEmail,
      buyerMobile: form.value.buyerMobile
    })
    const payInfo = response.data || response
    router.push({
      name: 'PayResult',
      query: { outTradeNo: payInfo.outTradeNo, token: payInfo.orderToken }
    })
    invokeWechatPay(payInfo)
  } finally {
    submitting.value = false
  }
}

function invokeWechatPay(payInfo) {
  if (typeof WeixinJSBridge === 'undefined') {
    ElMessage.warning('请在微信内打开')
    return
  }
  WeixinJSBridge.invoke('getBrandWCPayRequest', {
    appId: payInfo.appId,
    timeStamp: payInfo.timeStamp,
    nonceStr: payInfo.nonceStr,
    package: payInfo.packageStr,
    signType: payInfo.signType,
    paySign: payInfo.paySign
  }, res => {
    if (res.err_msg === 'get_brand_wcpay_request:ok') {
      ElMessage.success('支付处理中')
    } else if (res.err_msg === 'get_brand_wcpay_request:cancel') {
      ElMessage.info('已取消支付')
    } else {
      ElMessage.error('支付失败')
    }
  })
}
</script>

<style scoped>
.pay-page {
  min-height: 100vh;
  background: #f6f7fb;
  color: #1f2937;
  padding: 24px 16px;
}

.pay-shell {
  max-width: 520px;
  margin: 0 auto;
}

.pay-header {
  padding: 12px 0 18px;
}

.pay-eyebrow {
  margin: 0 0 6px;
  color: #409eff;
  font-size: 14px;
}

.pay-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
}

.pay-header p {
  margin: 0;
  color: #667085;
}

.pay-form,
.state-panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 18px;
}

.pay-submit {
  width: 100%;
}
</style>
