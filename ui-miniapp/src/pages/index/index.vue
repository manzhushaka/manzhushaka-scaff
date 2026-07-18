<template>
  <view class="page">
    <!-- 当前活动 banner -->
    <view class="banner" v-if="activity">
      <image
        v-if="activity.coverImage"
        class="banner__image"
        :src="resolveFileUrl(activity.coverImage)"
        mode="aspectFill"
      />
      <view v-else class="banner__image banner__image--plain">
        <view class="banner__logo">发票积分</view>
      </view>
      <view class="banner__mask">
        <view class="banner__name">{{ activity.activityName }}</view>
        <view class="banner__time">
          {{ fmtDate(activity.startTime) }} 至 {{ fmtDate(activity.endTime) }}
        </view>
        <view class="banner__ratio" v-if="activity.pointsRatio">
          发票面额 × {{ ratioText }} = 获得积分
        </view>
      </view>
    </view>
    <view class="banner banner--empty iip-card" v-else>
      <view class="banner__empty-text">当前暂无进行中的活动</view>
    </view>

    <!-- 进行中的活动列表（多活动并行展示） -->
    <view class="activities" v-if="activities.length">
      <view class="activities__header">
        <text class="activities__title">进行中的活动</text>
      </view>
      <view
        class="activity-item iip-card"
        v-for="item in activities"
        :key="item.activityId || item.activityName"
      >
        <view class="activity-item__top">
          <text class="activity-item__name">{{ item.activityName }}</text>
          <text class="activity-item__region">{{ regionText(item) }}</text>
        </view>
        <view class="activity-item__time">
          {{ fmtDate(item.startTime) }} 至 {{ fmtDate(item.endTime) }}
        </view>
        <view
          class="activity-item__ratio"
          :class="{ 'activity-item__ratio--boost': isBoostedRatio(item) }"
        >
          1 元 = {{ ratioOf(item) }} 分
        </view>
      </view>
    </view>

    <!-- 参与流程四步 -->
    <view class="steps iip-card">
      <view class="steps__item" v-for="(step, index) in steps" :key="index">
        <view class="steps__icon" :style="{ backgroundImage: step.icon }"></view>
        <text class="steps__label">{{ step.label }}</text>
        <text v-if="index < steps.length - 1" class="steps__arrow">›</text>
      </view>
    </view>

    <!-- 我的积分卡片 -->
    <view class="points iip-card" @click="onPointsCardClick">
      <view class="points__info">
        <view class="points__label">我的可用积分</view>
        <view class="points__value" v-if="userStore.isLogin">{{ userStore.availablePoints }}</view>
        <view class="points__value points__value--login" v-else>去登录</view>
      </view>
      <view class="points__side">
        <view class="points__detail">积分明细 ›</view>
      </view>
    </view>

    <!-- 优选券推荐 -->
    <view class="recommend">
      <view class="recommend__header">
        <text class="recommend__title">优选券推荐</text>
        <text class="recommend__more" @click="goMall">更多 ›</text>
      </view>
      <scroll-view v-if="userStore.isLogin && coupons.length" scroll-x class="recommend__scroll">
        <view
          class="coupon-card"
          v-for="item in coupons"
          :key="item.couponId"
          @click="goCouponDetail(item.couponId)"
        >
          <image
            v-if="item.coverImage"
            class="coupon-card__cover"
            :src="resolveFileUrl(item.coverImage)"
            mode="aspectFill"
          />
          <view v-else class="coupon-card__cover coupon-card__cover--plain">
            <view class="coupon-card__cover-icon" :style="{ backgroundImage: icons.ticket }"></view>
          </view>
          <view class="coupon-card__name">{{ item.couponName }}</view>
          <view class="coupon-card__cost">
            <text class="coupon-card__points">{{ item.pointsCost }}</text>
            <text class="coupon-card__unit">积分</text>
          </view>
        </view>
      </scroll-view>
      <view v-else class="recommend__guest iip-card">
        <view class="recommend__guest-text">
          {{ userStore.isLogin ? '暂无推荐优惠券' : '登录后查看优选优惠券' }}
        </view>
        <button v-if="!userStore.isLogin" class="iip-btn iip-btn--small" @click="goLogin">去登录</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getCurrentActivity, getActivityList } from '@/api/activity.js'
