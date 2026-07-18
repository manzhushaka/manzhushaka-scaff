<template>
  <view class="page">
    <!-- 游客态：hero 引导登录，不渲染按钮行与列表区 -->
    <view v-if="!userStore.isLogin" class="pts-hero">
      <view class="pts-hero__label">当前可用积分</view>
      <view class="pts-hero__guest">登录后查看积分</view>
      <button class="iip-btn pts-hero__login" @click="goLogin">去登录</button>
    </view>

    <template v-else>
      <!-- 积分 hero：页底上直接居中，不套卡 -->
      <view class="pts-hero">
        <view class="pts-hero__label">当前可用积分</view>
        <view class="pts-hero__num iip-num">
          {{ fmtThousands(userStore.availablePoints) }}<text class="pts-hero__unit">分</text>
        </view>
        <view v-if="hint" class="pts-hero__hint" @click="onHintTap">
          <template v-if="hint.type === 'lack'">
            再攒 <text class="pts-hero__hint-strong">{{ fmtThousands(hint.gap) }}</text> 分可兑「{{ hint.name }}」
          </template>
          <template v-else>
            你已可兑换「{{ hint.name }}」，<text class="pts-hero__hint-link">去商城看看 ›</text>
          </template>
        </view>
      </view>

      <!-- 双按钮行 -->
      <view class="actions">
        <button class="iip-btn actions__btn" @click="goUpload">上传发票赚分</button>
        <button class="iip-btn iip-btn--dark actions__btn" @click="goMall">去兑换</button>
      </view>

      <!-- 积分明细：章节标题 + 类型 pilltabs -->
      <view class="iip-sect sect">积分明细</view>
      <view class="iip-pilltabs pilltabs">
        <view
          v-for="tab in tabs"
          :key="tab.label"
          class="iip-pilltabs__item"
          :class="{ 'is-on': activeType === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>

      <view class="list">
        <!-- 首屏骨架：3 行 -->
        <view v-if="loading && !list.length" class="iip-card records">
          <view class="records__row" v-for="i in 3" :key="i">
            <view class="records__main">
              <view class="iip-skel iip-skel--row skel-title"></view>
              <view class="iip-skel iip-skel--row skel-sub"></view>
            </view>
            <view class="iip-skel iip-skel--row skel-amt"></view>
          </view>
        </view>

        <!-- 记录行卡：白卡内多行 -->
        <template v-else-if="list.length">
          <view class="iip-card records">
            <view class="records__row" v-for="item in list" :key="item.recordId">
              <view class="records__main">
                <view class="records__title">{{ recordTitle(item) }}</view>
                <view class="records__time">{{ fmtMinute(item.createTime) }}</view>
              </view>
              <view class="records__side">
                <view class="records__points iip-num" :class="signClass(item)">
                  {{ signText(item) }}{{ fmtThousands(Math.abs(item.points)) }}
                </view>
                <view class="records__balance iip-num">余额 {{ fmtThousands(item.balanceAfter) }}</view>
              </view>
            </view>
          </view>
          <view v-if="loading || finished" class="iip-listfoot">
            {{ loading ? '加载中…' : '没有更多了' }}
          </view>
        </template>

        <!-- 空状态（adjust 后端无数据，命中时给专属文案） -->
        <view v-else class="iip-empty">
          <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
          <view class="iip-empty__title">{{ emptyText }}</view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getPointsRecords } from '@/api/points.js'
import { getMallCoupons } from '@/api/coupon.js'
import { redirectToLogin } from '@/common/request.js'
import { fmtMinute, fmtThousands, pointsBizName } from '@/common/format.js'
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
/** 商城在售券（用于 hero 提示行测算） */
const mallCoupons = ref([])

/**
 * 空状态文案：「调整」筛选后端暂无数据，命中时显示专属文案。
 *
 * @returns {string} 空状态文案
 */
const emptyText = computed(() => (activeType.value === 'adjust' ? '暂无调整记录' : '暂无积分记录'))

/**
 * hero 提示行：拉商城券列表测算。
 * 有券可兑（availablePoints >= 最低券价）时提示「你已可兑换「最便宜可兑券名」」；
 * 否则取 pointsCost 最小且 > availablePoints 的券，提示「再攒 X 分可兑「券名」」。
 *
 * @returns {{type: string, name: string, gap?: number}|null} 提示数据，无券或未登录为 null
 */
const hint = computed(() => {
  if (!userStore.isLogin || !mallCoupons.value.length) {
    return null
  }
  const sorted = mallCoupons.value.slice().sort((a, b) => a.pointsCost - b.pointsCost)
  const affordable = sorted.find((c) => c.pointsCost <= userStore.availablePoints)
  if (affordable) {
    return { type: 'affordable', name: affordable.couponName }
  }
  return { type: 'lack', name: sorted[0].couponName, gap: sorted[0].pointsCost - userStore.availablePoints }
})

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
 * 数量着色：获得金色、扣减灰色。
 *
 * @param {object} item PointsRecordResult
 * @returns {string} 样式类
 */
