<template>
  <view class="page">
    <!-- 首屏骨架屏 -->
    <view v-if="firstLoading" class="skel">
      <view class="iip-skel skel__hero"></view>
      <view class="iip-skel skel__strip"></view>
      <view class="iip-skel iip-skel--row skel__row"></view>
      <view class="iip-skel iip-skel--row skel__row skel__row--short"></view>
      <view class="iip-skel skel__block"></view>
    </view>

    <template v-else>
      <!-- 城市 / 活动规则 小行 -->
      <view class="topline">
        <view class="topline__loc">
          <view class="topline__loc-icon" :style="{ backgroundImage: icons.location }"></view>
          <text class="topline__city">{{ cityText }}</text>
        </view>
        <view class="topline__rules" @click="goRules">活动规则 ›</view>
      </view>

      <!-- 活动大横幅 -->
      <view class="iip-hero hero">
        <view class="hero__tag">以票促消 · 以游惠民</view>
        <view class="hero__title">{{ activity ? activity.activityName : '发票积分 · 惠游全城' }}</view>
        <view class="hero__desc">{{ heroDesc }}</view>
        <view class="hero__date" v-if="activity">
          活动期：{{ fmtDateDot(activity.startTime) }} ~ {{ fmtDateDot(activity.endTime) }}
        </view>
        <view class="hero__date" v-else>上传发票攒积分，兑换超值优惠好礼</view>
      </view>

      <!-- 我的积分白条 -->
      <view class="iip-card strip">
        <view class="strip__main">
          <view class="strip__lab">
            我的积分
            <text class="strip__detail" @click.stop="goPoints">明细 ›</text>
          </view>
          <view v-if="userStore.isLogin" class="strip__num iip-num">
            {{ fmtThousands(userStore.availablePoints) }}<text class="strip__unit">分</text>
          </view>
          <view v-else class="strip__guest">登录后查看积分</view>
        </view>
        <button v-if="userStore.isLogin" class="iip-btn iip-btn--ghost strip__go" @click="goMall">去兑换</button>
        <button v-else class="iip-btn iip-btn--ghost strip__go" @click="goLogin">去登录</button>
      </view>

      <!-- 参与活动流程 -->
      <view class="iip-card steps">
        <view class="iip-sect">参与活动流程</view>
        <view class="steps__row">
          <view class="steps__line"></view>
          <view class="step" v-for="(item, index) in steps" :key="index">
            <view class="step__ic">
              <view class="step__icon" :style="{ backgroundImage: item.icon }"></view>
            </view>
            <text class="step__name">{{ item.name }}</text>
            <text class="step__note">{{ item.note }}</text>
          </view>
        </view>
      </view>

      <!-- 双入口格 -->
      <view class="entry">
        <view class="entry__main" hover-class="iip-tap" @click="goUpload">
          <view class="entry__title">上传发票换积分</view>
          <view class="entry__desc">餐饮 / 住宿 / 加油发票，按面额 1:{{ ratioText }} 积分</view>
          <view class="entry__arrow">去上传 ›</view>
        </view>
        <view class="entry__side">
          <view class="entry__mini" hover-class="iip-tap" @click="goMyCoupons">
            <view class="entry__mini-title">我的券包</view>
            <view class="entry__mini-desc">{{ couponCountText }}</view>
          </view>
          <view class="entry__mini" hover-class="iip-tap" @click="goRules">
            <view class="entry__mini-title">活动规则</view>
            <view class="entry__mini-desc">积分与核销说明</view>
          </view>
        </view>
      </view>

      <!-- 进行中的活动列表 -->
      <template v-if="activities.length">
        <view class="sect-row">
          <view class="iip-sect">进行中活动</view>
        </view>
        <view
          class="iip-card act"
          v-for="item in activities"
          :key="item.activityId || item.activityName"
        >
          <view class="act__top">
            <text class="act__name">{{ item.activityName }}</text>
            <text class="iip-chip" :class="regionChipClass(item)">{{ regionChip(item) }}</text>
          </view>
          <view class="act__region">{{ regionText(item) }}</view>
          <view class="act__bottom">
            <text class="act__ratio">1 元 = {{ ratioOf(item) }} 分</text>
            <text class="act__time">{{ fmtDateDot(item.startTime) }} ~ {{ fmtDateDot(item.endTime) }}</text>
          </view>
        </view>
      </template>

      <!-- 积分商城优选券 -->
      <view class="sect-row">
        <view class="iip-sect">积分商城</view>
        <text class="sect-row__more" @click="goMall">更多 ›</text>
      </view>
      <scroll-view v-if="userStore.isLogin && coupons.length" scroll-x class="mall-scroll">
        <view
          class="mini-coupon"
          hover-class="iip-tap"
          v-for="item in coupons"
          :key="item.couponId"
          @click="goCouponDetail(item.couponId)"
        >
          <image
            v-if="item.coverImage"
            class="mini-coupon__cover"
            :src="resolveFileUrl(item.coverImage)"
            mode="aspectFill"
          />
          <view v-else class="mini-coupon__cover mini-coupon__cover--plain">
            <view class="mini-coupon__cover-icon" :style="{ backgroundImage: icons.ticket }"></view>
          </view>
          <view class="mini-coupon__name">{{ item.couponName }}</view>
          <view class="mini-coupon__cost">
            <text class="mini-coupon__points iip-num">{{ item.pointsCost }}</text>
            <text class="mini-coupon__unit">积分</text>
          </view>
        </view>
      </scroll-view>
      <view v-else-if="!userStore.isLogin" class="iip-card mall-guest">
        <text class="mall-guest__text">登录后查看优选优惠券</text>
        <button class="iip-btn iip-btn--ghost mall-guest__btn" @click="goLogin">去登录</button>
      </view>
      <view v-else class="iip-card mall-guest">
        <text class="mall-guest__text">暂无推荐优惠券</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getCurrentActivity, getActivityList } from '@/api/activity.js'
