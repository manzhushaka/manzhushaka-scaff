<template>
  <view class="page" v-if="detail">
    <!-- 券大图与基本信息 -->
    <image
      v-if="detail.coverImage"
      class="cover"
      :src="resolveFileUrl(detail.coverImage)"
      mode="aspectFill"
    />
    <view v-else class="cover cover--plain">
      <view class="cover__icon" :style="{ backgroundImage: icons.ticket }"></view>
    </view>

    <view class="iip-card info">
      <view class="info__top">
        <text class="info__name">{{ detail.couponName }}</text>
        <text class="iip-tag iip-tag--pending">{{ couponTypeName(detail.couponType) }}</text>
      </view>
      <view class="info__tags">
        <text class="iip-tag info__tag--category">{{ couponCategoryName(detail.category) }}</text>
        <text v-if="sponsorText" class="iip-tag info__tag--sponsor">{{ sponsorText }}</text>
      </view>
      <view class="info__target" v-if="detail.targetName">适用对象：{{ detail.targetName }}</view>
      <view class="info__fullreduction" v-if="detail.couponType === 'full_reduction'">
        满 {{ detail.thresholdAmount }} 元减 {{ detail.discountAmount }} 元
      </view>
      <view class="info__cost">
        <text class="info__points">{{ detail.pointsCost }}</text>
        <text class="info__unit">积分</text>
        <text class="info__stock">{{ stockText }}</text>
      </view>
    </view>

    <!-- 商家信息（券绑定商户时展示） -->
    <view class="iip-card merchant" v-if="detail.merchantName">
      <view class="merchant__head">
        <image
          v-if="detail.merchantLogo"
          class="merchant__logo"
          :src="resolveFileUrl(detail.merchantLogo)"
          mode="aspectFill"
        />
        <text class="merchant__name">{{ detail.merchantName }}</text>
      </view>
      <view class="merchant__desc" v-if="detail.merchantDescription">{{ detail.merchantDescription }}</view>
      <view class="merchant__row" v-if="detail.merchantBusinessHours">
        <view class="merchant__icon" :style="{ backgroundImage: icons.clock }"></view>
        <text class="merchant__row-text">{{ detail.merchantBusinessHours }}</text>
      </view>
      <view class="merchant__row" v-if="detail.merchantAddress" @click="openMerchantLocation">
        <view class="merchant__icon" :style="{ backgroundImage: icons.location }"></view>
        <text class="merchant__row-text" :class="{ 'merchant__row-text--nav': hasMerchantLocation }">{{ detail.merchantAddress }}</text>
        <text v-if="hasMerchantLocation" class="merchant__nav">导航</text>
      </view>
      <view class="merchant__row" v-if="detail.merchantPhone" @click="callMerchant">
        <view class="merchant__icon" :style="{ backgroundImage: icons.phone }"></view>
        <text class="merchant__row-text merchant__row-text--phone">{{ detail.merchantPhone }}</text>
      </view>
    </view>

    <!-- 规则 -->
    <view class="iip-card rules">
      <view class="rules__item">
        <text class="rules__label">兑换窗口</text>
        <text class="rules__value">{{ exchangeWindowText }}</text>
      </view>
      <view class="rules__item">
        <text class="rules__label">有效期</text>
        <text class="rules__value">{{ validText }}</text>
      </view>
      <view class="rules__item">
        <text class="rules__label">限兑规则</text>
        <text class="rules__value">{{ limitText }}</text>
      </view>
    </view>

    <!-- 使用说明 -->
    <view class="iip-card desc" v-if="detail.useDesc">
      <view class="desc__title">使用说明</view>
      <view class="desc__content">{{ detail.useDesc }}</view>
    </view>

    <!-- 底部固定兑换栏 -->
    <view class="bar">
      <view class="bar__balance">
        <text class="bar__label">我的积分</text>
        <text class="bar__value">{{ userStore.availablePoints }}</text>
      </view>
      <button
        class="iip-btn bar__btn"
        :class="{ 'is-disabled': exchangeDisabled }"
        :disabled="exchangeDisabled || exchanging"
        @click="handleExchange"
      >
        {{ exchangeButtonText }}
      </button>
    </view>

    <!-- 兑换成功弹层：展示核销码 -->
    <view class="success-mask" v-if="exchangeResult" @click="closeSuccess">
      <view class="success" @click.stop>
        <view class="success__check" :style="{ backgroundImage: icons.check }"></view>
        <view class="success__title">兑换成功</view>
        <view class="success__name">{{ exchangeResult.couponName }}</view>
        <view class="success__code-label">核销码</view>
        <view class="success__code">{{ formatVerifyCode(exchangeResult.verifyCode) }}</view>
        <view class="success__valid" v-if="exchangeResult.validEndTime">
          有效期至 {{ fmtMinute(exchangeResult.validEndTime) }}
        </view>
        <button class="iip-btn success__btn" @click="goMine">查看我的券</button>
        <view class="success__close" @click="closeSuccess">继续逛逛</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getCouponDetail, exchangeCoupon } from '@/api/coupon.js'