import { getMallCoupons } from '@/api/coupon.js'
import { resolveFileUrl } from '@/common/config.js'
import { fmtDate } from '@/common/format.js'
import { redirectToLogin } from '@/common/request.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const activity = ref(null)
const activities = ref([])
const coupons = ref([])

/** 积分比例展示（BigDecimal 序列化如 1.00，去尾零后展示） */
const ratioText = computed(() => {
  if (!activity.value || activity.value.pointsRatio == null) {
    return '1'
  }
  return String(parseFloat(activity.value.pointsRatio))
})

/**
 * 单个活动的积分比例展示（去尾零）。
 *
 * @param {object} item 活动对象
 * @returns {string} 比例文本
 */
function ratioOf(item) {
  if (!item || item.pointsRatio == null) {
    return '1'
  }
  return String(parseFloat(item.pointsRatio))
}

/**
 * 积分比例是否非 1（非 1 时醒目展示）。
 *
 * @param {object} item 活动对象
 * @returns {boolean} 是否加码比例
 */
function isBoostedRatio(item) {
  return !!item && item.pointsRatio != null && parseFloat(item.pointsRatio) !== 1
}

/**
 * 地域标签：province 全省通用 / city 市县 / 商圈、景区拼接 city 与 regionName。
 *
 * @param {object} item 活动对象（含 regionType/city/regionName）
 * @returns {string} 地域标签文案
 */
function regionText(item) {
  if (!item) {
    return ''
  }
  if (!item.regionType || item.regionType === 'province') {
    return '全省通用'
  }
  if (item.regionType === 'city') {
    return item.city || '指定市县'
  }
  if (item.regionType === 'business_district' || item.regionType === 'scenic') {
    const fallback = item.regionType === 'business_district' ? '商圈' : '景区'
    const name = item.regionName || fallback
    return item.city ? item.city + '·' + name : name
  }
  return item.regionName || item.city || ''
}

/** 参与流程四步（上传发票 → 获得积分 → 兑换优惠券 → 到店核销） */
const steps = [
  { label: '上传发票', icon: icons.stepInvoice },
  { label: '获得积分', icon: icons.stepPoints },
  { label: '兑换优惠券', icon: icons.stepCoupon },
  { label: '到店核销', icon: icons.stepVerify }
]

/**
 * 加载首页数据：活动 banner + 活动列表 + 优选券（商城前 4 条）。
 * 商城接口需登录，游客跳过并展示登录引导。
 */
async function loadData() {
  const tasks = [
    getCurrentActivity()
      .then((data) => {
        activity.value = data
      })
      .catch(() => {}),
    getActivityList()
      .then((list) => {
        activities.value = list
      })
      .catch(() => {
        activities.value = []
      })
  ]
  if (userStore.isLogin) {
    tasks.push(
      userStore.fetchProfile().catch(() => {}),
      getMallCoupons()
        .then((list) => {
          coupons.value = list.slice(0, 4)
        })
        .catch(() => {})
    )
  } else {
    coupons.value = []
  }
  await Promise.all(tasks)
}

onShow(() => {
  loadData()
})

onPullDownRefresh(async () => {
  await loadData()
  uni.stopPullDownRefresh()
})

function onPointsCardClick() {
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  uni.navigateTo({ url: '/pages/points/records' })
}

function goMall() {
  uni.switchTab({ url: '/pages/coupon/mall' })
}

function goCouponDetail(couponId) {
  uni.navigateTo({ url: '/pages/coupon/detail?id=' + couponId })
}

function goLogin() {
  redirectToLogin()
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}