import { getMallCoupons, getMyCoupons } from '@/api/coupon.js'
import { resolveFileUrl } from '@/common/config.js'
import { redirectToLogin } from '@/common/request.js'
import { fmtThousands } from '@/common/format.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const activity = ref(null)
const activities = ref([])
const coupons = ref([])
const myCouponCount = ref(null)
const firstLoading = ref(true)

/** 城市小行文案：当前活动 city，无 city 或无活动则为「全省通用」 */
const cityText = computed(() => {
  if (activity.value && activity.value.city) {
    return activity.value.city
  }
  return '全省通用'
})

/** Hero 摘要：活动 description，缺省时给平台通用文案 */
const heroDesc = computed(() => {
  if (activity.value && activity.value.description) {
    return activity.value.description
  }
  if (activity.value) {
    return '上传发票攒积分，兑换超值优惠好礼'
  }
  return '消费留票，积分换礼，文旅惠民活动陆续上线'
})

/** 当前活动积分比例展示（BigDecimal 序列化如 1.00，去尾零后展示） */
const ratioText = computed(() => ratioOf(activity.value))

/** 我的券包副文案 */
const couponCountText = computed(() => {
  if (!userStore.isLogin) {
    return '登录后查看'
  }
  if (myCouponCount.value == null) {
    return '查看全部'
  }
  return myCouponCount.value + ' 张待使用'
})

/** 参与流程四步（上传发票 → 获得积分 → 兑换优惠 → 景区核销） */
const steps = [
  { name: '上传发票', note: '拍照 / PDF', icon: icons.stepInvoice },
  { name: '获得积分', note: '按面额比例', icon: icons.stepPoints },
  { name: '兑换优惠', note: '门票 / 满减券', icon: icons.stepCoupon },
  { name: '景区核销', note: '扫码入园', icon: icons.stepVerify }
]

/**
 * 日期展示为 yyyy.MM.dd。
 *
 * @param {string} value 后端日期时间字符串
 * @returns {string} yyyy.MM.dd 或空串
 */
