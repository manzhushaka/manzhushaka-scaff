<template>
  <view class="page">
    <!-- 品类 pilltabs 横滑：全部 + 按接口返回实际出现的品类动态生成 -->
    <scroll-view scroll-x class="tabs">
      <view class="iip-pilltabs tabs__inner">
        <view
          v-for="tab in categoryTabs"
          :key="tab.value"
          class="iip-pilltabs__item"
          :class="{ 'is-on': activeCategory === tab.value }"
          @click="switchCategory(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>
    </scroll-view>

    <view class="list">
      <!-- 骨架：首次加载 3 条券票卡 -->
      <template v-if="loading && !allList.length">
        <view class="iip-ticket skel-ticket" v-for="i in 3" :key="'skel' + i">
          <view class="iip-ticket__stub">
            <view class="iip-skel iip-skel--row skel-ticket__num"></view>
            <view class="iip-skel skel-ticket__note"></view>
          </view>
          <view class="iip-ticket__main">
            <view class="iip-skel iip-skel--row"></view>
            <view class="iip-skel iip-skel--row skel-row--mid"></view>
            <view class="iip-skel iip-skel--row skel-row--short"></view>
          </view>
        </view>
      </template>

      <!-- 券票卡 -->
      <template v-else-if="list.length">
        <view
          class="iip-ticket ticket"
          :class="{ 'ticket--soldout': isSoldOut(item) }"
          v-for="item in list"
          :key="item.couponId"
          @click="goDetail(item)"
          hover-class="iip-tap"
        >
          <view class="iip-ticket__stub">
            <view class="ticket__points">
              <text class="ticket__points-num iip-num">{{ fmtThousands(item.pointsCost) }}</text>
              <text class="ticket__points-unit">分</text>
            </view>
            <!-- CouponMallItemResult 暂无门市价字段，stub 小字统一为「积分兑换」 -->
            <text class="ticket__stub-note">积分兑换</text>
          </view>
          <view class="iip-ticket__main">
            <view class="ticket__name">{{ item.couponName }}</view>
            <view class="ticket__sponsor" v-if="sponsorChip(item)">
              <text class="iip-chip" :class="sponsorChip(item).chipClass">{{ sponsorChip(item).text }}</text>
            </view>
            <view class="ticket__meta" v-if="metaText(item)">{{ metaText(item) }}</view>
            <view class="ticket__bottom">
              <text class="ticket__stock iip-num">{{ stockText(item) }}</text>
              <button
                class="iip-btn ticket__exbtn"
                :class="{ 'is-disabled': isSoldOut(item) }"
                @click.stop="handleExchangeEntry(item)"
              >
                {{ isSoldOut(item) ? '已抢光' : '兑换' }}
              </button>
            </view>
          </view>
        </view>
      </template>

      <!-- 空状态 -->
      <view v-else-if="!loading" class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <view class="iip-empty__title">暂无可兑换的券</view>
        <view class="iip-empty__desc">换个品类看看，或下拉刷新</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getMallCoupons } from '@/api/coupon.js'
import { fmtThousands } from '@/common/format.js'
import { redirectToLogin } from '@/common/request.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

/** 品类 tab 文案映射（数组顺序即 tab 展示顺序；全部不传 category） */
const CATEGORY_TABS = [
  { value: 'general', label: '通用' },
  { value: 'scenic_ticket', label: '景区门票' },
  { value: 'hotel', label: '酒店券' },
  { value: 'dining', label: '餐饮券' },
  { value: 'flight_package', label: '机票+权益' },
  { value: 'duty_free', label: '免税周边' }
]

const allList = ref([])
const loading = ref(false)
const activeCategory = ref('')

/** 品类 tab：全部 + 接口返回中实际出现的品类 */
const categoryTabs = computed(() => {
  const present = new Set(allList.value.map((item) => item.category))
  const tabs = [{ value: '', label: '全部' }]
  CATEGORY_TABS.forEach((tab) => {
    if (present.has(tab.value)) {
      tabs.push(tab)
    }
  })
  return tabs
})

/** 当前品类下的券列表（全部 = 不过滤） */
const list = computed(() => {
  if (!activeCategory.value) {
    return allList.value
  }
  return allList.value.filter((item) => item.category === activeCategory.value)
})

/**
 * 是否售罄（remainStock -1 表示不限库存，仅 0 视为售罄）。
 *
 * @param {object} item CouponMallItemResult
 * @returns {boolean} 是否售罄
 */
function isSoldOut(item) {
  return item.remainStock === 0
}

/**
 * 库存文案（-1 不限，大于 999 显示 999+）。
 *
 * @param {object} item CouponMallItemResult
 * @returns {string} 库存文案
 */
function stockText(item) {
  if (item.remainStock === -1) {
    return '库存充足'
  }
  if (isSoldOut(item)) {
    return '已抢光'
  }
  if (item.remainStock > 999) {
    return '剩余 999+ 张'
  }
  return '剩余 ' + item.remainStock + ' 张'
}