import { resolveFileUrl } from '@/common/config.js'
import { fmtDate, fmtMinute, couponTypeName, couponCategoryName, couponSponsorText } from '@/common/format.js'
import { redirectToLogin } from '@/common/request.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const couponId = ref(null)
const detail = ref(null)
const exchangeResult = ref(null)
const exchanging = ref(false)

/** 赞助方文案（platform 平台券为空串，不展示） */
const sponsorText = computed(() => {
  if (!detail.value) {
    return ''
  }
  return couponSponsorText(detail.value.sponsorType, detail.value.sponsorName)
})

onLoad((options) => {
  couponId.value = options.id
  loadDetail()
})

async function loadDetail() {
  try {
    detail.value = await getCouponDetail(couponId.value)
  } catch (e) {
    // 错误提示已由 request 封装统一处理
  }
  if (userStore.isLogin) {
    userStore.fetchProfile().catch(() => {})
  }
}

/** 库存文案（-1 不限） */
const stockText = computed(() => {
  if (!detail.value) {
    return ''
  }
  if (detail.value.remainStock === -1) {
    return '库存充足'
  }
  if (detail.value.remainStock === 0) {
    return '已售罄'
  }
  return '剩余 ' + detail.value.remainStock
})

/** 兑换窗口文案 */
const exchangeWindowText = computed(() => {
  if (!detail.value) {
    return ''
  }
  const start = detail.value.exchangeStartTime
  const end = detail.value.exchangeEndTime
  if (!start && !end) {
    return '不限时间'
  }
  return fmtMinute(start) + ' 至 ' + fmtMinute(end)
})

/** 有效期规则文案：fixed 显示起止，days 显示领取后 N 天有效 */
const validText = computed(() => {
  if (!detail.value) {
    return ''
  }
  if (detail.value.validType === 'days') {
    return '领取后 ' + detail.value.validDays + ' 天内有效'
  }
  return fmtDate(detail.value.validStartTime) + ' 至 ' + fmtDate(detail.value.validEndTime)
})

/** 限兑文案（-1 不限，含当前用户已兑数量） */
const limitText = computed(() => {
  if (!detail.value) {
    return ''
  }
  if (detail.value.perMemberLimit === -1) {
    return '不限兑换次数'
  }
  return '每人限兑 ' + detail.value.perMemberLimit + ' 张，已兑 ' + (detail.value.exchangedCount || 0) + ' 张'
})

/** 积分是否不足 */
const pointsNotEnough = computed(() => {
  return detail.value && userStore.availablePoints < detail.value.pointsCost
})

/** 是否已达限兑上限 */
const overLimit = computed(() => {
  if (!detail.value || detail.value.perMemberLimit === -1) {
    return false
  }
  return (detail.value.exchangedCount || 0) >= detail.value.perMemberLimit
})

const exchangeDisabled = computed(() => {
  if (!detail.value) {
    return true
  }
  return detail.value.remainStock === 0 || pointsNotEnough.value || overLimit.value
})

const exchangeButtonText = computed(() => {
  if (!detail.value) {
    return '加载中'
  }
  if (detail.value.remainStock === 0) {
    return '已售罄'
  }
  if (overLimit.value) {
    return '已达限兑上限'
  }
  if (pointsNotEnough.value) {
    return '积分不足'
  }
  return '立即兑换'
})

/**
 * 二次确认后兑换；成功弹出核销码弹层。
 */
function handleExchange() {
  if (exchangeDisabled.value || exchanging.value) {
    return
  }
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  uni.showModal({
    title: '确认兑换',
    content: '消耗 ' + detail.value.pointsCost + ' 积分兑换「' + detail.value.couponName + '」？',
    confirmText: '确认兑换',
    confirmColor: '#ff6a2a',
    success: async (res) => {
      if (!res.confirm) {
        return
      }
      exchanging.value = true
      try {
        exchangeResult.value = await exchangeCoupon(detail.value.couponId)
        userStore.fetchProfile().catch(() => {})
      } catch (e) {
        // 错误提示已由 request 封装统一 toast（如库存不足、超出限兑）
      } finally {
        exchanging.value = false
      }
    }
  })
}

/** 商家经纬度是否齐全（齐全时地址行可点击唤起地图导航） */
const hasMerchantLocation = computed(() => {
  if (!detail.value) {
    return false
  }
  return detail.value.merchantLongitude != null && detail.value.merchantLatitude != null
})

/** 拨打商家电话（用户取消拨号属正常操作，无需提示） */
function callMerchant() {
  if (!detail.value || !detail.value.merchantPhone) {
    return
  }
  uni.makePhoneCall({ phoneNumber: detail.value.merchantPhone })
}

/**
 * 唤起地图查看商家位置并导航（仅在经纬度齐全时可触发）。
 */
