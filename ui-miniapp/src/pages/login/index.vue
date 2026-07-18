<template>
  <view class="page">
    <!-- 品牌区：深色墨卡 + 金色点缀 -->
    <view class="brand iip-dark-card">
      <view class="brand__eyebrow">
        <view class="brand__dot"></view>
        <text class="iip-gold">发票积分促消费平台</text>
      </view>
      <view class="brand__name">发票惠游</view>
      <view class="brand__slogan">以票促消 <text class="iip-gold">·</text> 以游惠民</view>
    </view>

    <!-- 登录卡：平台一键登录（按编译端 / 调试平台条件渲染） -->
    <view class="panel iip-card">
      <button class="iip-btn login-btn" :disabled="submitting" @click="handlePlatformLogin">
        <view class="login-btn__icon" :style="{ backgroundImage: platformIcon }"></view>
        <text>{{ platformName }}一键登录</text>
      </button>

      <!-- 开发模式：mock code 登录（灰小字调试区，正式构建不出现） -->
      <view class="mock" v-if="config.mockLoginEnabled">
        <view class="mock__divider">
          <view class="mock__line"></view>
          <text class="mock__text">开发调试</text>
          <view class="mock__line"></view>
        </view>
        <input
          class="mock__input"
          v-model="mockCode"
          placeholder="输入 mock code（任意字符串）"
          placeholder-class="mock__placeholder"
        />
        <view class="mock__row">
          <view class="mock__btn" :class="{ 'mock__btn--disabled': submitting }" @click="handleMockLogin">
            mock 登录
          </view>
          <!-- 平台调试切换：云闪付复用支付宝编译产物，通过适配层切换 platform -->
          <view class="chips">
            <view
              v-for="item in platformOptions"
              :key="item.key"
              class="chips__item"
              :class="{ 'chips__item--active': debugPlatform === item.key }"
              @click="switchPlatform(item.key)"
            >
              {{ item.name }}
            </view>
          </view>
        </view>
        <view class="chips__tip">调试用途：切换登录请求携带的 platform 标识</view>
      </view>

      <!-- 用户协议：整行可点 -->
      <view class="agreement" @click="agreed = !agreed">
        <view class="agreement__box" :class="{ 'agreement__box--checked': agreed }">
          <view v-if="agreed" class="agreement__check" :style="{ backgroundImage: icons.checkWhite }"></view>
        </view>
        <text class="agreement__text">我已阅读并同意《用户协议》与《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/store/user.js'
import { login } from '@/api/auth.js'
import config from '@/common/config.js'
import {
  getPlatform,
  getPlatformName,
  doLogin,
  setPlatformOverride,
  clearPlatformOverride
} from '@/common/platform.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const agreed = ref(false)
const mockCode = ref('')
const submitting = ref(false)

/** 编译期默认平台（未做调试覆盖时的平台） */
const compilePlatform = getPlatform()
/** 当前生效的登录平台（可被调试切换覆盖） */
const debugPlatform = ref(compilePlatform)

const platformOptions = [
  { key: 'wechat', name: '微信' },
  { key: 'alipay', name: '支付宝' },
  { key: 'unionpay', name: '云闪付' }
]

const platformName = computed(() => getPlatformName(debugPlatform.value))

const platformIcon = computed(() => {
  if (debugPlatform.value === 'alipay') {
    return icons.alipay
  }
  if (debugPlatform.value === 'unionpay') {
    return icons.unionpay
  }
  return icons.wechat
})

/**
 * 切换调试平台：云闪付复用支付宝编译产物，通过适配层覆盖 platform='unionpay'。
 *
 * @param {string} platform 目标平台标识
 */
function switchPlatform(platform) {
  debugPlatform.value = platform
  if (platform === compilePlatform) {
    clearPlatformOverride()
  } else {
    setPlatformOverride(platform)
  }
}

/**
 * 平台一键登录：uni.login 取 code → 后端登录 → 写 store → 返回上一页。
 * 未配置真实 appid 时 uni.login 可能失败或 H5 无此能力，回退到 mock code。
 */