/**
 * 有效期文案（fixed 为止期日期 yyyy.MM.dd 止，days 为兑后 N 天有效）。
 *
 * @param {object} item CouponMallItemResult
 * @returns {string} 有效期文案，无有效信息时返回空串
 */
function validText(item) {
  if (item.validType === 'days' && item.validDays) {
    return '兑后 ' + item.validDays + ' 天有效'
  }
  if (item.validEndTime) {
    return String(item.validEndTime).slice(0, 10).replace(/-/g, '.') + ' 止'
  }
  return ''
}

/**
 * meta 行：targetName ｜ 有效期 ｜ 每人限兑 N（perMemberLimit -1 不限则不显示）。
 *
 * @param {object} item CouponMallItemResult
 * @returns {string} meta 文案
 */
function metaText(item) {
  const parts = []
  if (item.targetName) {
    parts.push(item.targetName)
  }
  const valid = validText(item)
  if (valid) {
    parts.push(valid)
  }
  if (item.perMemberLimit !== -1 && item.perMemberLimit != null) {
    parts.push('每人限兑 ' + item.perMemberLimit)
  }
  return parts.join('｜')
}

/**
 * 赞助方 chip（bank 金色「银行赞助」、merchant 灰色「商户赞助」、platform 或无名称不展示）。
 *
 * @param {object} item CouponMallItemResult
 * @returns {object|null} { text, chipClass } 或 null
 */
function sponsorChip(item) {
  if (!item.sponsorName) {
    return null
  }
  if (item.sponsorType === 'bank') {
    return { text: '银行赞助·' + item.sponsorName, chipClass: 'iip-chip--y' }
  }
  if (item.sponsorType === 'merchant') {
    return { text: '商户赞助·' + item.sponsorName, chipClass: 'iip-chip--gray' }
  }
  return null
}

/**
 * 加载商城券列表（游客可浏览，失败时错误提示已由 request 封装统一处理）。
 */
async function loadList() {
  loading.value = true
  try {
    allList.value = await getMallCoupons()
    // 刷新后当前品类可能已无上架券，回退到全部
    if (activeCategory.value && !allList.value.some((item) => item.category === activeCategory.value)) {
      activeCategory.value = ''
    }
  } catch (e) {
    // 错误提示已由 request 封装统一处理
  } finally {
    loading.value = false
  }
}

/**
 * 切换品类（客户端过滤，不重复请求）。
 *
 * @param {string} value 品类值，空串表示全部
 */
function switchCategory(value) {
  activeCategory.value = value
}

function goDetail(item) {
  uni.navigateTo({ url: '/pages/coupon/detail?id=' + item.couponId })
}

/**
 * 券卡「兑换」按钮入口：售罄时仅进入详情查看；未登录先引导登录，已登录进入券详情走原兑换流程。
 *
 * @param {object} item CouponMallItemResult
 */
function handleExchangeEntry(item) {
  if (isSoldOut(item)) {
    goDetail(item)
    return
  }
  if (!userStore.isLogin) {
    redirectToLogin()
    return
  }
  goDetail(item)
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
/* 品类 pilltabs 横滑（滚动条由 App.vue 全局规则隐藏） */
.tabs {
  white-space: nowrap;
  padding: var(--iip-sp-24) var(--iip-sp-24) 0;
}
.tabs__inner {
  display: inline-flex;
  min-width: 100%;
}

.list {
  padding: var(--iip-sp-24);
}
.ticket {
  margin-bottom: var(--iip-sp-24);
}
.ticket--soldout {
  opacity: 0.55;
}

/* stub：积分大数字 + 灰色小字 */
.ticket__points {
  display: flex;
  align-items: baseline;
  color: var(--iip-color-primary);
}
.ticket__points-num {
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1.1;
}
.ticket__points-unit {
  margin-left: 4rpx;
  font-size: var(--iip-fs-20);
  font-weight: 700;
}
.ticket__stub-note {
  font-size: var(--iip-fs-20);
  color: var(--iip-color-text-secondary);
}

/* main：券名 / 赞助 chip / meta / 底行 */
.ticket__name {
  font-size: var(--iip-fs-30);
  font-weight: 800;
  color: var(--iip-color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ticket__sponsor {
  margin-top: 10rpx;
  display: flex;
}
.ticket__meta {
  margin-top: 10rpx;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ticket__bottom {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.ticket__stock {
  font-size: var(--iip-fs-22);
  font-weight: 600;
  color: var(--iip-color-gold);
}
.ticket__exbtn {
  flex-shrink: 0;
  padding: 12rpx 32rpx;
  font-size: var(--iip-fs-24);
  box-shadow: none;
}

/* 骨架券票卡 */
.skel-ticket {
  margin-bottom: var(--iip-sp-24);
}
.skel-ticket__num {
  width: 120rpx;
}
.skel-ticket__note {
  width: 88rpx;
  height: 22rpx;
}
.skel-ticket .iip-ticket__main .iip-skel + .iip-skel {
  margin-top: 16rpx;
}
.skel-row--mid {
  width: 80%;
}
.skel-row--short {
  width: 55%;
}
</style>
