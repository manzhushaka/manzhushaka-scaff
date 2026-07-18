<template>
  <view class="page">
    <template v-if="detail">
      <!-- 券票头卡：红渐变头 + 白区主体（接缝虚线 + 两侧打孔） -->
      <view class="ticket">
        <view class="ticket__head iip-hero">
          <view class="ticket__title-row">
            <view class="ticket__name">{{ detail.couponName }}</view>
            <text class="ticket__type">{{ couponTypeName(detail.couponType) }}</text>
          </view>
          <view class="ticket__chips">
            <text class="ticket__chip">{{ couponCategoryName(detail.category) }}</text>
            <text v-if="sponsorText" class="ticket__chip">{{ sponsorText }}</text>
          </view>
        </view>
        <view class="ticket__body">
          <view class="ticket__cost">
            <view class="ticket__price">
              <text class="ticket__points iip-num">{{ fmtThousands(detail.pointsCost) }}</text>
              <text class="ticket__unit">积分</text>
            </view>
            <view class="ticket__benefit" v-if="benefitText">{{ benefitText }}</view>
          </view>
          <view class="ticket__side">
            <text class="ticket__stock">{{ stockText }}</text>
            <text class="ticket__exchanged" v-if="detail.exchangedCount">已兑 {{ detail.exchangedCount }} 张</text>
          </view>
        </view>
      </view>

      <!-- 兑换信息 -->
      <view class="iip-sect sect">兑换信息</view>
      <view class="iip-card panel">
        <view class="kv">
          <text class="kv__label">兑换窗口</text>
          <text class="kv__value">{{ exchangeWindowText }}</text>
        </view>
        <view class="kv">
          <text class="kv__label">有效期</text>
          <text class="kv__value">{{ validText }}</text>
        </view>
        <view class="kv">
          <text class="kv__label">每人限兑</text>
          <text class="kv__value">{{ limitText }}</text>
        </view>
        <view class="kv">
          <text class="kv__label">剩余库存</text>
          <text class="kv__value">{{ stockText }}</text>
        </view>
        <view class="kv" v-if="detail.targetName">
          <text class="kv__label">适用对象</text>
          <text class="kv__value">{{ detail.targetName }}</text>
        </view>
        <view class="kv" v-if="sponsorText">
          <text class="kv__label">赞助方</text>
          <text class="kv__value">{{ sponsorText }}</text>
        </view>
      </view>

      <!-- 商户信息（券绑定商户时展示） -->
      <template v-if="detail.merchantName">
        <view class="iip-sect sect">商户信息</view>
        <view class="iip-card panel merchant">
          <view class="merchant__head">
            <image
              v-if="detail.merchantLogo"
              class="merchant__logo"
              :src="resolveFileUrl(detail.merchantLogo)"
              mode="aspectFill"
            />
            <view v-else class="merchant__logo merchant__logo--plain">
              <view class="merchant__logo-icon" :style="{ backgroundImage: icons.shop }"></view>
            </view>
            <view class="merchant__name">{{ detail.merchantName }}</view>
          </view>
          <view class="merchant__desc" v-if="detail.merchantDescription">{{ detail.merchantDescription }}</view>
          <view class="merchant__row" v-if="detail.merchantBusinessHours">
            <view class="merchant__icon" :style="{ backgroundImage: icons.clock }"></view>
            <text class="merchant__row-text">{{ detail.merchantBusinessHours }}</text>
          </view>
          <view class="merchant__row" v-if="detail.merchantAddress" @click="handleAddressTap" hover-class="iip-tap-bg">
            <view class="merchant__icon" :style="{ backgroundImage: icons.location }"></view>
            <text class="merchant__row-text">{{ detail.merchantAddress }}</text>
            <text class="merchant__action">{{ hasMerchantLocation ? '导航' : '复制' }}</text>
          </view>
          <view class="merchant__row" v-if="detail.merchantPhone" @click="callMerchant" hover-class="iip-tap-bg">
            <view class="merchant__icon" :style="{ backgroundImage: icons.phone }"></view>
            <text class="merchant__row-text">{{ detail.merchantPhone }}</text>
            <text class="merchant__action">拨打</text>
          </view>
        </view>
      </template>

      <!-- 使用说明 -->
      <template v-if="detail.useDesc">
        <view class="iip-sect sect">使用说明</view>
        <view class="iip-card panel">
          <view class="desc">{{ detail.useDesc }}</view>
        </view>
      </template>

      <!-- 底部固定兑换栏 -->
      <view class="bar">
        <view class="bar__balance">
          <text class="bar__label">我的积分</text>
          <text class="bar__value iip-num">{{ fmtThousands(userStore.availablePoints) }}</text>
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

      <!-- 兑换成功弹层（mask 点击不关闭，防误触） -->
      <view class="mask" v-if="exchangeResult">
        <view class="sheet">
          <view class="sheet__ok">
            <view class="sheet__ok-icon" :style="{ backgroundImage: icons.check }"></view>
          </view>
          <view class="sheet__title">兑换成功</view>
          <view class="sheet__points iip-num">
            -{{ fmtThousands(exchangeResult.pointsCost != null ? exchangeResult.pointsCost : detail.pointsCost) }}<text class="sheet__points-unit">积分</text>
          </view>
          <view class="sheet__name">{{ exchangeResult.couponName }}</view>
          <view class="sheet__qr">
            <QrCode :text="exchangeResult.verifyCode" :size="168" />
          </view>
          <view class="sheet__code-label">核销码</view>
          <view class="sheet__code">{{ formatVerifyCode(exchangeResult.verifyCode) }}</view>
          <view class="sheet__valid" v-if="exchangeResult.validEndTime">
            有效期至 {{ fmtMinute(exchangeResult.validEndTime) }}
          </view>
          <view class="sheet__actions">
            <button class="iip-btn iip-btn--ghost sheet__btn" @click="closeSuccess">继续逛逛</button>
            <button class="iip-btn sheet__btn" @click="goMine">查看券包</button>
          </view>
        </view>
      </view>
    </template>

    <!-- 加载失败/游客降级（游客无 token 时详情接口 reject） -->
    <view v-else-if="loadFailed" class="iip-empty failed">
      <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
      <view class="iip-empty__title">券详情加载失败</view>
      <view class="iip-empty__desc">{{ userStore.isLogin ? '请稍后重试' : '登录后可查看券详情并兑换' }}</view>
      <button v-if="!userStore.isLogin" class="iip-btn failed__btn" @click="goLogin">去登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getCouponDetail, exchangeCoupon } from '@/api/coupon.js'
