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

    <!-- 发票卡片列表 -->
    <view class="list">
      <view class="invoice-card iip-card" v-for="item in list" :key="item.invoiceId">
        <image
          v-if="item.imageUrl"
          class="invoice-card__thumb"
          :src="resolveFileUrl(item.imageUrl)"
          mode="aspectFill"
          @click="previewImage(item.imageUrl)"
        />
        <view v-else class="invoice-card__thumb invoice-card__thumb--plain">
          <view class="invoice-card__thumb-icon" :style="{ backgroundImage: icons.invoice }"></view>
        </view>
        <view class="invoice-card__body">
          <view class="invoice-card__top">
            <text class="invoice-card__merchant">{{ item.merchantName }}</text>
            <text class="iip-tag" :class="statusMeta(item.status).cls">{{ statusMeta(item.status).text }}</text>
          </view>
          <view class="invoice-card__line">发票号码：{{ item.invoiceNo }}</view>
          <view class="invoice-card__line" v-if="item.invoiceDate">
            开票日期：{{ fmtDate(item.invoiceDate) }}
          </view>
          <view class="invoice-card__bottom">
            <text class="invoice-card__amount">¥ {{ item.amount }}</text>
            <text class="invoice-card__points" v-if="item.status === '1'">+{{ item.points }} 积分</text>
          </view>
          <view class="invoice-card__reject" v-if="item.status === '2' && item.auditRemark">
            驳回原因：{{ item.auditRemark }}
          </view>
        </view>
      </view>

      <view v-if="!loading && !list.length" class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <text>暂无发票记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getInvoiceList } from '@/api/invoice.js'
import { resolveFileUrl } from '@/common/config.js'
import { fmtDate } from '@/common/format.js'
import icons from '@/common/icons.js'

const tabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: '0' },
  { label: '已通过', value: '1' },
  { label: '已驳回', value: '2' }
]

const activeStatus = ref('')
const list = ref([])
const loading = ref(false)

/**
 * 状态色签映射（待审核橙 / 已通过绿 / 已驳回红）。
 *
 * @param {string} status 0待审核 1已通过 2已驳回
 * @returns {{text: string, cls: string}} 文案与样式类
 */
function statusMeta(status) {
  const map = {
    0: { text: '待审核', cls: 'iip-tag--pending' },
    1: { text: '已通过', cls: 'iip-tag--approved' },
    2: { text: '已驳回', cls: 'iip-tag--rejected' }
  }
  return map[status] || { text: '未知', cls: 'iip-tag--muted' }
}

async function loadList() {
  loading.value = true
  try {
    list.value = await getInvoiceList(activeStatus.value || undefined)
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

function previewImage(imageUrl) {
  uni.previewImage({ urls: [resolveFileUrl(imageUrl)] })
}

onShow(() => {
  loadList()
})

onPullDownRefresh(async () => {
  await loadList()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.list {
  padding: 24rpx;
}

.invoice-card {
  display: flex;
  margin-bottom: 24rpx;
}
.invoice-card__thumb {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}
.invoice-card__thumb--plain {
  background-color: var(--iip-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}
.invoice-card__thumb-icon {
  width: 72rpx;
  height: 72rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.invoice-card__body {
  flex: 1;
  margin-left: 24rpx;
  min-width: 0;
}
.invoice-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.invoice-card__merchant {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--iip-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 16rpx;
}
.invoice-card__line {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
.invoice-card__bottom {
  margin-top: 12rpx;
  display: flex;
  align-items: baseline;
}
.invoice-card__amount {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--iip-text);
}
.invoice-card__points {
  margin-left: 20rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: var(--iip-success);
}
.invoice-card__reject {
  margin-top: 12rpx;
  padding: 12rpx 16rpx;
  border-radius: 8rpx;
  background-color: var(--iip-danger-soft);
  color: var(--iip-danger);
  font-size: 22rpx;
}
</style>
