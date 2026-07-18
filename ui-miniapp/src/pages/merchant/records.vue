<template>
  <view class="page">
    <!-- 时间范围选项卡：今天 / 近 7 天 / 近 30 天 / 全部 -->
    <view class="iip-pilltabs tabs">
      <view
        v-for="tab in tabs"
        :key="tab.label"
        class="iip-pilltabs__item"
        :class="{ 'is-on': tab.days === activeDays }"
        @click="switchTab(tab.days)"
      >
        {{ tab.label }}
      </view>
    </view>
    <view class="sync-note">数据实时同步活动监管端</view>

    <!-- 首屏骨架 -->
    <view v-if="loading && !list.length" class="iip-card skel">
      <view class="skel__row" v-for="i in 4" :key="i">
        <view class="skel__main">
          <view class="iip-skel iip-skel--row skel__name"></view>
          <view class="iip-skel iip-skel--row skel__meta"></view>
        </view>
        <view class="iip-skel iip-skel--row skel__side"></view>
      </view>
    </view>

    <!-- 核销记录白卡行 -->
    <view v-else-if="list.length" class="iip-card records">
      <view class="record" v-for="item in list" :key="item.recordId">
        <view class="record__main">
          <view class="record__name">{{ item.couponName }}</view>
          <view class="record__meta">
            <text class="iip-chip" :class="typeChipClass(item.couponType)">
              {{ couponTypeName(item.couponType) }}
            </text>
            <text class="record__code">{{ item.verifyCode }}</text>
          </view>
        </view>
        <view class="record__side">
          <view class="record__time">{{ fmtRecordTime(item.verifyTime) }}</view>
          <view class="record__points iip-num">{{ fmtThousands(item.pointsCost) }} 积分</view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="iip-empty">
      <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
      <view class="iip-empty__title">暂无核销记录</view>
      <view class="iip-empty__desc">当前时间范围内还没有核销</view>
    </view>

    <!-- 列表页脚 -->
    <view v-if="list.length" class="iip-listfoot">
      <text v-if="finished">没有更多了</text>
      <text v-else-if="loading">加载中…</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { getVerifyRecords } from '@/api/merchant.js'
import { couponTypeName, fmtThousands } from '@/common/format.js'
import icons from '@/common/icons.js'

/** 时间范围选项卡：days 对齐后端 records 接口参数（null 表示全部，不传 days） */
const tabs = [
  { label: '今天', days: 1 },
  { label: '近 7 天', days: 7 },
  { label: '近 30 天', days: 30 },
  { label: '全部', days: null }
]
const activeDays = ref(1)

const list = ref([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const finished = ref(false)

/**
 * 加载核销记录（reset 时从头加载，否则加载下一页）。
 *
 * @param {boolean} reset 是否重置分页
 */
async function loadList(reset) {
  if (loading.value) {
    return
  }
  if (reset) {
    pageNum.value = 1
    list.value = []
    finished.value = false
  }
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize }
    if (activeDays.value != null) {
      params.days = activeDays.value
    }
    const res = await getVerifyRecords(params)
    total.value = res.total
    list.value = reset ? res.rows : list.value.concat(res.rows)
    finished.value = list.value.length >= res.total || res.rows.length < pageSize
    pageNum.value += 1
  } catch (e) {
    // 错误提示已由 request 封装统一处理（如非商户账号）
  } finally {
    loading.value = false
  }
}

/**
 * 切换时间范围选项卡并重载列表（加载中不响应，避免分页错乱）。
 *
 * @param {number|null} days 时间范围天数
 */
function switchTab(days) {
  if (days === activeDays.value || loading.value) {
    return
  }
  activeDays.value = days
  loadList(true)
}

onShow(() => {
  loadList(true)
})

onReachBottom(() => {
  if (!finished.value) {
    loadList(false)
  }
})

onPullDownRefresh(async () => {
  await loadList(true)
  uni.stopPullDownRefresh()
})

/**
 * 记录时间：今天只显示 hh:mm，跨天显示 MM-dd hh:mm。
 *
 * @param {string} value 后端日期时间字符串（yyyy-MM-dd HH:mm:ss）
 * @returns {string} 展示用时间
 */
function fmtRecordTime(value) {
  if (!value) {
    return ''
  }
  const str = String(value)
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const today = now.getFullYear() + '-' + month + '-' + day
  if (str.slice(0, 10) === today) {
    return str.slice(11, 16)
  }
  return str.slice(5, 10) + ' ' + str.slice(11, 16)
}

/**
 * 券类型对应的 chip 颜色类。
 *
 * @param {string} type ticket/virtual/full_reduction/discount
 * @returns {string} iip-chip 变体类名
 */
function typeChipClass(type) {
  const classes = {
    ticket: 'iip-chip--g',
    virtual: 'iip-chip--y',
    full_reduction: 'iip-chip--r',
    discount: 'iip-chip--gray'
  }
  return classes[type] || 'iip-chip--gray'
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}

/* 选项卡允许换行，避免窄屏溢出 */
.tabs {
  flex-wrap: wrap;
  row-gap: 16rpx;
}

/* 监管同步提示 */
.sync-note {
  margin: 16rpx 4rpx 24rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}

/* 记录白卡：行内分隔线 */
.records {
  padding: 0 32rpx;
}
.record {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid var(--iip-color-line);
}
.record:last-child {
  border-bottom: none;
}
.record__main {
  flex: 1;
  min-width: 0;
}
.record__name {
  font-size: var(--iip-fs-28);
  font-weight: 800;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.record__meta {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.record__code {
  font-family: 'Courier New', Courier, monospace;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.record__side {
  margin-left: 24rpx;
  flex-shrink: 0;
  text-align: right;
}
.record__time {
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}
.record__points {
  margin-top: 12rpx;
  font-size: var(--iip-fs-24);
  font-weight: 700;
  color: var(--iip-color-gold);
}

/* 骨架：与记录行同构 */
.skel {
  padding: 12rpx 32rpx;
}
.skel__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid var(--iip-color-line);
}
.skel__row:last-child {
  border-bottom: none;
}
.skel__main {
  flex: 1;
  min-width: 0;
}
.skel__name {
  width: 55%;
}
.skel__meta {
  margin-top: 16rpx;
  width: 35%;
}
.skel__side {
  width: 120rpx;
  margin-left: 24rpx;
  flex-shrink: 0;
}
</style>
