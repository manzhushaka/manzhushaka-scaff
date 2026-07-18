<template>
  <view class="page">
    <!-- 用户卡：头像 / 昵称 / 实名状态 / 手机号遮罩 -->
    <view class="user iip-card" @click="onUserCardClick">
      <image
        v-if="userStore.isLogin && member && member.avatar"
        class="user__avatar"
        :src="member.avatar"
        mode="aspectFill"
      />
      <view v-else-if="userStore.isLogin && avatarChar" class="user__avatar user__avatar--name">
        {{ avatarChar }}
      </view>
      <view v-else class="user__avatar user__avatar--guest">
        <view class="user__avatar-icon" :style="{ backgroundImage: icons.user }"></view>
      </view>
      <view class="user__info">
        <view class="user__name-row">
          <text class="user__name">{{ userStore.isLogin ? nicknameText : '点击登录' }}</text>
          <text v-if="userStore.isLogin && member && member.phone" class="iip-chip iip-chip--g">已实名</text>
        </view>
        <view v-if="userStore.isLogin" class="user__sub">
          <text v-if="maskedPhone">{{ maskedPhone }} ｜ </text>实名与发票抬头一致方可积分
        </view>
        <view v-else class="user__sub">登录后查看积分、券包与发票</view>
      </view>
      <view v-if="!userStore.isLogin" class="iip-menu__chev"></view>
    </view>

    <!-- 统计卡：可用积分 / 累计积分 / 券包 / 已传发票 -->
    <view class="stats iip-card">
      <view class="stats__cell" @click="goPointsRecords">
        <view class="stats__value iip-num">{{ availableText }}</view>
        <view class="stats__label">可用积分</view>
      </view>
      <view class="stats__cell" @click="goPointsRecords">
        <view class="stats__value iip-num">{{ totalText }}</view>
        <view class="stats__label">累计积分</view>
      </view>
      <view class="stats__cell">
        <view class="stats__value iip-num">{{ couponText }}</view>
        <view class="stats__label">券包</view>
      </view>
      <view class="stats__cell">
        <view class="stats__value iip-num">{{ invoiceText }}</view>
        <view class="stats__label">已传发票</view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="iip-menu menu">
      <view
        v-for="item in menus"
        :key="item.title"
        class="iip-menu__row"
        :class="{ 'menu__row--dim': item.needLogin && !userStore.isLogin }"
        hover-class="iip-tap-bg"
        @click="onMenuClick(item)"
      >
        <view class="iip-menu__icon" :style="{ backgroundImage: item.icon }"></view>
        <text class="iip-menu__title">{{ item.title }}</text>
        <view class="iip-menu__chev"></view>
      </view>
    </view>

    <view class="foot">发票积分促消费平台 · 以票促消 以游惠民</view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { redirectToLogin } from '@/common/request.js'
import { getMyCoupons } from '@/api/coupon.js'
import { getInvoiceList } from '@/api/invoice.js'
import { fmtThousands } from '@/common/format.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const member = computed(() => userStore.member)

/** 券包数量（未使用券），未登录或拉取失败为 null，展示 '-' */
const couponCount = ref(null)
/** 已传发票数量，未登录或拉取失败为 null，展示 '-' */
const invoiceCount = ref(null)

/** 昵称（登录后缺昵称时给占位） */
const nicknameText = computed(() => {
  return member.value && member.value.nickname ? member.value.nickname : '未设置昵称'
})

/** 无头像时用昵称首字渲染金字渐变头像 */
const avatarChar = computed(() => {
  const name = member.value && member.value.nickname
  return name ? name.charAt(0) : ''
})

/** 手机号遮罩：前三后四，如 138****6688 */
const maskedPhone = computed(() => {
  const phone = member.value && member.value.phone
  if (!phone) {
    return ''
  }
  return phone.length >= 7 ? phone.slice(0, 3) + '****' + phone.slice(-4) : phone
})

const availableText = computed(() => (userStore.isLogin ? fmtThousands(userStore.availablePoints) : '-'))
const totalText = computed(() => (userStore.isLogin ? fmtThousands(userStore.totalPoints) : '-'))
const couponText = computed(() => (couponCount.value === null ? '-' : fmtThousands(couponCount.value)))
const invoiceText = computed(() => (invoiceCount.value === null ? '-' : fmtThousands(invoiceCount.value)))

const menus = [
  { title: '我的发票', icon: icons.invoice, url: '/pages/invoice/list', needLogin: true },
  { title: '我的券包', icon: icons.coupon, url: '/pages/coupon/mine', needLogin: true },
  { title: '上传发票', icon: icons.upload, url: '/pages/invoice/upload', needLogin: true },
  { title: '活动规则', icon: icons.stepInvoice, url: '/pages/activity/rules', needLogin: false },
  { title: '商户中心', icon: icons.shop, url: '/pages/merchant/center', needLogin: true }
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
 * 功能入口：需登录的入口未登录时置灰并引导登录；活动规则为公开页直接跳转。
 *
 * @param {object} item 菜单项
 */
function onMenuClick(item) {
  if (item.needLogin && !userStore.isLogin) {
    redirectToLogin()
    return
  }
  uni.navigateTo({ url: item.url })
}

/**
 * 拉取统计数字：登录后才请求，失败静默置 null（展示 '-'）。
 */
function loadStats() {
  getMyCoupons(0)
    .then((list) => {
      couponCount.value = list.length
    })
    .catch(() => {
      couponCount.value = null
    })
  getInvoiceList()
    .then((list) => {
      invoiceCount.value = list.length
    })
    .catch(() => {
      invoiceCount.value = null
    })
}

onShow(() => {
  if (userStore.isLogin) {
    userStore.fetchProfile().catch(() => {})
    loadStats()
  } else {
    couponCount.value = null
    invoiceCount.value = null
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
  gap: 24rpx;
  padding: 32rpx;
}
.user__avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  flex: 0 0 auto;
}
/* 金字渐变底 + 昵称首字（对齐原型 me-card avatar） */
.user__avatar--name {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0a13a, #c93418);
  color: #ffffff;
  font-size: 44rpx;
  font-weight: 800;
}
.user__avatar--guest {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--iip-color-chip-gray);
}
.user__avatar-icon {
  width: 56rpx;
  height: 56rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.user__info {
  flex: 1;
  min-width: 0;
}
.user__name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.user__name {
  font-size: var(--iip-fs-32);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.user__sub {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}

.stats {
  display: flex;
  margin-top: 24rpx;
  padding: 28rpx 0;
}
.stats__cell {
  flex: 1;
  min-width: 0;
  text-align: center;
  border-right: 1rpx solid var(--iip-color-line);
}
.stats__cell:last-child {
  border-right: none;
}
.stats__value {
  font-size: var(--iip-fs-36);
  font-weight: 900;
  color: var(--iip-color-ink);
}
.stats__label {
  margin-top: 6rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}

.menu {
  margin-top: 24rpx;
}
.menu__row--dim {
  opacity: 0.5;
}

.foot {
  padding: 40rpx 0 32rpx;
  text-align: center;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-faint);
}
</style>
