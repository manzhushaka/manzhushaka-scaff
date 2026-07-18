<template>
  <view class="page">
    <view class="list">
      <view class="record iip-card" v-for="item in list" :key="item.recordId">
        <view class="record__main">
          <view class="record__name">{{ item.couponName }}</view>
          <view class="record__line">券类型：{{ couponTypeName(item.couponType) }}</view>
          <view class="record__line record__code">核销码：{{ item.verifyCode }}</view>
        </view>
        <view class="record__side">
          <view class="record__time">{{ fmtMinute(item.verifyTime) }}</view>
          <view class="record__points">{{ item.pointsCost }} 积分</view>
        </view>
      </view>

      <view v-if="!loading && !list.length" class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <text>暂无核销记录</text>
      </view>
      <view v-if="list.length && finished" class="list__end">没有更多了</view>
      <view v-else-if="loading" class="list__end">加载中</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { getVerifyRecords } from '@/api/merchant.js'
import { fmtMinute, couponTypeName } from '@/common/format.js'
import icons from '@/common/icons.js'

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
    const res = await getVerifyRecords({ pageNum: pageNum.value, pageSize })
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
</script>

<style scoped>
.list {
  padding: 24rpx;
}
.record {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.record__main {
  flex: 1;
  min-width: 0;
}
.record__name {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--iip-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.record__line {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.record__code {
  font-family: 'Courier New', Courier, monospace;
}
.record__side {
  margin-left: 24rpx;
  flex-shrink: 0;
  text-align: right;
}
.record__time {
  font-size: 24rpx;
  color: var(--iip-text-secondary);
}
.record__points {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.list__end {
  text-align: center;
  padding: 32rpx 0;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
</style>
