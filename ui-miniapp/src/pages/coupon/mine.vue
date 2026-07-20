<template>
  <view class="page">
    <!-- 游客态：引导登录 -->
    <view v-if="!userStore.isLogin" class="guest">
      <view class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <text class="iip-empty__title">登录后查看我的券包</text>
      </view>
      <button class="iip-btn guest__btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
      <!-- 状态 pilltabs：可使用 / 已使用 / 已过期 / 已作废 -->
      <view class="tabs">
        <view class="iip-pilltabs">
          <view
            v-for="tab in tabs"
            :key="tab.value"
            class="iip-pilltabs__item"
            :class="{ 'is-on': activeStatus === tab.value }"
            @click="switchTab(tab.value)"
          >
            {{ tab.label }}
          </view>
        </view>
      </view>

      <!-- 骨架屏：未使用 tab 用 QR 卡骨架，其余用行卡骨架 -->
      <view v-if="loading && !list.length" class="list">
        <template v-if="activeStatus === '0'">
          <view class="iip-card skel" v-for="i in 2" :key="'qr' + i">
            <view class="iip-skel skel__head"></view>
            <view class="skel__body">
              <view class="iip-skel skel__qr"></view>
              <view class="skel__col">
                <view class="iip-skel iip-skel--row" style="width: 90%"></view>
                <view class="iip-skel iip-skel--row" style="width: 65%"></view>
                <view class="iip-skel iip-skel--row" style="width: 80%"></view>
              </view>
            </view>
          </view>
        </template>
        <template v-else>
          <view class="iip-card skel skel--rowcard" v-for="i in 3" :key="'row' + i">
            <view class="iip-skel iip-skel--row" style="width: 55%"></view>
            <view class="iip-skel iip-skel--row" style="width: 80%"></view>
          </view>
        </template>
      </view>

      <!-- 券列表 -->
      <view v-else class="list">
        <block v-for="item in list" :key="item.recordId">
          <!-- 未使用门票：QR 券卡 -->
          <view v-if="item.status === '0' && item.couponType === 'ticket'" class="qrt">
            <view class="qrt__head">
              <text class="qrt__name">{{ item.couponName }}</text>
              <text class="qrt__type">{{ couponTypeName(item.couponType) }}</text>
            </view>
            <view class="qrt__qrzone">
              <QrCode v-if="item.verifyCode" :text="item.verifyCode" :size="184" />
              <view class="qrt__qinfo">
                <view class="qrt__qrow">
                  <text class="qrt__qlabel">券码</text>
                  <text class="qrt__code">{{ formatVerifyCode(item.verifyCode) }}</text>
                  <view class="code-copy" @click="copyCode(item.verifyCode)">
                    <view class="code-copy__icon" :style="{ backgroundImage: icons.copy }"></view>
                    <text class="code-copy__text">复制</text>
                  </view>
                </view>
                <view class="qrt__qrow">
                  <text class="qrt__qlabel">有效期至</text>
                  <text class="qrt__qval">{{ fmtDate(item.validEndTime) }}</text>
                </view>
                <view class="qrt__qrow">
                  <text class="qrt__qlabel">核销方式</text>
                  <text class="qrt__qval">商户扫码 / 报券码核销</text>
                </view>
              </view>
            </view>
            <view class="qrt__foot">
              <text>兑换时间 {{ fmtDate(item.exchangeTime) }}</text>
              <text>持券账号 · {{ maskedName }}</text>
            </view>
          </view>

          <!-- 未使用其他券（满减/折扣/虚拟）：stub 券票卡 -->
          <view v-else-if="item.status === '0'" class="iip-ticket mini">
            <view class="iip-ticket__stub">
              <view class="mini__stub-icon" :style="{ backgroundImage: icons.coupon }"></view>
              <text class="mini__stub-text">{{ couponTypeName(item.couponType) }}</text>
            </view>
            <view class="iip-ticket__main">
              <view class="mini__head">
                <text class="mini__name">{{ item.couponName }}</text>
                <text class="iip-chip iip-chip--g">可用</text>
              </view>
              <view class="mini__line">有效期至 {{ fmtDate(item.validEndTime) }}</view>
              <view class="mini__code">
                <text class="mini__code-text">{{ formatVerifyCode(item.verifyCode) }}</text>
                <view class="code-copy" @click="copyCode(item.verifyCode)">
                  <view class="code-copy__icon" :style="{ backgroundImage: icons.copy }"></view>
                  <text class="code-copy__text">复制</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 已使用 / 已过期 / 已作废：简版行卡，整卡灰化 -->
          <view v-else class="iip-card done">
            <view class="done__head">
              <text class="done__name">{{ item.couponName }}</text>
              <text class="iip-chip iip-chip--gray">{{ statusLabel(item.status) }}</text>
            </view>
            <view class="done__sub">
              <text class="iip-chip iip-chip--gray">{{ couponTypeName(item.couponType) }}</text>
              <text class="done__time">
                {{ statusTimeText(item) }}
              </text>
            </view>
          </view>
        </block>

        <!-- 空状态：文案随 tab 变化 -->
        <view v-if="!loading && !list.length" class="iip-empty">
          <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
          <text class="iip-empty__title">{{ emptyText }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getMyCoupons } from '@/api/coupon.js'