import { resolveFileUrl } from '@/common/config.js'
import { fmtDate, fmtMinute, fmtThousands, couponTypeName, couponCategoryName, couponSponsorText } from '@/common/format.js'
import { redirectToLogin } from '@/common/request.js'
import icons from '@/common/icons.js'
import QrCode from '@/components/QrCode.vue'

const userStore = useUserStore()

const couponId = ref(null)
const detail = ref(null)
const loadFailed = ref(false)
const exchangeResult = ref(null)
const exchanging = ref(false)

onLoad((options) => {
  couponId.value = options.id
  loadDetail()
})

async function loadDetail() {
  loadFailed.value = false
  try {
    detail.value = await getCouponDetail(couponId.value)
  } catch (e) {
    // 错误提示已由 request 封装统一处理；游客无 token 时展示降级空态
    loadFailed.value = true
  }
  if (userStore.isLogin) {
    userStore.fetchProfile().catch(() => {})
  }
}

/** 赞助方文案（platform 平台券为空串，不展示） */
const sponsorText = computed(() => {
  if (!detail.value) {
    return ''
  }
  return couponSponsorText(detail.value.sponsorType, detail.value.sponsorName)
})

/** 头卡权益文案：满减券显示门槛面额，折扣券提取折扣，门票/虚拟券显示适用对象 */
const benefitText = computed(() => {
  const d = detail.value
  if (!d) {
    return ''
  }
  if (d.couponType === 'full_reduction' && d.thresholdAmount != null && d.discountAmount != null) {
    return '满 ' + Number(d.thresholdAmount) + ' 元减 ' + Number(d.discountAmount) + ' 元'
  }
  if (d.couponType === 'discount') {
    const match = /(\d+(?:\.\d+)?折)/.exec(d.couponName || '')
    return match ? match[1] : d.targetName || ''
  }
  return d.targetName || ''
})

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

/**
 * 当前时间字符串（yyyy-MM-dd HH:mm:ss，与后端 GMT+8 序列化格式一致，
 * 定宽格式字典序即时间序，避免 new Date 解析 '-' 分隔符的端差异）。
 *
 * @returns {string} 当前时间字符串
 */
function nowText() {
  const d = new Date()
  const p = (n) => (n < 10 ? '0' + n : '' + n)
  return (
    d.getFullYear() +
    '-' +
    p(d.getMonth() + 1) +
    '-' +
    p(d.getDate()) +
    ' ' +
    p(d.getHours()) +
    ':' +
    p(d.getMinutes()) +
    ':' +
    p(d.getSeconds())
  )
}

