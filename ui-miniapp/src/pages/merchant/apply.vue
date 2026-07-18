<template>
  <view class="page">
    <view class="iip-card form">
      <view class="form__item">
        <view class="label">商户名称 <text class="required">*</text></view>
        <input class="form__input" v-model.trim="form.merchantName" maxlength="128" placeholder="请输入商户名称" />
      </view>
      <view class="form__item">
        <view class="label">商户分类 <text class="required">*</text></view>
        <picker mode="selector" :range="categories" :value="categoryIndex" @change="onCategoryChange">
          <view class="form__input form__input--picker" :class="{ 'form__input--empty': !form.category }">
            {{ form.category || '请选择商户分类' }}
          </view>
        </picker>
      </view>
      <view class="form__item">
        <view class="label">联系人 <text class="required">*</text></view>
        <input class="form__input" v-model.trim="form.contactName" maxlength="64" placeholder="请输入联系人姓名" />
      </view>
      <view class="form__item">
        <view class="label">联系电话 <text class="required">*</text></view>
        <input class="form__input" v-model.trim="form.contactPhone" type="number" maxlength="20" placeholder="请输入联系电话" />
      </view>
      <view class="form__item">
        <view class="label">商户地址 <text class="required">*</text></view>
        <input class="form__input" v-model.trim="form.address" maxlength="255" placeholder="请输入商户地址" />
      </view>
      <view class="form__item">
        <view class="label">营业执照 <text class="required">*</text></view>
        <view class="uploader" @click="chooseLicense">
          <image v-if="preview" class="uploader__image" :src="preview" mode="aspectFill" />
          <view v-else class="uploader__placeholder">
            <view class="uploader__icon" :style="{ backgroundImage: icons.camera }"></view>
            <text class="uploader__text">上传营业执照照片</text>
          </view>
        </view>
      </view>
    </view>

    <button class="iip-btn submit" :disabled="submitting" @click="handleSubmit">
      {{ submitting ? '提交中' : '提交申请' }}
    </button>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { applyMerchant } from '@/api/merchant.js'
import { uploadFile } from '@/common/request.js'
import icons from '@/common/icons.js'

/** 商户分类选项（与后台管理端录入口径一致） */
const categories = ['餐饮', '住宿', '加油', '零售', '景区', '其他']

const form = reactive({
  merchantName: '',
  category: '',
  contactName: '',
  contactPhone: '',
  address: '',
  businessLicense: ''
})

const categoryIndex = ref(-1)
const preview = ref('')
const submitting = ref(false)

function onCategoryChange(e) {
  categoryIndex.value = Number(e.detail.value)
  form.category = categories[categoryIndex.value]
}

/**
 * 选择并上传营业执照图片（/common/upload，带 token）。
 */
function chooseLicense() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0]
      preview.value = filePath
      try {
        const body = await uploadFile(filePath)
        form.businessLicense = body.fileName
        preview.value = body.url
      } catch (e) {
        preview.value = ''
        form.businessLicense = ''
      }
    }
  })
}

/**
 * 校验并提交入驻申请，成功后返回商户中心。
 */
async function handleSubmit() {
  if (submitting.value) {
    return
  }
  if (!form.merchantName) {
    uni.showToast({ title: '请输入商户名称', icon: 'none' })
    return
  }
  if (!form.category) {
    uni.showToast({ title: '请选择商户分类', icon: 'none' })
    return
  }
  if (!form.contactName) {
    uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
    return
  }
  if (!form.contactPhone) {
    uni.showToast({ title: '请输入联系电话', icon: 'none' })
    return
  }
  if (!form.address) {
    uni.showToast({ title: '请输入商户地址', icon: 'none' })
    return
  }
  if (!form.businessLicense) {
    uni.showToast({ title: '请上传营业执照', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await applyMerchant({ ...form })
    uni.showToast({ title: '申请已提交，等待审核', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack({
        fail: () => {
          uni.redirectTo({ url: '/pages/merchant/center' })
        }
      })
    }, 800)
  } catch (e) {
    // 错误提示已由 request 封装统一 toast（如重复申请）
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

.uploader {
  border: 2rpx dashed var(--iip-border);
  border-radius: 12rpx;
  height: 280rpx;
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
  width: 80rpx;
  height: 80rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.uploader__text {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: var(--iip-text-muted);
}

.submit {
  margin-top: 40rpx;
}
</style>