function fmtDateDot(value) {
  return value ? String(value).slice(0, 10).replace(/-/g, '.') : ''
}

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

/**
 * 活动类型 chip 文案。
 *
 * @param {object} item 活动对象
 * @returns {string} chip 文案
 */
function regionChip(item) {
  const names = {
    province: '全省',
    city: '城市',
    business_district: '商圈',
    scenic: '景区'
  }
  return names[item && item.regionType] || '全省'
}

/**
 * 活动类型 chip 配色类。
 *
 * @param {object} item 活动对象
 * @returns {string} iip-chip 变体类名
 */
function regionChipClass(item) {
  const classes = {
    province: 'iip-chip--g',
    city: 'iip-chip--y',
    business_district: 'iip-chip--y',
    scenic: 'iip-chip--r'
  }
  return classes[item && item.regionType] || 'iip-chip--g'
}

/**
 * 加载首页数据：当前活动 + 活动列表；登录后追加积分资料、券包张数与商城优选券（前 6）。
 * 游客与接口失败均静默降级。
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
      getMyCoupons('0')
        .then((list) => {
          myCouponCount.value = list.length
        })
        .catch(() => {
          myCouponCount.value = null
        }),
      getMallCoupons()
        .then((list) => {
          coupons.value = list.slice(0, 6)
        })
        .catch(() => {})
    )
  } else {
    coupons.value = []
    myCouponCount.value = null
  }
  await Promise.all(tasks)
}

onShow(async () => {
  await loadData()
  firstLoading.value = false
})

onPullDownRefresh(async () => {
  await loadData()
  uni.stopPullDownRefresh()
})

function goRules() {
  uni.navigateTo({ url: '/pages/activity/rules' })
}

function goPoints() {
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  uni.navigateTo({ url: '/pages/points/records' })
}

function goUpload() {
  uni.navigateTo({ url: '/pages/invoice/upload' })
}

function goMyCoupons() {
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  uni.navigateTo({ url: '/pages/coupon/mine' })
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

/* 首屏骨架屏 */
.skel__hero {
  height: 360rpx;
  border-radius: 40rpx;
  margin-top: 24rpx;
}
.skel__strip {
  height: 128rpx;
  border-radius: 32rpx;
  margin-top: 24rpx;
}
.skel__row {
  margin-top: 24rpx;
}
.skel__row--short {
  width: 60%;
}
.skel__block {
  height: 220rpx;
  border-radius: 32rpx;
  margin-top: 24rpx;
}

