<template>
  <view class="page">
    <!-- 扫码取景框：暗底 + 金色四角 + 红色激光线，点击调起扫码 -->
    <view class="finder" @click="scanCode">
      <view class="finder__corner finder__corner--tl"></view>
      <view class="finder__corner finder__corner--tr"></view>
      <view class="finder__corner finder__corner--bl"></view>
      <view class="finder__corner finder__corner--br"></view>
      <view class="finder__laser"></view>
    </view>
    <view class="scan-tip">对准用户券码 / 门票二维码，自动识别</view>

    <!-- 手动输入核销码 -->
    <view class="iip-card panel">
      <view class="panel__label">核销码</view>
      <input
        class="panel__input"
        :value="verifyCode"
        maxlength="16"
        placeholder="请输入 16 位核销码"
        @input="onCodeInput"
      />
    </view>

    <button
      class="iip-btn submit"
      :class="{ 'is-disabled': !verifyCode }"
      :disabled="!verifyCode || verifying"
      @click="openConfirm"
    >
      {{ verifying ? '核销中' : '确认核销' }}
    </button>

    <!-- 核销二次确认 bottom sheet -->
    <view v-if="confirming" class="mask" @click="closeConfirm">
      <view class="sheet" @click.stop>
        <view class="sheet__title">确认核销</view>
        <view class="sheet__code">{{ maskedCode }}</view>
        <view class="sheet__desc">确认核销该券？核销后不可撤销</view>
        <view class="sheet__actions">
          <button class="sheet__btn sheet__btn--plain" :disabled="verifying" @click="closeConfirm">取消</button>
          <button class="sheet__btn sheet__btn--primary" :disabled="verifying" @click="handleVerify">
            {{ verifying ? '核销中' : '确认核销' }}
          </button>
        </view>
      </view>
    </view>

    <!-- 核销成功结果 center sheet -->
    <view v-if="result" class="mask mask--center">
      <view class="sheet sheet--center">
        <view class="ok-ic">
          <view class="ok-ic__icon" :style="{ backgroundImage: icons.check }"></view>
        </view>
        <view class="sheet__title">核销成功</view>
        <view class="result__name">{{ result.couponName }}</view>
        <view class="result__line">券类型：{{ couponTypeName(result.couponType) }}</view>
        <view class="result__line">核销时间：{{ fmtSecond(result.verifyTime) }}</view>
        <view class="result__code">{{ result.verifyCode }}</view>
        <button class="iip-btn result__btn" @click="resetVerify">继续核销</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { verifyCoupon } from '@/api/merchant.js'
import { couponTypeName } from '@/common/format.js'
import icons from '@/common/icons.js'

const verifyCode = ref('')
const result = ref(null)
const verifying = ref(false)
const confirming = ref(false)

/** 确认弹层中的脱敏券码：保留前 4 位与后 2 位，中间以 * 代替 */
const maskedCode = computed(() => {
  const code = verifyCode.value
  if (code.length <= 6) {
    return code
  }
  return code.slice(0, 4) + '*'.repeat(code.length - 6) + code.slice(-2)
})

/**
 * 输入核销码：统一转大写并去空格（核销码为字母数字组合）。
 *
 * @param {object} e input 事件
 */
function onCodeInput(e) {
  verifyCode.value = (e.detail.value || '').toUpperCase().trim()
}

/**
 * 点击取景框调起相机扫码；用户取消或 H5 不支持时在 fail 中提示改用手输。
 */
function scanCode() {
  uni.scanCode({
    success: (res) => {
      if (res.result) {
        verifyCode.value = String(res.result).toUpperCase().trim()
      }
    },
    fail: () => {
      uni.showToast({ title: '扫码取消或失败，可手动输入', icon: 'none' })
    }
  })
}

/**
 * 打开二次确认弹层（无码或核销中不响应）。
 */
function openConfirm() {
  if (!verifyCode.value || verifying.value) {
    return
  }
  confirming.value = true
}

/**
 * 关闭二次确认弹层（核销请求进行中禁止关闭，避免重复提交）。
 */
function closeConfirm() {
  if (verifying.value) {
    return
  }
  confirming.value = false
}

/**
 * 二次确认后提交核销；失败原因由 request 封装统一 toast，关闭弹层停留在操作态。
 */
async function handleVerify() {
  if (verifying.value) {
    return
  }
  verifying.value = true
  try {
    result.value = await verifyCoupon(verifyCode.value)
    verifyCode.value = ''
    confirming.value = false
  } catch (e) {
    // 核销失败原因（券不存在/已使用/已过期/非本商户券）已由 request 封装 toast
    confirming.value = false
  } finally {
    verifying.value = false
  }
}

/**
 * 核销时间展示到秒（后端已序列化为 yyyy-MM-dd HH:mm:ss）。
 *
 * @param {string} value 后端日期时间字符串
 * @returns {string} yyyy-MM-dd HH:mm:ss 或空串
 */
function fmtSecond(value) {
  return value ? String(value).slice(0, 19) : ''
}

/**
 * 关闭结果弹层，回到操作态继续核销下一券。
 */
function resetVerify() {
  result.value = null
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}