/** 不在兑换窗口内的原因文案（空串表示在窗口内） */
const windowClosedText = computed(() => {
  const d = detail.value
  if (!d) {
    return ''
  }
  const now = nowText()
  if (d.exchangeStartTime && now < d.exchangeStartTime) {
    return '未到兑换时间'
  }
  if (d.exchangeEndTime && now > d.exchangeEndTime) {
    return '兑换已截止'
  }
  return ''
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

/** 兑换按钮禁用态（未登录不禁用，点击引导登录） */
const exchangeDisabled = computed(() => {
  if (!detail.value) {
    return true
  }
  if (!userStore.isLogin) {
    return false
  }
  return detail.value.remainStock === 0 || !!windowClosedText.value || overLimit.value || pointsNotEnough.value
})

/** 兑换按钮文案（库存为 0 或不在兑换窗口时显示原因） */
const exchangeButtonText = computed(() => {
  if (!detail.value) {
    return '加载中'
  }
  if (!userStore.isLogin) {
    return '登录后兑换'
  }
  if (detail.value.remainStock === 0) {
    return '已售罄'
  }
  if (windowClosedText.value) {
    return windowClosedText.value
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
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  if (exchangeDisabled.value || exchanging.value) {
    return
  }
  uni.showModal({
    title: '确认兑换',
    content: '消耗 ' + fmtThousands(detail.value.pointsCost) + ' 积分兑换「' + detail.value.couponName + '」？',
    confirmText: '确认兑换',
    confirmColor: '#c93418',
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

/** 商家经纬度是否齐全（齐全时地址行唤起地图导航，否则复制地址） */
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
 * 地址行点击：经纬度齐全时唤起地图查看并导航，无坐标时复制地址到剪贴板。
 */
function handleAddressTap() {
  if (!detail.value || !detail.value.merchantAddress) {
    return
  }
  if (hasMerchantLocation.value) {
    uni.openLocation({
      latitude: Number(detail.value.merchantLatitude),
      longitude: Number(detail.value.merchantLongitude),
      name: detail.value.merchantName,
      address: detail.value.merchantAddress
    })
    return
  }
  uni.setClipboardData({ data: detail.value.merchantAddress })
}

/** 核销码分组展示（每 4 位一组，如 4-4-4-4） */
function formatVerifyCode(code) {
  if (!code) {
    return ''
  }
  return String(code).replace(/(.{4})/g, '$1 ').trim()
}

/** 查看券包：关闭弹层并跳我的券 */
function goMine() {
  exchangeResult.value = null
  uni.redirectTo({ url: '/pages/coupon/mine' })
}

/** 关闭成功弹层并刷新详情（库存与已兑数量已变化） */
function closeSuccess() {
  exchangeResult.value = null
  loadDetail()
}

function goLogin() {
  redirectToLogin()
}
</script>

<style scoped>
.page {
  padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
}

/* 券票头卡：红渐变头 + 白区主体 */
.ticket {
  margin: var(--iip-sp-24) var(--iip-sp-24) 0;
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-32);
  overflow: hidden;
}
.ticket__head {
  border-radius: 0;
  padding: 32rpx 32rpx 36rpx;
}
.ticket__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--iip-sp-16);
}
.ticket__name {
  flex: 1;
  min-width: 0;
  font-size: var(--iip-fs-36);
  font-weight: 800;
  line-height: 1.35;
}
.ticket__type {
  flex-shrink: 0;
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  opacity: 0.85;
}
.ticket__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: var(--iip-sp-16);
}
.ticket__chip {
  font-size: var(--iip-fs-20);
  font-weight: 600;
  line-height: 1.4;
  padding: 4rpx 16rpx;
  border-radius: var(--iip-radius-pill);
  background-color: rgba(255, 255, 255, 0.2);
}
.ticket__body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--iip-sp-24);
  padding: 28rpx 32rpx;
  border-top: 2rpx dashed var(--iip-color-line);
  /* 头体接缝两侧打孔（页面底色圆，对齐原型 qr-ticket） */
  background-image: radial-gradient(circle at 0 0, var(--iip-color-bg) 16rpx, transparent 16rpx),
    radial-gradient(circle at 100% 0, var(--iip-color-bg) 16rpx, transparent 16rpx);
}
.ticket__price {
  display: flex;
  align-items: baseline;
}
.ticket__points {
  font-size: var(--iip-fs-56);
  font-weight: 800;
  line-height: 1.1;
  color: var(--iip-color-primary);
}
.ticket__unit {
  margin-left: 8rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}
.ticket__benefit {
  margin-top: 8rpx;
  font-size: var(--iip-fs-24);
  font-weight: 600;
  color: var(--iip-color-gold);
}
.ticket__side {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}
.ticket__stock {
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}
.ticket__exchanged {
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-faint);
}