/* 城市 / 活动规则 小行 */
.topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}
.topline__loc {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.topline__loc-icon {
  width: 28rpx;
  height: 28rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.topline__city {
  font-size: var(--iip-fs-28);
  font-weight: 700;
  color: var(--iip-color-ink);
}
.topline__rules {
  padding: 8rpx 24rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-pill);
}

/* 活动大横幅 */
.hero {
  padding: 44rpx 40rpx 52rpx;
}
.hero__tag {
  display: inline-block;
  padding: 6rpx 20rpx;
  font-size: var(--iip-fs-22);
  letter-spacing: 2rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: var(--iip-radius-pill);
}
.hero__title {
  margin-top: 20rpx;
  font-size: 48rpx;
  font-weight: 900;
  letter-spacing: 2rpx;
  line-height: 1.3;
}
.hero__desc {
  margin-top: 12rpx;
  font-size: var(--iip-fs-24);
  line-height: 1.6;
  opacity: 0.9;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.hero__date {
  display: inline-block;
  margin-top: 24rpx;
  padding-top: 20rpx;
  font-size: var(--iip-fs-22);
  opacity: 0.85;
  border-top: 1rpx dashed rgba(255, 255, 255, 0.35);
}

/* 我的积分白条 */
.strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  padding: 28rpx 32rpx;
}
.strip__lab {
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}
.strip__detail {
  margin-left: 12rpx;
  font-size: var(--iip-fs-20);
  text-decoration: underline;
}
.strip__num {
  margin-top: 8rpx;
  font-size: 52rpx;
  font-weight: 900;
  line-height: 1.1;
  color: var(--iip-color-ink);
}
.strip__unit {
  margin-left: 8rpx;
  font-size: var(--iip-fs-24);
  font-weight: 400;
  color: var(--iip-color-gold);
}
.strip__guest {
  margin-top: 12rpx;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.strip__go {
  padding: 14rpx 32rpx;
  font-size: var(--iip-fs-24);
  flex: 0 0 auto;
}

/* 参与活动流程 */
.steps {
  margin-top: 24rpx;
  padding: 32rpx;
}
.steps__row {
  display: flex;
  position: relative;
  margin-top: 28rpx;
}
.steps__line {
  position: absolute;
  top: 38rpx;
  left: 12%;
  right: 12%;
  height: 2rpx;
  background-color: var(--iip-color-line);
}
.step {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.step__ic {
  width: 76rpx;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--iip-color-cream);
  border: 1rpx solid var(--iip-color-line);
  border-radius: 24rpx;
}
.step__icon {
  width: 40rpx;
  height: 40rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.step__name {
  margin-top: 14rpx;
  font-size: var(--iip-fs-22);
  font-weight: 600;
  color: var(--iip-color-ink);
}
.step__note {
  margin-top: 4rpx;
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}

/* 双入口格 */
.entry {
  display: flex;
  gap: 20rpx;
  margin-top: 24rpx;
}
.entry__main {
  flex: 1.35;
  position: relative;
  padding: 32rpx 28rpx 72rpx;
  background-color: var(--iip-color-surface);
  border: 3rpx solid var(--iip-color-primary);
  border-radius: var(--iip-radius-32);
}
.entry__title {
  font-size: var(--iip-fs-30);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.entry__desc {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  line-height: 1.5;
  color: var(--iip-color-text-secondary);
}
.entry__arrow {
  position: absolute;
  right: 24rpx;
  bottom: 24rpx;
  font-size: var(--iip-fs-22);
  font-weight: 700;
  color: var(--iip-color-primary);
}
.entry__side {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.entry__mini {
  flex: 1;
  padding: 24rpx;
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-32);
}
.entry__mini-title {
  font-size: var(--iip-fs-26);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.entry__mini-desc {
  margin-top: 6rpx;
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}

/* 章节行 */
.sect-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 32rpx 4rpx 20rpx;
}
.sect-row__more {
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}

/* 活动卡 */
.act {
  padding: 28rpx 32rpx;
  margin-bottom: 20rpx;
}
.act__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}
.act__name {
  flex: 1;
  min-width: 0;
  font-size: var(--iip-fs-28);
  font-weight: 700;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.act__region {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}
.act__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}
.act__ratio {
  font-size: var(--iip-fs-22);
  font-weight: 700;
  color: var(--iip-color-primary);
}
.act__time {
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}

/* 积分商城横滑券卡 */
.mall-scroll {
  white-space: nowrap;
}
.mini-coupon {
  display: inline-flex;
  flex-direction: column;
  width: 260rpx;
  margin-right: 20rpx;
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-24);
  overflow: hidden;
  vertical-align: top;
}
.mini-coupon__cover {
  width: 100%;
  height: 170rpx;
}
.mini-coupon__cover--plain {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--iip-color-cream);
}
.mini-coupon__cover-icon {
  width: 72rpx;
  height: 72rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.mini-coupon__name {
  padding: 16rpx 20rpx 0;
  font-size: var(--iip-fs-24);
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mini-coupon__cost {
  padding: 8rpx 20rpx 20rpx;
}
.mini-coupon__points {
  font-size: var(--iip-fs-36);
  font-weight: 700;
  color: var(--iip-color-primary);
}
.mini-coupon__unit {
  margin-left: 6rpx;
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}

/* 商城游客 / 空态 */
.mall-guest {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
}
.mall-guest__text {
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.mall-guest__btn {
  padding: 12rpx 32rpx;
  font-size: var(--iip-fs-24);
  flex: 0 0 auto;
}
</style>
