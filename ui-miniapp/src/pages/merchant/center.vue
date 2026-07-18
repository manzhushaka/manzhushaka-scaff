<template>
  <view class="page">
    <!-- 未登录引导 -->
    <view v-if="!userStore.isLogin" class="guest">
      <view class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <text>登录后进入商户中心</text>
      </view>
      <button class="iip-btn guest__btn" @click="goLogin">去登录</button>
    </view>

    <!-- 未入驻：引导申请 -->
    <view v-else-if="loaded && !merchant" class="iip-card guide">
      <view class="guide__icon" :style="{ backgroundImage: icons.shop }"></view>
      <view class="guide__title">入驻成为参与商户</view>
      <view class="guide__desc">入驻审核通过后，即可为用户核销兑换的优惠券</view>
      <button class="iip-btn guide__btn" @click="goApply">申请入驻</button>
    </view>

    <!-- 待审核 -->
    <view v-else-if="loaded && merchant && merchant.status === '2'" class="iip-card guide">
      <view class="guide__icon" :style="{ backgroundImage: icons.clock }"></view>
      <view class="guide__title">入驻申请审核中</view>
      <view class="guide__desc">「{{ merchant.merchantName }}」已提交，请等待管理员审核</view>
    </view>

    <!-- 已停用 -->
    <view v-else-if="loaded && merchant && merchant.status === '1'" class="iip-card guide">
      <view class="guide__icon" :style="{ backgroundImage: icons.clock }"></view>
      <view class="guide__title">商户已停用</view>
      <view class="guide__desc" v-if="merchant.auditRemark">原因：{{ merchant.auditRemark }}</view>
    </view>

    <!-- 正常：商户信息 + 核销入口 -->
    <template v-else-if="loaded && merchant && merchant.status === '0'">
      <view class="iip-card info">
        <view class="info__top">
          <text class="info__name">{{ merchant.merchantName }}</text>
          <text class="iip-tag iip-tag--approved">正常营业</text>
        </view>
        <view class="info__line" v-if="merchant.category">类别：{{ merchant.category }}</view>
        <view class="info__line" v-if="merchant.contactName">
          联系人：{{ merchant.contactName }}（{{ merchant.contactPhone }}）
        </view>
        <view class="info__line" v-if="merchant.address">地址：{{ merchant.address }}</view>
        <view class="info__no">商户编号：{{ merchant.merchantNo }}</view>
      </view>

      <button class="iip-btn verify-btn" @click="goVerify">
        <view class="verify-btn__icon" :style="{ backgroundImage: icons.scanWhite }"></view>
        <text>核销券码</text>
      </button>

      <view class="iip-card entry" @click="goRecords">
        <text class="entry__text">核销记录</text>
        <view class="entry__arrow" :style="{ backgroundImage: icons.arrowRight }"></view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getMerchantInfo } from '@/api/merchant.js'
import { redirectToLogin } from '@/common/request.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const merchant = ref(null)
const loaded = ref(false)

async function loadInfo() {
  if (!userStore.isLogin) {
    return
  }
  try {
    merchant.value = await getMerchantInfo()
  } catch (e) {
    merchant.value = null
  } finally {
    loaded.value = true
  }
}

function goLogin() {
  redirectToLogin()
}

function goApply() {
  uni.navigateTo({ url: '/pages/merchant/apply' })
}

function goVerify() {
  uni.navigateTo({ url: '/pages/merchant/verify' })
}

function goRecords() {
  uni.navigateTo({ url: '/pages/merchant/records' })
}

onShow(() => {
  loaded.value = false
  loadInfo()
})
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.guest {
  padding-top: 72rpx;
}
.guest__btn {
  margin: 0 64rpx;
}

.guide {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 72rpx 40rpx;
}
.guide__icon {
  width: 120rpx;
  height: 120rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.guide__title {
  margin-top: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.guide__desc {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: var(--iip-text-muted);
  text-align: center;
}
.guide__btn {
  width: 100%;
  margin-top: 40rpx;
}

.info__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.info__name {
  flex: 1;
  margin-right: 16rpx;
  font-size: 34rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.info__line {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: var(--iip-text-secondary);
}
.info__no {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid var(--iip-border);
  font-size: 24rpx;
  color: var(--iip-text-muted);
}

.verify-btn {
  margin-top: 32rpx;
  height: 104rpx;
  font-size: 34rpx;
  font-weight: 600;
}
.verify-btn__icon {
  width: 44rpx;
  height: 44rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  margin-right: 16rpx;
}

.entry {
  margin-top: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.entry__text {
  font-size: 28rpx;
  color: var(--iip-text);
}
.entry__arrow {
  width: 32rpx;
  height: 32rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
</style>
