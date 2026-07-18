<template>
  <view class="page">
    <!-- 状态筛选 -->
    <view class="iip-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.label"
        class="iip-tabs__item"
        :class="{ 'is-active': activeStatus === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 我的券列表 -->
    <view class="list">
      <view
        class="coupon iip-card"
        :class="{ 'coupon--expired': activeStatus === '2' }"
        v-for="item in list"
        :key="item.recordId"
      >
        <view class="coupon__top">
          <text class="coupon__name">{{ item.couponName }}</text>
          <text class="iip-tag" :class="statusTag.cls">{{ statusTag.text }}</text>
        </view>
        <view class="coupon__sponsor" v-if="couponSponsorText(item.sponsorType, item.sponsorName)">
          <text class="coupon__sponsor-tag">
            {{ couponSponsorText(item.sponsorType, item.sponsorName) }}
          </text>
        </view>

        <!-- 未使用：核销码大字 + 复制 -->
        <template v-if="item.status === '0'">
          <view class="coupon__code-row">
            <text class="coupon__code">{{ formatVerifyCode(item.verifyCode) }}</text>
            <view class="coupon__copy" @click="copyCode(item.verifyCode)">
              <view class="coupon__copy-icon" :style="{ backgroundImage: icons.copy }"></view>
              <text class="coupon__copy-text">复制</text>
            </view>
          </view>
          <view class="coupon__line">兑换时间：{{ fmtMinute(item.exchangeTime) }}</view>
          <view class="coupon__line">
            有效期：{{ fmtDate(item.validStartTime) }} 至 {{ fmtDate(item.validEndTime) }}
          </view>
        </template>

        <!-- 已使用：核销时间 -->
        <template v-else-if="item.status === '1'">
          <view class="coupon__line">券类型：{{ couponTypeName(item.couponType) }}</view>
          <view class="coupon__line">兑换时间：{{ fmtMinute(item.exchangeTime) }}</view>
          <view class="coupon__line">核销时间：{{ fmtMinute(item.verifyTime) }}</view>
        </template>

        <!-- 已过期 -->
        <template v-else>
          <view class="coupon__line">券类型：{{ couponTypeName(item.couponType) }}</view>
          <view class="coupon__line">有效期至：{{ fmtDate(item.validEndTime) }}</view>
        </template>
      </view>

      <view v-if="!loading && !list.length" class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <text>{{ emptyText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyCoupons } from '@/api/coupon.js'
import { fmtDate, fmtMinute, couponTypeName, couponSponsorText } from '@/common/format.js'
import icons from '@/common/icons.js'

const tabs = [
  { label: '未使用', value: '0' },
  { label: '已使用', value: '1' },
  { label: '已过期', value: '2' }
]

const activeStatus = ref('0')
const list = ref([])
const loading = ref(false)

const statusTag = computed(() => {
  const map = {
    0: { text: '未使用', cls: 'iip-tag--approved' },
    1: { text: '已使用', cls: 'iip-tag--muted' },
    2: { text: '已过期', cls: 'iip-tag--rejected' }
  }
  return map[activeStatus.value]
})

const emptyText = computed(() => {
  const map = { 0: '暂无未使用的券', 1: '暂无已使用的券', 2: '暂无已过期的券' }
  return map[activeStatus.value]
})

async function loadList() {
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

onShow(() => {
  loadList()
})
</script>

<style scoped>
.list {
  padding: 24rpx;
}
.coupon {
  margin-bottom: 24rpx;
}
.coupon--expired {
  opacity: 0.55;
}
.coupon__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.coupon__sponsor {
  margin-top: 10rpx;
  display: flex;
}
.coupon__sponsor-tag {
  display: inline-flex;
  align-items: center;
  height: 36rpx;
  padding: 0 14rpx;
  border-radius: 8rpx;
  background-color: var(--iip-pending-soft);
  color: var(--iip-pending);
  font-size: 20rpx;
}
.coupon__name {
  flex: 1;
  margin-right: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: var(--iip-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.coupon__code-row {
  margin-top: 20rpx;
  padding: 24rpx;
  border-radius: 12rpx;
  background-color: var(--iip-primary-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.coupon__code {
  font-family: 'Courier New', Courier, monospace;
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  color: var(--iip-text);
}
.coupon__copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  margin-left: 24rpx;
}
.coupon__copy-icon {
  width: 40rpx;
  height: 40rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.coupon__copy-text {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--iip-primary-deep);
}
.coupon__line {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
</style>
