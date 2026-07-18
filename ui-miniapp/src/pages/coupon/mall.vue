<template>
  <view class="page">
    <!-- 未登录引导 -->
    <view v-if="!userStore.isLogin" class="guest">
      <view class="iip-empty">
        <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
        <text>登录后逛积分商城</text>
      </view>
      <button class="iip-btn guest__btn" @click="goLogin">去登录</button>
    </view>

    <!-- 券卡片列表 -->
    <template v-else>
      <!-- 品类分类 tab -->
      <scroll-view scroll-x class="category-tabs">
        <view
          v-for="tab in categoryTabs"
          :key="tab.label"
          class="category-tabs__item"
          :class="{ 'is-active': activeCategory === tab.value }"
          @click="switchCategory(tab.value)"
        >
          {{ tab.label }}
        </view>
      </scroll-view>

      <view class="list">
        <view
          class="coupon iip-card"
          :class="{ 'coupon--soldout': item.remainStock === 0 }"
          v-for="item in list"
          :key="item.couponId"
          @click="goDetail(item)"
        >
          <image
            v-if="item.coverImage"
            class="coupon__cover"
            :src="resolveFileUrl(item.coverImage)"
            mode="aspectFill"
          />
          <view v-else class="coupon__cover coupon__cover--plain">
            <view class="coupon__cover-icon" :style="{ backgroundImage: icons.ticket }"></view>
          </view>
          <view class="coupon__body">
            <view class="coupon__name">{{ item.couponName }}</view>
            <view class="coupon__tags">
              <text class="coupon__tag coupon__tag--category">{{ couponCategoryName(item.category) }}</text>
              <text
                v-if="couponSponsorText(item.sponsorType, item.sponsorName)"
                class="coupon__tag coupon__tag--sponsor"
              >
                {{ couponSponsorText(item.sponsorType, item.sponsorName) }}
              </text>
            </view>
            <view class="coupon__target" v-if="item.targetName">适用：{{ item.targetName }}</view>
            <view class="coupon__meta">
              <text class="coupon__stock">{{ stockText(item) }}</text>
              <text class="coupon__limit">{{ limitText(item) }}</text>
            </view>
            <view class="coupon__bottom">
              <view class="coupon__cost">
                <text class="coupon__points">{{ item.pointsCost }}</text>
                <text class="coupon__unit">积分</text>
              </view>
              <text v-if="item.remainStock === 0" class="coupon__soldout">已售罄</text>
            </view>
          </view>
        </view>

        <view v-if="!loading && !list.length" class="iip-empty">
          <view class="iip-empty__icon" :style="{ backgroundImage: icons.empty }"></view>
          <text>暂无可兑换的券</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getMallCoupons } from '@/api/coupon.js'
import { resolveFileUrl } from '@/common/config.js'
import { redirectToLogin } from '@/common/request.js'
import { couponCategoryName, couponSponsorText } from '@/common/format.js'
import icons from '@/common/icons.js'

const userStore = useUserStore()

/** 品类分类 tab（全部不传 category） */
const categoryTabs = [
  { label: '全部', value: '' },
  { label: '门票', value: 'scenic_ticket' },
  { label: '酒店', value: 'hotel' },
  { label: '餐饮', value: 'dining' },
  { label: '机票+', value: 'flight_package' },
  { label: '免税', value: 'duty_free' },
  { label: '通用', value: 'general' }
]

const list = ref([])
const loading = ref(false)
const activeCategory = ref('')

/**
 * 库存文案（remainStock -1 表示不限）。
 *
 * @param {object} item CouponMallItemResult
 * @returns {string} 库存文案
 */
function stockText(item) {
  if (item.remainStock === -1) {
    return '库存充足'
  }
  if (item.remainStock === 0) {
    return '已售罄'
  }
  return '剩余 ' + item.remainStock
}

/**
 * 限兑文案（perMemberLimit -1 表示不限）。
 *
 * @param {object} item CouponMallItemResult
 * @returns {string} 限兑文案
 */
function limitText(item) {
  if (item.perMemberLimit === -1) {
    return '不限兑'
  }
  return '每人限兑 ' + item.perMemberLimit + ' 张'
}

async function loadList() {
  if (!userStore.isLogin) {
    return
  }
  loading.value = true
  try {
    list.value = await getMallCoupons(activeCategory.value)
  } catch (e) {
    // 错误提示已由 request 封装统一处理
  } finally {
    loading.value = false
  }
}

/**
 * 切换品类 tab 并按品类重新请求列表（全部不传 category）。
 *
 * @param {string} value 品类值，空串表示全部
 */
function switchCategory(value) {
  if (activeCategory.value === value) {
    return
  }
  activeCategory.value = value
  loadList()
}

function goDetail(item) {
  if (item.remainStock === 0) {
    return
  }
  uni.navigateTo({ url: '/pages/coupon/detail?id=' + item.couponId })
}

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
.guest {
  padding: 96rpx 32rpx;
}
.guest__btn {
  margin: 0 64rpx;
}

/* 品类分类 tab（横向滑动，视觉对齐全局 iip-tabs） */
.category-tabs {
  white-space: nowrap;
  background-color: var(--iip-panel);
  border-bottom: 1rpx solid var(--iip-border);
}
.category-tabs__item {
  display: inline-block;
  padding: 24rpx 32rpx;
  font-size: 28rpx;
  color: var(--iip-text-secondary);
  position: relative;
}
.category-tabs__item.is-active {
  color: var(--iip-primary-deep);
  font-weight: 600;
}
.category-tabs__item.is-active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background-color: var(--iip-primary);
}

.list {
  padding: 24rpx;
}
.coupon {
  display: flex;
  margin-bottom: 24rpx;
  padding: 0;
  overflow: hidden;
}
.coupon--soldout {
  opacity: 0.55;
}
.coupon__cover {
  width: 220rpx;
  min-height: 220rpx;
  flex-shrink: 0;
}
.coupon__cover--plain {
  background-color: var(--iip-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}
.coupon__cover-icon {
  width: 88rpx;
  height: 88rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.coupon__body {
  flex: 1;
  padding: 24rpx;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.coupon__name {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--iip-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.coupon__tags {
  margin-top: 10rpx;
  display: flex;
  flex-wrap: wrap;
}
.coupon__tag {
  display: inline-flex;
  align-items: center;
  height: 36rpx;
  padding: 0 14rpx;
  margin-right: 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
}
.coupon__tag--category {
  background-color: var(--iip-primary-soft);
  color: var(--iip-primary-deep);
}
.coupon__tag--sponsor {
  background-color: var(--iip-pending-soft);
  color: var(--iip-pending);
}
.coupon__target {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--iip-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.coupon__meta {
  margin-top: 12rpx;
  display: flex;
  font-size: 22rpx;
  color: var(--iip-text-secondary);
}
.coupon__limit {
  margin-left: 24rpx;
}
.coupon__bottom {
  margin-top: auto;
  padding-top: 16rpx;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.coupon__points {
  font-size: 44rpx;
  font-weight: 700;
  color: var(--iip-primary-deep);
}
.coupon__unit {
  margin-left: 6rpx;
  font-size: 22rpx;
  color: var(--iip-text-muted);
}
.coupon__soldout {
  font-size: 24rpx;
  color: var(--iip-text-muted);
}
</style>
