<template>
  <view class="page">
    <template v-if="userStore.isLogin">
      <!-- 状态筛选 -->
      <view class="iip-pilltabs tabs">
        <view
          v-for="tab in tabs"
          :key="tab.label"
          class="iip-pilltabs__item"
          :class="{ 'is-on': activeStatus === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>

      <!-- 发票卡片列表 -->
      <view class="list">
        <view class="inv iip-card" v-for="item in list" :key="item.invoiceId">
          <image
            v-if="item.imageUrl"
            class="inv__thumb"
            :src="resolveFileUrl(item.imageUrl)"
            mode="aspectFill"
            @click="previewImage(item.imageUrl)"
          />
          <view v-else class="inv__ficon">
            <view class="inv__ficon-icon" :style="{ backgroundImage: icons.invoice }"></view>
          </view>
          <view class="inv__det">
            <view class="inv__name">{{ item.merchantName }}</view>
            <view class="inv__meta">{{ metaText(item) }}</view>
            <view v-if="item.status === '2' && item.auditRemark" class="inv__reject">
              驳回原因：{{ item.auditRemark }}
            </view>
          </view>
          <view class="inv__side">
            <view class="inv__pts iip-num" :class="{ 'inv__pts--none': !hasPoints(item) }">{{ pointsOf(item) }}</view>
            <view class="iip-chip" :class="statusMeta(item.status).cls">{{ statusMeta(item.status).text }}</view>
          </view>
        </view>

        <view v-if="!loading && !list.length" class="iip-empty">
          <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
          <text class="iip-empty__title">暂无发票记录</text>
          <text class="iip-empty__desc">上传发票审核通过后可获得积分</text>
        </view>
      </view>
    </template>

    <!-- 游客引导 -->
    <view v-else class="iip-empty">
      <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
      <text class="iip-empty__title">登录后查看我的发票</text>
      <text class="iip-empty__desc">上传发票审核通过后可获得积分</text>
      <button class="iip-btn guest__btn" @click="goLogin">去登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getInvoiceList } from '@/api/invoice.js'
import { redirectToLogin } from '@/common/request.js'
import { resolveFileUrl } from '@/common/config.js'
import { fmtDate } from '@/common/format.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

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
 * 状态 chip 映射（待审核金 / 核验通过绿 / 已驳回红）。
 *
 * @param {string} status 0待审核 1已通过 2已驳回
 * @returns {{text: string, cls: string}} 文案与样式类
 */
function statusMeta(status) {
  const map = {
    0: { text: '待审核', cls: 'iip-chip--y' },
    1: { text: '核验通过', cls: 'iip-chip--g' },
    2: { text: '已驳回', cls: 'iip-chip--r' }
  }
  return map[status] || { text: '未知', cls: 'iip-chip--gray' }
}

/**
 * 发票号遮罩：保留前 4 后 4，中间以 **** 代替。
 *
 * @param {string} no 发票号码
 * @returns {string} 遮罩后的号码
 */
function maskNo(no) {
  const s = String(no || '')
  if (!s) {
    return '—'
  }
  if (s.length <= 8) {
    return s
  }
  return s.slice(0, 4) + '****' + s.slice(-4)
}

/**
 * 行次级信息：开票日期 · 发票号遮罩。
 *
 * @param {object} item InvoiceResult
 * @returns {string} 次级信息文案
 */
function metaText(item) {
  const parts = []
  const date = fmtDate(item.invoiceDate)
  if (date) {
    parts.push(date)
  }
  parts.push('发票号 ' + maskNo(item.invoiceNo))
  return parts.join(' · ')
}

/**
 * 是否有已入账积分（仅核验通过且积分非空时展示 +N）。
 *
 * @param {object} item InvoiceResult
 * @returns {boolean} 是否展示积分
 */
function hasPoints(item) {
  return item.status === '1' && item.points !== null && item.points !== undefined
}

/**
 * 右侧积分文案：核验通过显示 +积分，其余显示 —。
 *
 * @param {object} item InvoiceResult
 * @returns {string} 积分文案
 */
function pointsOf(item) {
  return hasPoints(item) ? '+' + item.points : '—'
}

/**
 * 按当前状态加载发票列表（游客降级为空列表）。
 */
async function loadList() {
  if (!userStore.isLogin) {
    list.value = []
    return
  }
  loading.value = true
  try {
    list.value = await getInvoiceList(activeStatus.value || undefined)
  } catch (e) {
    // 错误提示已由 request 封装统一处理
  } finally {
    loading.value = false
  }
}

/**
 * 切换状态筛选。
 *
 * @param {string} value 状态值（空串为全部）
 */
function switchTab(value) {
  if (activeStatus.value === value) {
    return
  }
  activeStatus.value = value
  loadList()
}

/**
 * 点击缩略图预览发票大图。
 *
 * @param {string} imageUrl 发票图片相对地址
 */
function previewImage(imageUrl) {
  uni.previewImage({ urls: [resolveFileUrl(imageUrl)] })
}

/**
 * 跳转登录页。
 */
function goLogin() {
  redirectToLogin()
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
.tabs {
  padding: var(--iip-sp-24) var(--iip-sp-24) 0;
}

.list {
  padding: var(--iip-sp-24);
}

/* 发票行卡 */
.inv {
  display: flex;
  align-items: center;
  gap: var(--iip-sp-24);
  padding: var(--iip-sp-24);
  margin-bottom: var(--iip-sp-24);
}
.inv__thumb {
  width: 96rpx;
  height: 96rpx;
  flex: 0 0 auto;
  border-radius: var(--iip-radius-16);
}
.inv__ficon {
  width: 96rpx;
  height: 96rpx;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--iip-color-cream);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-16);
}
.inv__ficon-icon {
  width: 44rpx;
  height: 44rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.inv__det {
  flex: 1;
  min-width: 0;
}
.inv__name {
  font-size: var(--iip-fs-28);
  font-weight: 700;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.inv__meta {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}
.inv__reject {
  margin-top: var(--iip-sp-8);
  font-size: var(--iip-fs-22);
  color: var(--iip-color-primary);
}
.inv__side {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--iip-sp-8);
}
.inv__pts {
  font-size: var(--iip-fs-30);
  font-weight: 700;
  color: var(--iip-color-gold);
}
.inv__pts--none {
  color: var(--iip-color-text-faint);
}

/* 游客登录按钮 */
.guest__btn {
  margin-top: var(--iip-sp-32);
}
</style>
