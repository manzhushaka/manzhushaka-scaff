<template>
  <view class="page">
    <!-- 未登录引导 -->
    <view v-if="!userStore.isLogin" class="guest">
      <view class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <view class="iip-empty__title">登录后进入商户中心</view>
        <view class="iip-empty__desc">登录后可查看核销工作台与门店信息</view>
      </view>
      <button class="iip-btn guest__btn" @click="goLogin">去登录</button>
    </view>

    <!-- 未入驻：引导申请 -->
    <view v-else-if="loaded && !merchant" class="guide">
      <view class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.shop }"></view>
        <view class="iip-empty__title">入驻成为参与商户</view>
        <view class="iip-empty__desc">入驻审核通过后，即可为用户核销兑换的优惠券</view>
      </view>
      <button class="iip-btn guide__btn" @click="goApply">申请入驻</button>
    </view>

    <!-- 待审核 -->
    <view v-else-if="loaded && merchant && merchant.status === '2'" class="iip-empty">
      <view class="iip-empty__icon" :style="{ backgroundImage: icons.clock }"></view>
      <view class="iip-empty__title">入驻申请审核中</view>
      <view class="iip-empty__desc">「{{ merchant.merchantName }}」已提交，请等待管理员审核</view>
      <view class="iip-empty__desc" v-if="merchant.createTime">提交时间：{{ fmtMinute(merchant.createTime) }}</view>
    </view>

    <!-- 已停用 -->
    <view v-else-if="loaded && merchant && merchant.status === '1'" class="iip-empty">
      <view class="iip-empty__icon" :style="{ backgroundImage: icons.clock }"></view>
      <view class="iip-empty__title">商户已停用</view>
      <view class="iip-empty__desc" v-if="merchant.auditRemark">原因：{{ merchant.auditRemark }}</view>
      <view class="iip-empty__desc">如有疑问请联系平台运营</view>
    </view>

    <!-- 正常：问候卡 + KPI + 扫码入口 + 菜单 -->
    <template v-else-if="loaded && merchant && merchant.status === '0'">
      <view class="iip-dark-card hero">
        <view class="hero__top">
          <text class="hero__shop">
            {{ merchant.merchantName }}<text v-if="merchant.category"> · {{ merchant.category }}</text>
          </text>
          <text class="iip-chip hero__chip">正常营业</text>
        </view>
        <view class="hero__hello">{{ greeting }}，{{ merchant.contactName || merchant.merchantName }}</view>
      </view>

      <view v-if="stats" class="kpi">
        <view class="iip-card kpi__item">
          <view class="kpi__num iip-num">{{ fmtThousands(stats.todayCount || 0) }}</view>
          <view class="kpi__label">今日核销（笔）</view>
        </view>
        <view class="iip-card kpi__item">
          <view class="kpi__num kpi__num--red iip-num">{{ fmtThousands(stats.todayPoints || 0) }}</view>
          <view class="kpi__label">今日核销积分</view>
        </view>
        <view class="iip-card kpi__item">
          <view class="kpi__num iip-num">{{ fmtThousands(stats.totalCount || 0) }}</view>
          <view class="kpi__label">累计核销（笔）</view>
        </view>
      </view>

      <button class="iip-btn scan" @click="goVerify">
        <view class="scan__icon" :style="{ backgroundImage: icons.scanWhite }"></view>
        <text>扫码核销</text>
      </button>

      <view class="iip-menu menu">
        <view class="iip-menu__row" hover-class="iip-tap-bg" @click="goRecords">
          <view class="iip-menu__icon" :style="{ backgroundImage: icons.ticket }"></view>
          <text class="iip-menu__title">核销记录</text>
          <view class="iip-menu__chev"></view>
        </view>
        <view class="iip-menu__row" hover-class="iip-tap-bg" @click="showInfo = true">
          <view class="iip-menu__icon" :style="{ backgroundImage: icons.shop }"></view>
          <text class="iip-menu__title">门店信息</text>
          <view class="iip-menu__chev"></view>
        </view>
      </view>

      <view class="iip-listfoot">补贴与结算以商务局公示为准</view>
    </template>

    <!-- 门店信息半屏弹层（只读） -->
    <view v-if="showInfo && merchant" class="sheet">
      <view class="sheet__mask" @click="showInfo = false"></view>
      <view class="sheet__panel">
        <view class="sheet__title">门店信息</view>
        <view class="sheet__rows">
          <view class="srow">
            <text class="srow__label">商户编号</text>
            <text class="srow__value">{{ merchant.merchantNo || '—' }}</text>
          </view>
          <view class="srow">
            <text class="srow__label">商户类别</text>
            <text class="srow__value">{{ merchant.category || '—' }}</text>
          </view>
          <view class="srow">
            <text class="srow__label">联系人</text>
            <text class="srow__value">{{ merchant.contactName || '—' }}</text>
          </view>
          <view class="srow">
            <text class="srow__label">联系电话</text>
            <text class="srow__value">{{ merchant.contactPhone || '—' }}</text>
          </view>
          <view class="srow">
            <text class="srow__label">商户地址</text>
            <text class="srow__value">{{ merchant.address || '—' }}</text>
          </view>
          <view class="srow">
            <text class="srow__label">营业时间</text>
            <text class="srow__value">{{ merchant.businessHours || '—' }}</text>
          </view>
          <view class="srow">
            <text class="srow__label">商户简介</text>
            <text class="srow__value">{{ merchant.description || '—' }}</text>
          </view>
        </view>
        <button class="iip-btn sheet__btn" @click="showInfo = false">知道了</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getMerchantInfo, getVerifyStats } from '@/api/merchant.js'