function openMerchantLocation() {
  if (!hasMerchantLocation.value) {
    return
  }
  uni.openLocation({
    latitude: Number(detail.value.merchantLatitude),
    longitude: Number(detail.value.merchantLongitude),
    name: detail.value.merchantName,
    address: detail.value.merchantAddress
  })
}

/** 核销码分组展示（每 4 位一组） */
function formatVerifyCode(code) {
  if (!code) {
    return ''
  }
  return String(code).replace(/(.{4})/g, '$1 ').trim()
}

function goMine() {
  exchangeResult.value = null
  uni.redirectTo({ url: '/pages/coupon/mine' })
}

function closeSuccess() {
  exchangeResult.value = null
  loadDetail()
}
</script>

<style scoped>
.page {
  padding-bottom: 160rpx;
}

.cover {
  width: 100%;
  height: 400rpx;
}
.cover--plain {
  background-color: var(--iip-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover__icon {
  width: 160rpx;
  height: 160rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

.info {
  margin: 24rpx;
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
.info__tags {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
}
.info__tags .iip-tag + .iip-tag {
  margin-left: 12rpx;
}
.info__tag--category {
  background-color: var(--iip-primary-soft);
  color: var(--iip-primary-deep);
}
.info__tag--sponsor {
  background-color: var(--iip-pending-soft);
  color: var(--iip-pending);
}
.info__target {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.info__fullreduction {
  display: inline-flex;
  margin-top: 16rpx;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  background-color: var(--iip-primary-soft);
  color: var(--iip-primary-deep);
  font-size: 24rpx;
}
.info__cost {
  margin-top: 20rpx;
  display: flex;
  align-items: baseline;
}
.info__points {
  font-size: 52rpx;
  font-weight: 700;
  color: var(--iip-primary-deep);
}
.info__unit {
  margin-left: 8rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.info__stock {
  margin-left: auto;
  font-size: 24rpx;
  color: var(--iip-text-secondary);
}

.rules {
  margin: 0 24rpx 24rpx;
}
.rules__item {
  display: flex;
  padding: 18rpx 0;
}
.rules__item + .rules__item {
  border-top: 1rpx solid var(--iip-border);
}
.rules__label {
  width: 160rpx;
  flex-shrink: 0;
  color: var(--iip-text-muted);
  font-size: 26rpx;
}
.rules__value {
  flex: 1;
  color: var(--iip-text);
  font-size: 26rpx;
}

.desc {
  margin: 0 24rpx 24rpx;
}
.desc__title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--iip-text);
  margin-bottom: 12rpx;
}
.desc__content {
  font-size: 26rpx;
  color: var(--iip-text-secondary);
  line-height: 1.7;
}

/* 商家信息卡片 */
.merchant {
  margin: 0 24rpx 24rpx;
}
.merchant__head {
  display: flex;
  align-items: center;
}
.merchant__logo {
  width: 72rpx;
  height: 72rpx;
  flex-shrink: 0;
  margin-right: 16rpx;
  border-radius: 12rpx;
  background-color: var(--iip-primary-soft);
}
.merchant__name {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.merchant__desc {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: var(--iip-text-secondary);
  line-height: 1.7;
}
.merchant__row {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
}
.merchant__icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
  margin-right: 12rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.merchant__row-text {
  flex: 1;
  font-size: 26rpx;
  color: var(--iip-text-secondary);
}
.merchant__row-text--phone {
  color: var(--iip-primary-deep);
}
.merchant__row-text--nav {
  color: var(--iip-primary-deep);
}
.merchant__nav {
  flex-shrink: 0;
  margin-left: 12rpx;
  font-size: 24rpx;
  color: var(--iip-primary-deep);
}

/* 底部固定兑换栏 */
.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background-color: var(--iip-panel);
  border-top: 1rpx solid var(--iip-border);
}
.bar__balance {
  display: flex;
  flex-direction: column;
  margin-right: 24rpx;
}
.bar__label {
  font-size: 22rpx;
  color: var(--iip-text-muted);
}
.bar__value {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--iip-text);
}
.bar__btn {
  flex: 1;
}

/* 兑换成功弹层 */
.success-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(58, 47, 40, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
}
.success {
  width: 600rpx;
  background-color: var(--iip-panel);
  border-radius: 16rpx;
  padding: 48rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.success__check {
  width: 88rpx;
  height: 88rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.success__title {
  margin-top: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.success__name {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: var(--iip-text-secondary);
}
.success__code-label {
  margin-top: 32rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.success__code {
  margin-top: 12rpx;
  font-family: 'Courier New', Courier, monospace;
  font-size: 44rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  color: var(--iip-text);
}
.success__valid {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.success__btn {
  width: 100%;
  margin-top: 40rpx;
}
.success__close {
  margin-top: 24rpx;
  font-size: 26rpx;
  color: var(--iip-text-muted);
}
</style>
