<template>
  <view class="page">
    <!-- 加载骨架 -->
    <view v-if="loading" class="skel">
      <view class="iip-skel skel__hero"></view>
      <view class="iip-skel iip-skel--row skel__row"></view>
      <view class="iip-skel iip-skel--row skel__row"></view>
      <view class="iip-skel iip-skel--row skel__row skel__row--short"></view>
    </view>

    <template v-else>
      <!-- 活动头 -->
      <view class="iip-hero head">
        <view class="head__tag">活动规则</view>
        <view class="head__title">{{ activity ? activity.activityName : '发票积分平台' }}</view>
        <view class="head__date" v-if="activity">
          活动期：{{ fmtDateDot(activity.startTime) }} ~ {{ fmtDateDot(activity.endTime) }}
        </view>
        <view class="head__date" v-else>当前暂无进行中的活动，以下为平台通用规则</view>
      </view>

      <!-- 活动自定义规则文案（按换行分段渲染） -->
      <view v-if="paragraphs.length" class="iip-card body">
        <text class="body__para" v-for="(para, index) in paragraphs" :key="index">{{ para }}</text>
      </view>

      <!-- 平台通用规则 -->
      <template v-else>
        <view class="iip-card body">
          <view class="sect">
            <view class="sect__title">一、积分规则</view>
            <text class="sect__para">在活动商户消费并上传真实有效的发票，按发票面额与活动比例换算积分（1 元 = {{ ratioText }} 分，以活动公示为准）。</text>
            <text class="sect__para">红冲、作废、代开及重复上传的发票不计积分；发票抬头须与平台实名信息一致。</text>
          </view>
          <view class="sect">
            <view class="sect__title">二、兑换与核销</view>
            <text class="sect__para">积分可在积分商城兑换优惠券，各券所需积分以商城页展示为准。</text>
            <text class="sect__para">兑换券自兑换起 30 天内有效，逾期自动失效；到景区由闸机或工作人员扫码核销，实名使用。</text>
          </view>
          <view class="sect">
            <view class="sect__title">三、禁止行为</view>
            <text class="sect__para">不得虚构交易、套开发票或用于充值、储值等非即时消费；违者取消参与资格并追回已发优惠，依法移交线索。</text>
          </view>
        </view>
      </template>

      <!-- 底部公示说明 -->
      <view class="foot">参与商户与景区名单以活动公示为准</view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCurrentActivity } from '@/api/activity.js'

const activity = ref(null)
const loading = ref(true)

/** 活动规则文案按换行分段 */
const paragraphs = computed(() => {
  if (!activity.value || !activity.value.description) {
    return []
  }
  return String(activity.value.description)
    .split(/\n+/)
    .map((line) => line.trim())
    .filter((line) => line)
})

/** 积分比例展示（BigDecimal 序列化如 1.00，去尾零后展示） */
const ratioText = computed(() => {
  if (!activity.value || activity.value.pointsRatio == null) {
    return '1'
  }
  return String(parseFloat(activity.value.pointsRatio))
})

/**
 * 日期展示为 yyyy.MM.dd。
 *
 * @param {string} value 后端日期时间字符串
 * @returns {string} yyyy.MM.dd 或空串
 */
function fmtDateDot(value) {
  return value ? String(value).slice(0, 10).replace(/-/g, '.') : ''
}

/** 拉取当前活动；无活动或接口失败时降级为平台通用规则 */
async function loadData() {
  try {
    activity.value = await getCurrentActivity()
  } catch (e) {
    activity.value = null
  } finally {
    loading.value = false
  }
}

onShow(() => {
  loadData()
})
</script>

<style scoped>
.page {
  padding: 24rpx;
}

/* 加载骨架 */
.skel__hero {
  height: 260rpx;
  border-radius: 40rpx;
}
.skel__row {
  margin-top: 24rpx;
}
.skel__row--short {
  width: 60%;
}

/* 活动头 */
.head {
  padding: 40rpx;
}
.head__tag {
  display: inline-block;
  padding: 6rpx 20rpx;
  font-size: var(--iip-fs-22);
  letter-spacing: 2rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: var(--iip-radius-pill);
}
.head__title {
  margin-top: 20rpx;
  font-size: var(--iip-fs-44);
  font-weight: 900;
  letter-spacing: 2rpx;
  line-height: 1.3;
}
.head__date {
  display: inline-block;
  margin-top: 20rpx;
  padding-top: 16rpx;
  font-size: var(--iip-fs-22);
  opacity: 0.85;
  border-top: 1rpx dashed rgba(255, 255, 255, 0.35);
}

/* 规则正文 */
.body {
  margin-top: 24rpx;
  padding: 32rpx;
}
.body__para {
  display: block;
  font-size: var(--iip-fs-26);
  line-height: 1.9;
  color: var(--iip-color-ink);
  margin-bottom: 16rpx;
}
.body__para:last-child {
  margin-bottom: 0;
}
.sect {
  margin-bottom: 32rpx;
}
.sect:last-child {
  margin-bottom: 0;
}
.sect__title {
  font-size: var(--iip-fs-28);
  font-weight: 800;
  color: var(--iip-color-ink);
  margin-bottom: 12rpx;
}
.sect__para {
  display: block;
  font-size: var(--iip-fs-26);
  line-height: 1.9;
  color: var(--iip-color-text-secondary);
  margin-bottom: 8rpx;
}
.sect__para:last-child {
  margin-bottom: 0;
}

/* 底部公示说明 */
.foot {
  padding: 28rpx 0 12rpx;
  text-align: center;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-faint);
}
</style>
