<template>
  <view class="page">
    <!-- 用户卡片 -->
    <view class="user iip-card" @click="onUserCardClick">
      <image v-if="member && member.avatar" class="user__avatar" :src="member.avatar" mode="aspectFill" />
      <view v-else class="user__avatar user__avatar--default">
        <view class="user__avatar-icon" :style="{ backgroundImage: icons.user }"></view>
      </view>
      <view class="user__info">
        <view class="user__name">{{ member && member.nickname ? member.nickname : '未登录' }}</view>
        <view class="user__platform">
          <text class="user__platform-tag">{{ platformName }}用户</text>
        </view>
      </view>
      <view v-if="!userStore.isLogin" class="user__login">点击登录 ›</view>
    </view>

    <!-- 积分卡片 -->
    <view class="points" @click="goPointsRecords">
      <view class="points__item">
        <view class="points__value">{{ userStore.isLogin ? userStore.availablePoints : '--' }}</view>
        <view class="points__label">可用积分</view>
      </view>
      <view class="points__divider"></view>
      <view class="points__item">
        <view class="points__value">{{ totalPointsText }}</view>
        <view class="points__label">累计获得</view>
      </view>
      <view class="points__entry">积分明细 ›</view>
    </view>

    <!-- 功能列表 -->
    <view class="iip-card menu">
      <view
        class="menu__item"
        :class="{ 'menu__item--disabled': !userStore.isLogin }"
        v-for="(item, index) in menus"
        :key="item.title"
        @click="onMenuClick(item)"
      >
        <view class="menu__icon" :style="{ backgroundImage: item.icon }"></view>
        <text class="menu__title">{{ item.title }}</text>
        <view class="menu__arrow" :style="{ backgroundImage: icons.arrowRight }"></view>
        <view v-if="index < menus.length - 1" class="menu__border"></view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getPlatformName } from '@/common/platform.js'
import { redirectToLogin } from '@/common/request.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const member = computed(() => userStore.member)
const platformName = computed(() => getPlatformName())

/**
 * 累计获得积分（后端 MemberProfileResult 返回 totalPoints，未登录时显示占位）。
 */
const totalPointsText = computed(() => {
  if (!userStore.isLogin) {
    return '--'
  }
  return userStore.totalPoints
})

const menus = [
  { title: '我的发票', icon: icons.invoice, url: '/pages/invoice/list' },
  { title: '我的券', icon: icons.coupon, url: '/pages/coupon/mine' },
  { title: '商户中心', icon: icons.shop, url: '/pages/merchant/center' },
  { title: '上传发票', icon: icons.upload, url: '/pages/invoice/upload' }
]

function onUserCardClick() {
  if (!userStore.isLogin) {
    redirectToLogin()
  }
}

function goPointsRecords() {
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  uni.navigateTo({ url: '/pages/points/records' })
}

/**
 * 功能入口：未登录置灰并引导登录。
 *
 * @param {object} item 菜单项
 */
function onMenuClick(item) {
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  uni.navigateTo({ url: item.url })
}

onShow(() => {
  if (userStore.isLogin) {
    userStore.fetchProfile().catch(() => {})
  }
})
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.user {
  display: flex;
  align-items: center;
}
.user__avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  flex-shrink: 0;
}
.user__avatar--default {
  background-color: var(--iip-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}
.user__avatar-icon {
  width: 64rpx;
  height: 64rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.user__info {
  flex: 1;
  margin-left: 24rpx;
  min-width: 0;
}
.user__name {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.user__platform {
  margin-top: 10rpx;
}
.user__platform-tag {
  display: inline-flex;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  background-color: var(--iip-primary-soft);
  color: var(--iip-primary-deep);
  font-size: 22rpx;
}
.user__login {
  font-size: 26rpx;
  color: var(--iip-primary-deep);
}

.points {
  margin-top: 24rpx;
  border-radius: 16rpx;
  background-color: var(--iip-primary);
  padding: 36rpx 32rpx;
  display: flex;
  align-items: center;
  position: relative;
}
.points__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.points__value {
  font-size: 48rpx;
  font-weight: 700;
  color: #ffffff;
}
.points__label {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}
.points__divider {
  width: 1rpx;
  height: 64rpx;
  background-color: rgba(255, 255, 255, 0.3);
}
.points__entry {
  position: absolute;
  right: 32rpx;
  bottom: 20rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}

.menu {
  margin-top: 24rpx;
  padding: 0 28rpx;
}
.menu__item {
  display: flex;
  align-items: center;
  height: 108rpx;
  position: relative;
}
.menu__item--disabled {
  opacity: 0.5;
}
.menu__icon {
  width: 48rpx;
  height: 48rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.menu__title {
  flex: 1;
  margin-left: 24rpx;
  font-size: 28rpx;
  color: var(--iip-text);
}
.menu__arrow {
  width: 32rpx;
  height: 32rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.menu__border {
  position: absolute;
  left: 72rpx;
  right: 0;
  bottom: 0;
  height: 1rpx;
  background-color: var(--iip-border);
}
</style>