import { redirectToLogin } from '@/common/request.js'
import { fmtMinute, fmtThousands } from '@/common/format.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const merchant = ref(null)
const loaded = ref(false)
/** 核销工作台统计，接口失败时保持 null 整行隐藏 */
const stats = ref(null)
/** 门店信息弹层开关 */
const showInfo = ref(false)

/** 按时段生成问候语 */
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) {
    return '上午好'
  }
  if (hour < 18) {
    return '下午好'
  }
  return '晚上好'
})

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

/** 加载核销统计（仅正常营业商户调用；失败静默隐藏 KPI 行） */
async function loadStats() {
  try {
    stats.value = await getVerifyStats()
  } catch (e) {
    stats.value = null
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

onShow(async () => {
  loaded.value = false
  stats.value = null
  showInfo.value = false
  await loadInfo()
  if (merchant.value && merchant.value.status === '0') {
    loadStats()
  }
})
</script>

<style scoped>
.page {
  padding: 24rpx;
}

.guest__btn {
  margin: 0 64rpx;
}

.guide__btn {
  width: 100%;
}

/* 深色问候卡：上行小字 + 右上金 chip，下行 800 大字问候 */
.hero {
  padding: 32rpx;
}
.hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}
.hero__shop {
  flex: 1;
  min-width: 0;
  font-size: var(--iip-fs-26);
  color: rgba(255, 255, 255, 0.75);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* 营业状态 chip：墨底卡上取金系（与 .iip-dark-card .iip-gold 的 #ffd98a 一致），胶囊/字号/字重沿用全局 .iip-chip */
.hero__chip {
  background-color: rgba(255, 217, 138, 0.16);
  color: #ffd98a;
}
.hero__hello {
  margin-top: 8rpx;
  font-size: var(--iip-fs-36);
  font-weight: 800;
  color: #ffffff;
}

/* 3 KPI 白卡行 */
.kpi {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}
.kpi__item {
  flex: 1;
  min-width: 0;
  padding: 24rpx 12rpx;
  text-align: center;
}
.kpi__num {
  font-size: var(--iip-fs-44);
  font-weight: 900;
  color: var(--iip-color-ink);
}
.kpi__num--red {
  color: var(--iip-color-primary);
}
.kpi__label {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}

/* 扫码核销大按钮 */
.scan {
  margin-top: 24rpx;
  height: 104rpx;
  font-size: 34rpx;
  font-weight: 800;
}
.scan__icon {
  width: 44rpx;
  height: 44rpx;
  margin-right: 16rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

.menu {
  margin-top: 24rpx;
}

/* 门店信息半屏弹层 */
.sheet {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.sheet__mask {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.45);
}
.sheet__panel {
  position: relative;
  padding: 32rpx 32rpx 48rpx;
  background-color: var(--iip-color-surface);
  border-radius: 32rpx 32rpx 0 0;
}
.sheet__title {
  font-size: var(--iip-fs-32);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.sheet__rows {
  margin-top: 16rpx;
}
.srow {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid var(--iip-color-line);
}
.srow:last-child {
  border-bottom: none;
}
.srow__label {
  flex: 0 0 160rpx;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-text-secondary);
}
.srow__value {
  flex: 1;
  min-width: 0;
  font-size: var(--iip-fs-26);
  color: var(--iip-color-ink);
  text-align: right;
}
.sheet__btn {
  width: 100%;
  margin-top: 24rpx;
}
</style>