/* 活动 banner */
.banner {
  position: relative;
  border-radius: 16rpx;
  overflow: hidden;
  height: 300rpx;
}
.banner__image {
  width: 100%;
  height: 100%;
}
.banner__image--plain {
  background-color: var(--iip-primary);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 48rpx;
}
.banner__logo {
  color: rgba(255, 255, 255, 0.28);
  font-size: 64rpx;
  font-weight: 700;
  letter-spacing: 8rpx;
}
.banner__mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 32rpx;
  background-color: rgba(58, 47, 40, 0.45);
}
.banner__name {
  color: #ffffff;
  font-size: 34rpx;
  font-weight: 600;
}
.banner__time {
  color: rgba(255, 255, 255, 0.85);
  font-size: 24rpx;
  margin-top: 8rpx;
}
.banner__ratio {
  display: inline-flex;
  margin-top: 16rpx;
  padding: 6rpx 18rpx;
  border-radius: 8rpx;
  background-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-size: 22rpx;
}
.banner--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200rpx;
}
.banner__empty-text {
  color: var(--iip-text-muted);
  font-size: 26rpx;
}

/* 进行中的活动列表 */
.activities {
  margin-top: 32rpx;
}
.activities__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.activities__title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.activity-item {
  margin-bottom: 20rpx;
}
.activity-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.activity-item__name {
  flex: 1;
  margin-right: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: var(--iip-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.activity-item__region {
  flex-shrink: 0;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  background-color: var(--iip-primary-soft);
  color: var(--iip-primary-deep);
  font-size: 22rpx;
}
.activity-item__time {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.activity-item__ratio {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: var(--iip-text-secondary);
}
.activity-item__ratio--boost {
  display: inline-flex;
  padding: 6rpx 18rpx;
  border-radius: 8rpx;
  background-color: var(--iip-primary);
  color: #ffffff;
  font-weight: 600;
}

/* 参与流程 */
.steps {
  display: flex;
  margin-top: 24rpx;
  padding: 32rpx 12rpx;
}
.steps__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}
.steps__icon {
  width: 76rpx;
  height: 76rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.steps__label {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: var(--iip-text-secondary);
}
.steps__arrow {
  position: absolute;
  top: 22rpx;
  right: -8rpx;
  color: var(--iip-text-muted);
  font-size: 32rpx;
}

/* 我的积分卡片 */
.points {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  background-color: var(--iip-primary);
  border: none;
}
.points__label {
  color: rgba(255, 255, 255, 0.85);
  font-size: 24rpx;
}
.points__value {
  color: #ffffff;
  font-size: 56rpx;
  font-weight: 700;
  margin-top: 8rpx;
}
.points__value--login {
  font-size: 34rpx;
}
.points__detail {
  color: rgba(255, 255, 255, 0.9);
  font-size: 24rpx;
}

/* 优选券推荐 */
.recommend {
  margin-top: 32rpx;
}
.recommend__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.recommend__title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.recommend__more {
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.recommend__scroll {
  white-space: nowrap;
}
.coupon-card {
  display: inline-flex;
  flex-direction: column;
  width: 240rpx;
  margin-right: 20rpx;
  background-color: var(--iip-panel);
  border: 1rpx solid var(--iip-border);
  border-radius: 16rpx;
  overflow: hidden;
  vertical-align: top;
}
.coupon-card__cover {
  width: 100%;
  height: 160rpx;
}
.coupon-card__cover--plain {
  background-color: var(--iip-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}
.coupon-card__cover-icon {
  width: 72rpx;
  height: 72rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.coupon-card__name {
  padding: 16rpx 16rpx 0;
  font-size: 26rpx;
  color: var(--iip-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.coupon-card__cost {
  padding: 8rpx 16rpx 18rpx;
}
.coupon-card__points {
  color: var(--iip-primary-deep);
  font-size: 36rpx;
  font-weight: 700;
}
.coupon-card__unit {
  color: var(--iip-text-muted);
  font-size: 22rpx;
  margin-left: 6rpx;
}
.recommend__guest {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.recommend__guest-text {
  color: var(--iip-text-muted);
  font-size: 26rpx;
}
</style>