async function handlePlatformLogin() {
  if (!checkAgreement()) {
    return
  }
  let code = ''
  try {
    const res = await doLogin()
    code = res.code
  } catch (e) {
    // 未配置真实平台密钥时 uni.login 会失败，提示后回退 mock code
    if (!mockCode.value) {
      uni.showToast({ title: '平台登录不可用，请使用 mock code', icon: 'none' })
      return
    }
  }
  await doLoginRequest(code || mockCode.value)
}

/**
 * mock code 登录（开发模式）。
 */
async function handleMockLogin() {
  if (!checkAgreement()) {
    return
  }
  if (!mockCode.value.trim()) {
    uni.showToast({ title: '请输入 mock code', icon: 'none' })
    return
  }
  await doLoginRequest(mockCode.value.trim())
}

/**
 * 校验协议勾选。
 *
 * @returns {boolean} 是否已勾选
 */
function checkAgreement() {
  if (!agreed.value) {
    uni.showToast({ title: '请先勾选并同意用户协议', icon: 'none' })
    return false
  }
  return true
}

/**
 * 调用后端登录接口并写入登录态。
 *
 * @param {string} code 登录 code（真实 code 或 mock code）
 */
async function doLoginRequest(code) {
  if (!code || submitting.value) {
    return
  }
  submitting.value = true
  try {
    // 登录响应体顶层为 {code, msg, token, member}（AjaxResult.put）
    const res = await login({ platform: debugPlatform.value, code })
    userStore.setLogin(res.token, res.member)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack({
        fail: () => {
          uni.switchTab({ url: '/pages/index/index' })
        }
      })
    }, 600)
  } catch (e) {
    // 错误提示已由 request 封装统一 toast
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 48rpx 32rpx;
}

.brand {
  padding: 56rpx 40rpx;
}
.brand__eyebrow {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: var(--iip-fs-22);
  letter-spacing: 4rpx;
}
.brand__dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: var(--iip-color-primary);
}
.brand__name {
  margin-top: 16rpx;
  font-size: var(--iip-fs-56);
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 4rpx;
}
.brand__slogan {
  margin-top: 12rpx;
  font-size: var(--iip-fs-26);
  color: rgba(255, 255, 255, 0.75);
  letter-spacing: 2rpx;
}

.panel {
  margin-top: 24rpx;
  padding: 40rpx 32rpx;
}

.login-btn {
  font-weight: 700;
}
.login-btn__icon {
  width: 40rpx;
  height: 40rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  margin-right: 16rpx;
}

/* 开发调试区：灰小字收敛，不抢主按钮视觉 */
.mock {
  margin-top: 40rpx;
}
.mock__divider {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}
.mock__line {
  flex: 1;
  height: 1rpx;
  background-color: var(--iip-color-line);
}
.mock__text {
  padding: 0 24rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-faint);
}
.mock__input {
  height: 72rpx;
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-16);
  padding: 0 24rpx;
  font-size: var(--iip-fs-24);
  background-color: var(--iip-color-bg);
  color: var(--iip-color-ink);
}
.mock__placeholder {
  color: var(--iip-color-text-faint);
}
.mock__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20rpx;
}
.mock__btn {
  padding: 12rpx 32rpx;
  border-radius: var(--iip-radius-pill);
  background-color: var(--iip-color-chip-gray);
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}
.mock__btn--disabled {
  opacity: 0.5;
}
.chips {
  display: flex;
}
.chips__item {
  padding: 8rpx 24rpx;
  margin-left: 12rpx;
  border-radius: var(--iip-radius-pill);
  border: 1rpx solid var(--iip-color-line);
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
  background-color: var(--iip-color-surface);
}
.chips__item--active {
  border-color: var(--iip-color-ink);
  color: #ffffff;
  background-color: var(--iip-color-ink);
}
.chips__tip {
  margin-top: 14rpx;
  text-align: center;
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-faint);
}

.agreement {
  display: flex;
  align-items: center;
  margin-top: 40rpx;
  padding: 8rpx 0;
}
.agreement__box {
  width: 40rpx;
  height: 40rpx;
  border-radius: 10rpx;
  border: 2rpx solid var(--iip-color-line);
  background-color: var(--iip-color-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.agreement__box--checked {
  border-color: var(--iip-color-primary);
  background-color: var(--iip-color-primary);
}
.agreement__check {
  width: 26rpx;
  height: 26rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.agreement__text {
  margin-left: 16rpx;
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}
</style>