import { redirectToLogin } from '@/common/request.js'
import { fmtDate, fmtMinute, couponTypeName } from '@/common/format.js'
import icons from '@/common/icons.js'
import QrCode from '@/components/QrCode.vue'

const userStore = useUserStore()

/** 状态 tab（value 对应后端 status：0未使用 1已使用 2已过期 3已作废） */
const tabs = [
  { label: '可使用', value: '0' },
  { label: '已使用', value: '1' },
  { label: '已过期', value: '2' },
  { label: '已作废', value: '3' }
]

const activeStatus = ref('0')
const list = ref([])
const loading = ref(false)

/** 持券账号脱敏名：首字+*，无昵称显示「会员」（QR 券卡 footer 用） */
const maskedName = computed(() => {
  const nickname = userStore.member && userStore.member.nickname
  if (!nickname) {
    return '会员'
  }
  return String(nickname).slice(0, 1) + '*'
})

const emptyText = computed(() => {
  const map = { 0: '暂无未使用的券', 1: '暂无已使用的券', 2: '暂无已过期的券', 3: '暂无已作废的券' }
  return map[activeStatus.value]
})

/** 券实例状态文案 */
function statusLabel(status) {
  return { 1: '已使用', 2: '已过期', 3: '已作废' }[status] || '不可使用'
}

/** 已结束券的时间说明 */
function statusTimeText(item) {
  if (item.status === '1') {
    return '使用时间 ' + fmtMinute(item.verifyTime)
  }
  if (item.status === '3') {
    return '作废时间 ' + fmtMinute(item.voidTime)
  }
  return '过期时间 ' + fmtDate(item.validEndTime)
}

async function loadList() {
  if (!userStore.isLogin) {
    list.value = []
    return
  }
  loading.value = true
  try {
    list.value = await getMyCoupons(activeStatus.value)
  } catch (e) {
    // 错误提示已由 request 封装统一处理
  } finally {
    loading.value = false
  }
}

function switchTab(value) {
  if (activeStatus.value === value) {
    return
  }
  activeStatus.value = value
  list.value = []
  loadList()
}

/** 核销码分组展示（每 4 位一组） */
function formatVerifyCode(code) {
  if (!code) {
    return ''
  }
  return String(code).replace(/(.{4})/g, '$1 ').trim()
}

/**
 * 一键复制核销码。
 *
 * @param {string} code 核销码
 */
function copyCode(code) {
  uni.setClipboardData({
    data: code,
    success: () => {
      uni.showToast({ title: '核销码已复制', icon: 'success' })
    }
  })
}

function goLogin() {
  redirectToLogin()
}

onShow(() => {
  if (!userStore.isLogin) {
    list.value = []
    return
  }
  /* 刷新资料，保证券卡 footer 持券账号信息可用 */
  userStore.fetchProfile().catch(() => {})
  loadList()
})

