<template>
  <view class="page">
    <!-- 发票图片 -->
    <view class="iip-card">
      <view class="label">发票图片 <text class="required">*</text></view>
      <view class="uploader" @click="chooseImage">
        <image v-if="preview" class="uploader__image" :src="preview" mode="aspectFill" />
        <view v-else class="uploader__placeholder">
          <view class="uploader__icon" :style="{ backgroundImage: icons.camera }"></view>
          <text class="uploader__text">点击上传发票图片</text>
        </view>
      </view>
      <view v-if="preview" class="uploader__tip">已上传，点击可重新选择</view>
    </view>

    <!-- 发票信息表单 -->
    <view class="iip-card form">
      <view class="form__item">
        <view class="label">发票号码 <text class="required">*</text></view>
        <input class="form__input" v-model.trim="form.invoiceNo" maxlength="30" placeholder="请输入发票号码" />
      </view>
      <view class="form__item">
        <view class="label">发票代码</view>
        <input class="form__input" v-model.trim="form.invoiceCode" maxlength="20" placeholder="请输入发票代码（选填）" />
      </view>
      <view class="form__item">
        <view class="label">发票金额（元） <text class="required">*</text></view>
        <input class="form__input" v-model="form.amount" type="digit" placeholder="请输入发票金额" />
      </view>
      <view class="form__item">
        <view class="label">开票日期</view>
        <picker mode="date" :value="form.invoiceDate" @change="onDateChange">
          <view class="form__input form__input--picker" :class="{ 'form__input--empty': !form.invoiceDate }">
            {{ form.invoiceDate || '请选择开票日期' }}
          </view>
        </picker>
      </view>
      <view class="form__item">
        <view class="label">商户名称 <text class="required">*</text></view>
        <input class="form__input" v-model.trim="form.merchantName" maxlength="128" placeholder="请输入开票商户名称" />
      </view>
      <view class="form__item">
        <view class="label">商户ID</view>
        <input class="form__input" v-model="form.merchantId" type="number" placeholder="参与商户ID（选填）" />
      </view>
    </view>

    <button class="iip-btn submit" :disabled="submitting" @click="handleSubmit">
      {{ submitting ? '提交中' : '提交发票' }}
    </button>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { submitInvoice } from '@/api/invoice.js'
import { uploadFile } from '@/common/request.js'
import icons from '@/common/icons.js'

const form = reactive({
  invoiceNo: '',
  invoiceCode: '',
  amount: '',
  invoiceDate: '',
  merchantName: '',
  merchantId: ''
})

/** 已上传的发票图片相对地址（提交用 fileName） */
const imageUrl = ref('')
/** 预览地址（本地临时路径或后端完整 URL） */
const preview = ref('')
const submitting = ref(false)

function onDateChange(e) {
  form.invoiceDate = e.detail.value
}

/**
 * 选图并上传到 /common/upload（带 token），成功后将 fileName 作为 imageUrl 提交。
 */
function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0]
      preview.value = filePath
      try {
        // 上传响应体：{code, msg, url, fileName, newFileName}
        const body = await uploadFile(filePath)
        imageUrl.value = body.fileName
        preview.value = body.url
      } catch (e) {
        preview.value = ''
        imageUrl.value = ''
      }
    }
  })
}

/**
 * 表单校验并提交发票。
 */
async function handleSubmit() {
  if (submitting.value) {
    return
  }
  if (!imageUrl.value) {
    uni.showToast({ title: '请先上传发票图片', icon: 'none' })
    return
  }
  if (!form.invoiceNo) {
    uni.showToast({ title: '请输入发票号码', icon: 'none' })
    return
  }
  const amount = Number(form.amount)
  if (!form.amount || isNaN(amount) || amount <= 0) {
    uni.showToast({ title: '请输入大于 0 的发票金额', icon: 'none' })
    return
  }
  if (!form.merchantName) {
    uni.showToast({ title: '请输入商户名称', icon: 'none' })
    return
  }
  const payload = {
    invoiceNo: form.invoiceNo,
    amount,
    merchantName: form.merchantName,
    imageUrl: imageUrl.value
  }
  if (form.invoiceCode) {
    payload.invoiceCode = form.invoiceCode
  }
  if (form.invoiceDate) {
    payload.invoiceDate = form.invoiceDate
  }
  if (form.merchantId) {
    payload.merchantId = Number(form.merchantId)
  }
  submitting.value = true
  try {
    await submitInvoice(payload)
    uni.showToast({ title: '提交成功，等待审核', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/invoice/list' })
    }, 800)
  } catch (e) {
    // 错误提示已由 request 封装统一 toast（如发票号码重复）
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.label {
  font-size: 26rpx;
  color: var(--iip-text-secondary);
  margin-bottom: 16rpx;
}
.required {
  color: var(--iip-danger);
}

.uploader {
  border: 2rpx dashed var(--iip-border);
  border-radius: 12rpx;
  height: 320rpx;
  overflow: hidden;
}
.uploader__image {
  width: 100%;
  height: 100%;
}
.uploader__placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.uploader__icon {
  width: 88rpx;
  height: 88rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.uploader__text {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: var(--iip-text-muted);
}
.uploader__tip {
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--iip-text-muted);
  text-align: center;
}

.form {
  margin-top: 24rpx;
}
.form__item {
  margin-bottom: 28rpx;
}
.form__item:last-child {
  margin-bottom: 0;
}
.form__input {
  height: 84rpx;
  border: 1rpx solid var(--iip-border);
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  background-color: #ffffff;
  color: var(--iip-text);
  display: flex;
  align-items: center;
}
.form__input--empty {
  color: var(--iip-text-muted);
}

.submit {
  margin-top: 40rpx;
}
</style>