function signClass(item) {
  return signText(item) === '+' ? 'records__points--plus' : 'records__points--minus'
}

/**
 * 记录来源文案：bizType 前缀 + remark 为辅（发票核验·商户名 / 兑换·券名 / 活动调整）。
 *
 * @param {object} item PointsRecordResult
 * @returns {string} 来源文案
 */
function recordTitle(item) {
  const prefixes = {
    invoice_audit: '发票核验',
    coupon_exchange: '兑换',
    admin_adjust: '活动调整',
    point_expire: '积分过期'
  }
  const prefix = prefixes[item.bizType] || pointsBizName(item.bizType)
  return item.remark ? prefix + ' · ' + item.remark : prefix
}

/**
 * 加载流水（reset 时从头加载）。
 *
 * @param {boolean} reset 是否重置分页
 */
async function loadList(reset) {
  if (!userStore.isLogin || loading.value) {
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

/**
 * 拉商城在售券列表（hero 提示行测算用，失败降级为不显示提示行）。
 */
async function loadMall() {
  try {
    mallCoupons.value = await getMallCoupons()
  } catch (e) {
    mallCoupons.value = []
  }
}

function switchTab(value) {
  if (activeType.value === value) {
    return
  }
  activeType.value = value
  loadList(true)
}

function goUpload() {
  uni.navigateTo({ url: '/pages/invoice/upload' })
}

function goMall() {
  uni.switchTab({ url: '/pages/coupon/mall' })
}

function goLogin() {
  redirectToLogin()
}

/**
 * 提示行点击：仅在「已可兑换」态跳转商城 tab。
 */
function onHintTap() {
  if (hint.value && hint.value.type === 'affordable') {
    goMall()
  }
}

onShow(() => {
  if (!userStore.isLogin) {
    list.value = []
    mallCoupons.value = []
    return
  }
  userStore.fetchProfile().catch(() => {})
  loadMall()
  loadList(true)
})

onReachBottom(() => {
  if (userStore.isLogin && !finished.value) {
    loadList(false)
  }
})

onPullDownRefresh(async () => {
  if (userStore.isLogin) {
    await Promise.all([loadList(true), loadMall(), userStore.fetchProfile().catch(() => {})])
  }
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.page {
  padding-bottom: var(--iip-sp-48);
}

/* 积分 hero：页底上直接居中，不套卡 */
.pts-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--iip-sp-48) var(--iip-sp-32) var(--iip-sp-32);
}
.pts-hero__label {
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}
.pts-hero__num {
  margin-top: var(--iip-sp-16);
  font-size: var(--iip-fs-92);
  font-weight: 900;
  line-height: 1.1;
  color: var(--iip-color-ink);
}
.pts-hero__unit {
  margin-left: 12rpx;
  font-size: var(--iip-fs-28);
  font-weight: 700;
  color: var(--iip-color-gold);
}
.pts-hero__hint {
  margin-top: var(--iip-sp-16);
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}
.pts-hero__hint-strong,
.pts-hero__hint-link {
  font-weight: 700;
  color: var(--iip-color-gold);
}
.pts-hero__guest {
  margin-top: var(--iip-sp-24);
  font-size: var(--iip-fs-36);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.pts-hero__login {
  margin: var(--iip-sp-32) 0 0;
}

/* 双按钮行 */
.actions {
  display: flex;
  gap: 20rpx;
  padding: 0 var(--iip-sp-32);
}
.actions__btn {
  flex: 1;
  margin: 0;
  padding-left: 0;
  padding-right: 0;
}

/* 章节标题 + 类型 pilltabs */
.sect {
  margin: var(--iip-sp-32) var(--iip-sp-32) 0;
}
.pilltabs {
  margin-top: var(--iip-sp-24);
  padding: 0 var(--iip-sp-32);
}

/* 记录行卡：白卡内多行 */
.list {
  margin: var(--iip-sp-24) var(--iip-sp-32) 0;
}
.records {
  overflow: hidden;
}
.records__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx var(--iip-sp-32);
  border-bottom: 1rpx solid var(--iip-color-line);
}
.records__row:last-child {
  border-bottom: none;
}
.records__main {
  flex: 1;
  min-width: 0;
}
.records__title {
  font-size: var(--iip-fs-28);
  font-weight: 600;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.records__time {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-faint);
}
.records__side {
  margin-left: var(--iip-sp-24);
  text-align: right;
  flex-shrink: 0;
}
.records__points {
  font-size: var(--iip-fs-32);
  font-weight: 800;
}
.records__points--plus {
  color: var(--iip-color-gold);
}
.records__points--minus {
  color: var(--iip-color-text-secondary);
}
.records__balance {
  margin-top: 8rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-faint);
}

/* 骨架行 */
.skel-title {
  width: 280rpx;
}
.skel-sub {
  width: 200rpx;
  margin-top: 16rpx;
}
.skel-amt {
  width: 120rpx;
  margin-left: var(--iip-sp-24);
  flex-shrink: 0;
}
</style>