onPullDownRefresh(async () => {
  await loadList()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.guest {
  padding: 96rpx 32rpx;
}
.guest__btn {
  margin: 0 64rpx;
}

/* 状态 pilltabs（视觉由全局 .iip-pilltabs 提供，这里只控制与列表间距） */
.tabs {
  padding: 24rpx 24rpx 0;
}

.list {
  padding: 24rpx;
}

/* QR 券卡：红渐变头 + qrzone + 虚线 footer（对齐原型 .qr-ticket） */
.qrt {
  margin-bottom: 24rpx;
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: 36rpx;
  overflow: hidden;
}
.qrt__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  /* 与全局 .iip-hero 同组品牌渐变停点 */
  background: linear-gradient(135deg, #b92c12, #d94a1e);
}
.qrt__name {
  flex: 1;
  min-width: 0;
  margin-right: 16rpx;
  font-size: var(--iip-fs-30);
  font-weight: 800;
  color: #ffffff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.qrt__type {
  flex-shrink: 0;
  font-size: var(--iip-fs-20);
  color: rgba(255, 255, 255, 0.85);
}
.qrt__qrzone {
  position: relative;
  display: flex;
  align-items: center;
  gap: 32rpx;
  padding: 36rpx 32rpx;
}
/* 两侧半圆缺口：页面底色圆贴边，由卡片 overflow:hidden 裁出半圆 */
.qrt__qrzone::before,
.qrt__qrzone::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background-color: var(--iip-color-bg);
  transform: translateY(-50%);
}
.qrt__qrzone::before {
  left: -17rpx;
}
.qrt__qrzone::after {
  right: -17rpx;
}
.qrt__qinfo {
  flex: 1;
  min-width: 0;
}
.qrt__qrow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 14rpx;
}
.qrt__qrow:first-child {
  margin-top: 0;
}
.qrt__qlabel {
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}
.qrt__qval {
  font-size: var(--iip-fs-24);
  font-weight: 600;
  color: var(--iip-color-ink);
}
.qrt__code {
  font-family: 'Courier New', monospace;
  font-size: var(--iip-fs-26);
  font-weight: 700;
  letter-spacing: 2rpx;
  color: var(--iip-color-ink);
}
.qrt__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  border-top: 1rpx dashed var(--iip-color-line);
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}

/* 复制核销码小按钮（QR 卡与券票卡共用） */
.code-copy {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 16rpx;
  border: 1rpx solid var(--iip-color-primary);
  border-radius: var(--iip-radius-pill);
}
.code-copy__icon {
  width: 22rpx;
  height: 22rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.code-copy__text {
  font-size: var(--iip-fs-20);
  color: var(--iip-color-primary);
}

/* 未使用其他券：stub 券票卡（容器为全局 .iip-ticket） */
.mini {
  margin-bottom: 24rpx;
}
.mini__stub-icon {
  width: 56rpx;
  height: 56rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.mini__stub-text {
  font-size: var(--iip-fs-22);
  font-weight: 600;
  color: var(--iip-color-gold);
}
.mini__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.mini__name {
  flex: 1;
  min-width: 0;
  margin-right: 12rpx;
  font-size: var(--iip-fs-28);
  font-weight: 600;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mini__line {
  margin-top: 10rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}
.mini__code {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
}
.mini__code-text {
  font-family: 'Courier New', monospace;
  font-size: var(--iip-fs-28);
  font-weight: 700;
  letter-spacing: 2rpx;
  color: var(--iip-color-ink);
}

/* 已使用/已过期：简版行卡，整卡灰化 */
.done {
  margin-bottom: 24rpx;
  padding: 26rpx 28rpx;
  filter: grayscale(0.6);
  opacity: 0.45;
}
.done__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.done__name {
  flex: 1;
  min-width: 0;
  margin-right: 12rpx;
  font-size: var(--iip-fs-28);
  font-weight: 600;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.done__sub {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.done__time {
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}

/* 骨架屏 */
.skel {
  margin-bottom: 24rpx;
  overflow: hidden;
}
.skel__head {
  height: 76rpx;
  border-radius: 0;
}
.skel__body {
  display: flex;
  gap: 32rpx;
  padding: 36rpx 32rpx;
}
.skel__qr {
  width: 180rpx;
  height: 180rpx;
  flex-shrink: 0;
}
.skel__col {
  flex: 1;
  min-width: 0;
}
.skel__col .iip-skel--row {
  margin-top: 16rpx;
}
.skel__col .iip-skel--row:first-child {
  margin-top: 0;
}
.skel--rowcard {
  padding: 26rpx 28rpx;
}
.skel--rowcard .iip-skel--row {
  margin-top: 16rpx;
}
.skel--rowcard .iip-skel--row:first-child {
  margin-top: 0;
}
</style>
