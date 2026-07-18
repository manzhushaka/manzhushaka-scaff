<template>
  <view class="page">
    <!-- 核销结果页 -->
    <view v-if="result" class="result iip-card">
      <view class="result__check" :style="{ backgroundImage: icons.check }"></view>
      <view class="result__title">核销成功</view>
      <view class="result__name">{{ result.couponName }}</view>
      <view class="result__line">券类型：{{ couponTypeName(result.couponType) }}</view>
      <view class="result__line">核销时间：{{ fmtMinute(result.verifyTime) }}</view>
      <view class="result__code">{{ result.verifyCode }}</view>
      <button class="iip-btn result__btn" @click="resetVerify">继续核销</button>
    </view>

    <!-- 核销操作页 -->
    <template v-else>
      <view class="iip-card panel">
        <view class="label">核销码</view>
        <input
          class="panel__input"
          :value="verifyCode"
          maxlength="32"
          placeholder="请输入 16 位核销码"
          @input="onCodeInput"
        />
        <view class="panel__scan" @click="scanCode">
          <view class="panel__scan-icon" :style="{ backgroundImage: icons.scan }"></view>
          <text class="panel__scan-text">扫码核销</text>
        </view>
      </view>
      <button
        class="iip-btn submit"
        :class="{ 'is-disabled': !verifyCode }"
        :disabled="!verifyCode || verifying"
        @click="handleVerify"
      >
        {{ verifying ? '核销中' : '确认核销' }}
      </button>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { verifyCoupon } from '@/api/merchant.js'
import { fmtMinute, couponTypeName } from '@/common/format.js'
import icons from '@/common/icons.js'

const verifyCode = ref('')
const result = ref(null)
const verifying = ref(false)

/**
 * 输入核销码：统一转大写（核销码为字母数字组合）。
 *
 * @param {object} e input 事件
 */
function onCodeInput(e) {
  verifyCode.value = (e.detail.value || '').toUpperCase().trim()
}

/**
 * 调起相机扫码（条码/二维码内容作为核销码）。
 */
function scanCode() {
  uni.scanCode({
    success: (res) => {
      if (res.result) {
        verifyCode.value = String(res.result).toUpperCase().trim()
      }
    },
    fail: () => {}
  })
}

/**
 * 提交核销；失败原因由后端 msg 统一 toast。
 */
async function handleVerify() {
  if (!verifyCode.value || verifying.value) {
    return
  }
  verifying.value = true
  try {
    result.value = await verifyCoupon(verifyCode.value)
    verifyCode.value = ''
  } catch (e) {
    // 核销失败原因（券不存在/已使用/已过期/非本商户券）已由 request 封装 toast
  } finally {
    verifying.value = false
  }
}

function resetVerify() {
  result.value = null
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

.panel {
  padding: 32rpx;
}
.panel__input {
  height: 96rpx;
  border: 1rpx solid var(--iip-border);
  border-radius: 12rpx;
  padding: 0 28rpx;
  font-family: 'Courier New', Courier, monospace;
  font-size: 36rpx;
  letter-spacing: 4rpx;
  background-color: #ffffff;
  color: var(--iip-text);
}
.panel__scan {
  margin-top: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0 8rpx;
}
.panel__scan-icon {
  width: 96rpx;
  height: 96rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.panel__scan-text {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: var(--iip-primary-deep);
}

.submit {
  margin-top: 40rpx;
}

.result {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64rpx 40rpx;
}
.result__check {
  width: 96rpx;
  height: 96rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.result__title {
  margin-top: 20rpx;
  font-size: 34rpx;
  font-weight: 600;
  color: var(--iip-success);
}
.result__name {
  margin-top: 24rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.result__line {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: var(--iip-text-secondary);
}
.result__code {
  margin-top: 24rpx;
  font-family: 'Courier New', Courier, monospace;
  font-size: 36rpx;
  letter-spacing: 4rpx;
  color: var(--iip-text-muted);
}
.result__btn {
  width: 100%;
  margin-top: 48rpx;
}
</style>
