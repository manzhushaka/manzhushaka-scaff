<template>
  <view class="qr-box">
    <canvas
      v-show="!failed"
      class="qr-box__canvas"
      :canvas-id="cid"
      :id="cid"
      :style="{ width: size + 'px', height: size + 'px' }"
    />
    <view v-if="failed" class="qr-box__fallback" :style="{ width: size + 'px', height: size + 'px' }">
      <slot name="fallback">
        <text class="qr-box__fallback-text">{{ text }}</text>
      </slot>
    </view>
  </view>
</template>

<script setup>
import { ref, watch, onMounted, nextTick, getCurrentInstance } from 'vue'
import qrcode from 'qrcode-generator'

const props = defineProps({
  /** 二维码内容（通常为券核销码） */
  text: {
    type: String,
    required: true
  },
  /** 画布边长，单位 px */
  size: {
    type: Number,
    default: 180
  }
})

/** 多实例冲突防护：每个实例独立 canvas-id */
const cid = 'qr_' + Math.random().toString(36).slice(2)

/** 绘制失败标记：true 时降级为白底占位 + 原文本 */
const failed = ref(false)

const instance = getCurrentInstance()

/** 深色模块色（墨黑，对齐设计 token --iip-color-ink） */
const DARK_COLOR = '#221e18'
/** 四周留白边（单位：码格数） */
const QUIET_CELLS = 2

/**
 * 生成二维码矩阵并逐格绘制到 canvas。
 * 兼容 H5 / 微信 / 支付宝：仅使用 uni.createCanvasContext 旧接口，不依赖 DOM。
 */
function draw() {
  if (!props.text) {
    failed.value = true
    return
  }
  try {
    const qr = qrcode(0, 'M')
    qr.addData(props.text)
    qr.make()
    const count = qr.getModuleCount()
    const total = count + QUIET_CELLS * 2
    const ctx = uni.createCanvasContext(cid, instance.proxy)
    ctx.setFillStyle('#ffffff')
    ctx.fillRect(0, 0, props.size, props.size)
    ctx.setFillStyle(DARK_COLOR)
    for (let row = 0; row < count; row++) {
      for (let col = 0; col < count; col++) {
        if (!qr.isDark(row, col)) {
          continue
        }
        /* 用取整后的相邻坐标差作宽高，避免码格间出现细缝 */
        const x = Math.round(((col + QUIET_CELLS) * props.size) / total)
        const y = Math.round(((row + QUIET_CELLS) * props.size) / total)
        const w = Math.round(((col + QUIET_CELLS + 1) * props.size) / total) - x
        const h = Math.round(((row + QUIET_CELLS + 1) * props.size) / total) - y
        ctx.fillRect(x, y, w, h)
      }
    }
    ctx.draw(false, () => {
      failed.value = false
    })
  } catch (e) {
    failed.value = true
  }
}

onMounted(() => {
  /* 等待 canvas 节点渲染完成后再绘制 */
  nextTick(() => {
    setTimeout(draw, 50)
  })
})

watch(
  () => props.text,
  () => {
    nextTick(draw)
  }
)
</script>

<style scoped>
/* 外层 8rpx 白边 + 1rpx 线框（对齐原型 .qr-ticket canvas） */
.qr-box {
  display: inline-block;
  padding: 8rpx;
  background-color: #ffffff;
  box-shadow: 0 0 0 1rpx #eae1cf;
  line-height: 0;
}
.qr-box__canvas {
  display: block;
}
.qr-box__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  border: 1rpx solid #eae1cf;
  padding: 16rpx;
  line-height: 1.4;
}
.qr-box__fallback-text {
  font-size: 24rpx;
  color: #221e18;
  word-break: break-all;
  text-align: center;
}
</style>