/* 章节标题与卡片节奏 */
.sect {
  margin: var(--iip-sp-24) 32rpx var(--iip-sp-16);
}
.panel {
  margin: 0 var(--iip-sp-24);
  padding: 12rpx 32rpx;
}

/* 兑换信息 label-value 行 */
.kv {
  display: flex;
  padding: 20rpx 0;
}
.kv + .kv {
  border-top: 1rpx solid var(--iip-color-line);
}
.kv__label {
  width: 150rpx;
  flex-shrink: 0;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.kv__value {
  flex: 1;
  min-width: 0;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-ink);
}

/* 商户信息卡片 */
.merchant {
  padding: 28rpx 32rpx;
}
.merchant__head {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.merchant__logo {
  width: 88rpx;
  height: 88rpx;
  flex-shrink: 0;
  border-radius: var(--iip-radius-16);
}
.merchant__logo--plain {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--iip-color-cream);
}
.merchant__logo-icon {
  width: 44rpx;
  height: 44rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.merchant__name {
  flex: 1;
  min-width: 0;
  font-size: var(--iip-fs-30);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.merchant__desc {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  margin-top: var(--iip-sp-16);
  font-size: var(--iip-fs-26);
  line-height: 1.7;
  color: var(--iip-color-text-secondary);
}
.merchant__row {
  display: flex;
  align-items: center;
  margin-top: var(--iip-sp-16);
}
.merchant__icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
  margin-right: var(--iip-sp-16);
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.merchant__row-text {
  flex: 1;
  min-width: 0;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.merchant__action {
  flex-shrink: 0;
  margin-left: var(--iip-sp-16);
  font-size: var(--iip-fs-24);
  font-weight: 600;
  color: var(--iip-color-primary);
}

/* 使用说明（保留换行多行渲染） */
.desc {
  padding: var(--iip-sp-16) 0;
  font-size: var(--iip-fs-26);
  line-height: 1.7;
  color: var(--iip-color-text-secondary);
  white-space: pre-line;
}

/* 底部固定兑换栏 */
.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: var(--iip-sp-24);
  padding: var(--iip-sp-16) var(--iip-sp-24) calc(var(--iip-sp-16) + env(safe-area-inset-bottom));
  background-color: var(--iip-color-surface);
  border-top: 1rpx solid var(--iip-color-line);
}
.bar__balance {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.bar__label {
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}
.bar__value {
  font-size: var(--iip-fs-36);
  font-weight: 800;
  line-height: 1.2;
  color: var(--iip-color-gold);
}
.bar__btn {
  flex: 1;
}

/* 兑换成功弹层（对齐原型 .sheet.center；遮罩色为墨色透明变体） */
.mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(34, 30, 24, 0.5);
}
.sheet {
  width: 600rpx;
  padding: 48rpx 44rpx 44rpx;
  background-color: var(--iip-color-surface);
  border-radius: 44rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: sheet-pop 0.22s ease;
}
@keyframes sheet-pop {
  0% {
    transform: scale(0.9);
    opacity: 0;
  }
  100% {
    transform: none;
    opacity: 1;
  }
}
.sheet__ok {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background-color: var(--iip-color-chip-green);
  display: flex;
  align-items: center;
  justify-content: center;
}
.sheet__ok-icon {
  width: 56rpx;
  height: 56rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.sheet__title {
  margin-top: 20rpx;
  font-size: var(--iip-fs-32);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.sheet__points {
  margin-top: 12rpx;
  font-size: 72rpx;
  font-weight: 900;
  line-height: 1.1;
  color: var(--iip-color-gold);
}
.sheet__points-unit {
  margin-left: 8rpx;
  font-size: var(--iip-fs-24);
  font-weight: 600;
}
.sheet__name {
  margin-top: 8rpx;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
  text-align: center;
}
.sheet__qr {
  margin-top: 28rpx;
  line-height: 0;
}
.sheet__code-label {
  margin-top: 20rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}
.sheet__code {
  margin-top: 8rpx;
  font-family: 'Courier New', Courier, monospace;
  font-size: var(--iip-fs-36);
  font-weight: 700;
  letter-spacing: 4rpx;
  color: var(--iip-color-ink);
}
.sheet__valid {
  margin-top: var(--iip-sp-16);
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-faint);
}
.sheet__actions {
  display: flex;
  gap: 20rpx;
  width: 100%;
  margin-top: 36rpx;
}
.sheet__btn {
  flex: 1;
  padding: 20rpx 0;
  font-size: var(--iip-fs-26);
}

/* 加载失败/游客降级 */
.failed {
  padding-top: 160rpx;
}
.failed__btn {
  margin-top: 32rpx;
  padding: 16rpx 64rpx;
}
</style>
