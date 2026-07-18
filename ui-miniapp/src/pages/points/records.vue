<template>
  <view class="page">
    <!-- 可用积分大数字卡片 -->
    <view class="summary">
      <view class="summary__label">可用积分</view>
      <view class="summary__value">{{ userStore.availablePoints }}</view>
    </view>

    <!-- 类型筛选 -->
    <view class="iip-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.label"
        class="iip-tabs__item"
        :class="{ 'is-active': activeType === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 流水列表 -->
    <view class="list">
      <view class="record iip-card" v-for="item in list" :key="item.recordId">
        <view class="record__main">
          <view class="record__biz">{{ pointsBizName(item.bizType) }}</view>
          <view class="record__time">{{ fmtMinute(item.createTime) }}</view>
          <view class="record__remark" v-if="item.remark">{{ item.remark }}</view>
        </view>
        <view class="record__side">
          <view class="record__points" :class="signClass(item)">
            {{ signText(item) }}{{ Math.abs(item.points) }}
          </view>
          <view class="record__balance">余额 {{ item.balanceAfter }}</view>
        </view>
      </view>

      <view v-if="!loading && !list.length" class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <text>暂无积分流水</text>
      </view>
      <view v-if="list.length && finished" class="list__end">没有更多了</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getPointsRecords } from '@/api/points.js'
import { fmtMinute, pointsBizName } from '@/common/format.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

const tabs = [
  { label: '全部', value: '' },
  { label: '获得', value: 'earn' },
  { label: '消费', value: 'consume' },
  { label: '过期', value: 'expire' },
  { label: '调整', value: 'adjust' }
]

const activeType = ref('')
const list = ref([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const finished = ref(false)

/**
 * 变动数量正负号。
 * earn 为正；consume/expire 为负；adjust 以后端返回的 points 符号为准
 * （当前调整落库为 earn/consume + bizType=admin_adjust，此处做防御处理）。
 *
 * @param {object} item PointsRecordResult
 * @returns {string} '+' 或 '-'
 */
function signText(item) {
  if (item.changeType === 'earn') {
    return '+'
  }
  if (item.changeType === 'consume' || item.changeType === 'expire') {
    return '-'
  }
  return item.points >= 0 ? '+' : '-'
}

/**
 * 数量着色：获得绿、扣减红。
 *
 * @param {object} item PointsRecordResult
 * @returns {string} 样式类
 */
function signClass(item) {
  return signText(item) === '+' ? 'record__points--plus' : 'record__points--minus'
}

/**
 * 加载流水（reset 时从头加载）。
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
    const res = await getPointsRecords({
      pageNum: pageNum.value,
      pageSize,
      changeType: activeType.value || undefined
    })
    total.value = res.total
    list.value = reset ? res.rows : list.value.concat(res.rows)
    finished.value = list.value.length >= res.total || res.rows.length < pageSize
    pageNum.value += 1
  } catch (e) {
    // 错误提示已由 request 封装统一处理
  } finally {
    loading.value = false
  }
}

function switchTab(value) {
  if (activeType.value === value) {
    return
  }
  activeType.value = value
  loadList(true)
}

onShow(() => {
  userStore.fetchProfile().catch(() => {})
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
</script>

<style scoped>
.summary {
  padding: 48rpx 32rpx;
  background-color: var(--iip-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.summary__label {
  color: rgba(255, 255, 255, 0.85);
  font-size: 24rpx;
}
.summary__value {
  color: #ffffff;
  font-size: 72rpx;
  font-weight: 700;
  line-height: 1.2;
}

.list {
  padding: 24rpx;
}
.record {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.record__main {
  flex: 1;
  min-width: 0;
}
.record__biz {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--iip-text);
}
.record__time {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--iip-text-muted);
}
.record__remark {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--iip-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.record__side {
  margin-left: 24rpx;
  text-align: right;
  flex-shrink: 0;
}
.record__points {
  font-size: 34rpx;
  font-weight: 700;
}
.record__points--plus {
  color: var(--iip-success);
}
.record__points--minus {
  color: var(--iip-danger);
}
.record__balance {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: var(--iip-text-muted);
}
.list__end {
  text-align: center;
  padding: 32rpx 0;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
</style>
