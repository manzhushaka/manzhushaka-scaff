<template>
  <div class="app-container">
    <el-page-header title="返回" :content="pageTitle" @back="goBack" />

    <el-form ref="configRef" :model="form" :rules="rules" label-width="140px" class="merchant-config-form">
      <el-divider content-position="left">银商参数</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="商户名称">
            <el-input v-model="form.merchantName" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="部门ID">
            <el-input v-model="form.deptId" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银商商户号" prop="umsMerchantId">
            <el-input v-model="form.umsMerchantId" maxlength="32" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银商终端号" prop="umsTerminalId">
            <el-input v-model="form.umsTerminalId" maxlength="32" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="支付签名密钥">
            <el-input v-model="form.umsPaySignKey" type="password" show-password :placeholder="payKeyPlaceholder" maxlength="128" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发票签名密钥">
            <el-input v-model="form.umsInvoiceSignKey" type="password" show-password :placeholder="invoiceKeyPlaceholder" maxlength="128" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发票来源">
            <el-input v-model="form.invoiceMsgSrc" maxlength="32" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="通知地址">
            <el-input v-model="form.notifyUrl" maxlength="255" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">销方开票信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="销方名称">
            <el-input v-model="form.invoiceSellerName" maxlength="128" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="销方税号">
            <el-input v-model="form.invoiceSellerTaxCode" maxlength="32" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="销方地址">
            <el-input v-model="form.invoiceSellerAddress" maxlength="255" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="销方电话">
            <el-input v-model="form.invoiceSellerTelephone" maxlength="32" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开户银行">
            <el-input v-model="form.invoiceSellerBank" maxlength="128" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银行账号">
            <el-input v-model="form.invoiceSellerAccount" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="收款人">
            <el-input v-model="form.invoicePayee" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="复核人">
            <el-input v-model="form.invoiceChecker" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="开票人">
            <el-input v-model="form.invoiceDrawer" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" maxlength="255" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item>
        <el-button type="primary" icon="Check" @click="submitForm" v-hasPermi="['biz:merchant:config']">保存</el-button>
        <el-button icon="Refresh" @click="loadConfig">刷新</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup name="MerchantConfig">
import { getMerchantConfig, updateMerchantConfig } from '@/api/pii/merchant'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const pageTitle = ref('商户参数配置')

const data = reactive({
  form: {},
  rules: {
    umsMerchantId: [{ required: true, message: '银商商户号不能为空', trigger: 'blur' }],
    umsTerminalId: [{ required: true, message: '银商终端号不能为空', trigger: 'blur' }]
  }
})
const { form, rules } = toRefs(data)

const payKeyPlaceholder = computed(() => form.value.umsPaySignKeyMasked ? `已配置：${form.value.umsPaySignKeyMasked}` : '请输入支付签名密钥')
const invoiceKeyPlaceholder = computed(() => form.value.umsInvoiceSignKeyMasked ? `已配置：${form.value.umsInvoiceSignKeyMasked}` : '请输入发票签名密钥')

function loadConfig() {
  getMerchantConfig(route.params.deptId).then(response => {
    form.value = {
      ...response.data,
      umsPaySignKey: undefined,
      umsInvoiceSignKey: undefined
    }
    pageTitle.value = response.data.merchantName ? `${response.data.merchantName} - 参数配置` : '商户参数配置'
  })
}

function submitForm() {
  proxy.$refs.configRef.validate(valid => {
    if (!valid) return
    updateMerchantConfig(form.value).then(() => {
      proxy.$modal.msgSuccess('保存成功')
      loadConfig()
    })
  })
}

function goBack() {
  router.push('/pii/merchant')
}

loadConfig()
</script>

<style scoped>
.merchant-config-form {
  margin-top: 18px;
  max-width: 1120px;
}
</style>