/* 扫码取景框：460rpx 见方暗底，四角金色 L 边框，红色激光线上下扫动 */
.finder {
  position: relative;
  width: 460rpx;
  height: 460rpx;
  margin: 32rpx auto 0;
  overflow: hidden;
  border-radius: var(--iip-radius-32);
  background-color: var(--iip-color-ink);
}
/* 暗色横纹纹理（对齐原型 .finder::before） */
.finder::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(
    0deg,
    rgba(255, 255, 255, 0.03) 0,
    rgba(255, 255, 255, 0.03) 4rpx,
    transparent 4rpx,
    transparent 12rpx
  );
}
.finder__corner {
  position: absolute;
  width: 68rpx;
  height: 68rpx;
  border: 6rpx solid var(--iip-color-gold);
}
.finder__corner--tl {
  top: 36rpx;
  left: 36rpx;
  border-right: none;
  border-bottom: none;
  border-radius: 16rpx 0 0 0;
}
.finder__corner--tr {
  top: 36rpx;
  right: 36rpx;
  border-left: none;
  border-bottom: none;
  border-radius: 0 16rpx 0 0;
}
.finder__corner--bl {
  bottom: 36rpx;
  left: 36rpx;
  border-right: none;
  border-top: none;
  border-radius: 0 0 0 16rpx;
}
.finder__corner--br {
  bottom: 36rpx;
  right: 36rpx;
  border-left: none;
  border-top: none;
  border-radius: 0 0 16rpx 0;
}
.finder__laser {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  top: 22%;
  height: 4rpx;
  background-color: var(--iip-color-primary);
  box-shadow: 0 0 24rpx var(--iip-color-primary);
  animation: verify-scan 2.2s ease-in-out infinite;
}
@keyframes verify-scan {
  0%,
  100% {
    top: 22%;
  }
  50% {
    top: 74%;
  }
}
/* 减弱动态偏好时激光线静止于中部 */
@media (prefers-reduced-motion: reduce) {
  .finder__laser {
    animation: none;
    top: 48%;
  }
}

.scan-tip {
  margin: 32rpx 0;
  text-align: center;
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}

/* 手输区 */
.panel {
  padding: 32rpx;
}
.panel__label {
  margin-bottom: 16rpx;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.panel__input {
  height: 96rpx;
  padding: 0 28rpx;
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-16);
  background-color: var(--iip-color-surface);
  font-family: 'Courier New', Courier, monospace;
  font-size: var(--iip-fs-36);
  letter-spacing: 4rpx;
  color: var(--iip-color-ink);
}

.submit {
  margin-top: 40rpx;
}

/* 遮罩：底部 sheet / 居中 sheet 共用 */
.mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 99;
  display: flex;
  align-items: flex-end;
  background-color: rgba(34, 30, 24, 0.5);
}
.mask--center {
  align-items: center;
  justify-content: center;
}

/* bottom sheet（二次确认） */
.sheet {
  width: 100%;
  padding: 44rpx 40rpx calc(44rpx + env(safe-area-inset-bottom));
  background-color: var(--iip-color-surface);
  border-radius: 44rpx 44rpx 0 0;
  animation: verify-sheet-up 0.25s ease 1;
}
@keyframes verify-sheet-up {
  0% {
    transform: translateY(80rpx);
    opacity: 0.4;
  }
  100% {
    transform: none;
    opacity: 1;
  }
}
.sheet__title {
  text-align: center;
  font-size: var(--iip-fs-32);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.sheet__code {
  margin-top: 24rpx;
  text-align: center;
  font-family: 'Courier New', Courier, monospace;
  font-size: var(--iip-fs-36);
  letter-spacing: 4rpx;
  color: var(--iip-color-ink);
}
.sheet__desc {
  margin-top: 16rpx;
  text-align: center;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.sheet__actions {
  display: flex;
  gap: 24rpx;
  margin-top: 40rpx;
}
.sheet__btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22rpx 0;
  font-size: var(--iip-fs-28);
  font-weight: 700;
  line-height: 1.4;
  border-radius: var(--iip-radius-pill);
}
.sheet__btn::after {
  border: none;
}
.sheet__btn--plain {
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  color: var(--iip-color-ink);
}
.sheet__btn--primary {
  background-color: var(--iip-color-primary);
  color: #ffffff;
}

/* center sheet（核销成功结果） */
.sheet--center {
  width: 620rpx;
  border-radius: 44rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: verify-sheet-pop 0.22s ease 1;
}
@keyframes verify-sheet-pop {
  0% {
    transform: scale(0.9);
    opacity: 0;
  }
  100% {
    transform: none;
    opacity: 1;
  }
}
.ok-ic {
  width: 116rpx;
  height: 116rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: var(--iip-color-chip-green);
}
.ok-ic__icon {
  width: 56rpx;
  height: 56rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.result__name {
  margin-top: 16rpx;
  font-size: var(--iip-fs-30);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.result__line {
  margin-top: 8rpx;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.result__code {
  margin-top: 24rpx;
  font-family: 'Courier New', Courier, monospace;
  font-size: var(--iip-fs-32);
  letter-spacing: 4rpx;
  color: var(--iip-color-text-secondary);
}
.result__btn {
  width: 100%;
  margin-top: 40rpx;
}
</style>
