<script>
export default {
  onLaunch() {
    // 应用启动：用户态由 store 初始化时从 storage 恢复，无需额外处理
  }
}
</script>

<style>
/* 全局视觉基线：暖米纸感底 + 赤陶红 + 墨黑 + 金（对齐 Kimi 原型 prototype.css） */
page {
  /* 色板 */
  --iip-color-primary: #c93418;
  --iip-color-primary-deep: #9c2410;
  --iip-color-gold: #c8891a;
  --iip-color-success: #2f7d50;
  --iip-color-info: #2b5f9e;
  --iip-color-ink: #221e18;
  --iip-color-text-secondary: #8d8478;
  --iip-color-text-faint: #c9c0b0;
  --iip-color-bg: #f6f1e6;
  --iip-color-surface: #ffffff;
  --iip-color-line: #eae1cf;
  --iip-color-cream: #fdf6e7;
  --iip-color-chip-green: #e7f2ea;
  --iip-color-chip-gold: #fbf0d9;
  --iip-color-chip-red: #fbe4de;
  --iip-color-chip-gray: #efede8;
  /* 字号阶梯 */
  --iip-fs-20: 20rpx;
  --iip-fs-22: 22rpx;
  --iip-fs-24: 24rpx;
  --iip-fs-26: 26rpx;
  --iip-fs-28: 28rpx;
  --iip-fs-30: 30rpx;
  --iip-fs-32: 32rpx;
  --iip-fs-36: 36rpx;
  --iip-fs-44: 44rpx;
  --iip-fs-56: 56rpx;
  --iip-fs-92: 92rpx;
  /* 间距阶梯 */
  --iip-sp-8: 8rpx;
  --iip-sp-16: 16rpx;
  --iip-sp-24: 24rpx;
  --iip-sp-32: 32rpx;
  --iip-sp-48: 48rpx;
  /* 圆角阶梯 + 胶囊 */
  --iip-radius-16: 16rpx;
  --iip-radius-24: 24rpx;
  --iip-radius-32: 32rpx;
  --iip-radius-pill: 999rpx;
  background-color: var(--iip-color-bg);
  color: var(--iip-color-ink);
  font-size: var(--iip-fs-28);
  line-height: 1.5;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}

view,
text,
input,
button,
scroll-view,
image {
  box-sizing: border-box;
}

/* 通用卡片：白底 / 1rpx 米线 / 32rpx 圆角 / 无阴影 */
.iip-card {
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-32);
}

/* 章节标题：4rpx×28rpx 红竖条 + 30rpx 800 粗标题 */
.iip-sect {
  display: flex;
  align-items: center;
  gap: var(--iip-sp-16);
  font-size: var(--iip-fs-30);
  font-weight: 800;
  color: var(--iip-color-ink);
}
.iip-sect::before {
  content: '';
  width: 4rpx;
  height: 28rpx;
  border-radius: 2rpx;
  background-color: var(--iip-color-primary);
}

/* 状态 chip：20rpx 字号 / 胶囊 / 600 粗 */
.iip-chip {
  display: inline-block;
  font-size: var(--iip-fs-20);
  font-weight: 600;
  line-height: 1.4;
  padding: 4rpx 16rpx;
  border-radius: var(--iip-radius-pill);
}
.iip-chip--g {
  background-color: var(--iip-color-chip-green);
  color: var(--iip-color-success);
}
.iip-chip--y {
  background-color: var(--iip-color-chip-gold);
  color: var(--iip-color-gold);
}
.iip-chip--r {
  background-color: var(--iip-color-chip-red);
  color: var(--iip-color-primary);
}
.iip-chip--gray {
  background-color: var(--iip-color-chip-gray);
  color: var(--iip-color-text-secondary);
}

/* 主按钮：红底白字 / 胶囊 / 700 粗 / 红色投影 */
.iip-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22rpx 52rpx;
  font-size: var(--iip-fs-28);
  font-weight: 700;
  line-height: 1.4;
  color: #ffffff;
  background-color: var(--iip-color-primary);
  border: none;
  border-radius: var(--iip-radius-pill);
  box-shadow: rgba(201, 52, 24, 0.28) 0 8rpx 24rpx;
}
.iip-btn::after {
  border: none;
}
.iip-btn:active {
  transform: scale(0.97);
}
/* 墨底白字 */
.iip-btn--dark {
  background-color: var(--iip-color-ink);
  box-shadow: none;
}
/* 白底红边红字 */
.iip-btn--ghost {
  background-color: var(--iip-color-surface);
  color: var(--iip-color-primary);
  border: 1rpx solid var(--iip-color-primary);
  box-shadow: none;
}
/* 禁用态 */
.iip-btn.is-disabled {
  background-color: #d8d2c4;
  color: #9a938a;
  border: none;
  box-shadow: none;
}

/* Hero 横幅：红渐变 + 两个半透明白圆装饰 */
.iip-hero {
  position: relative;
  overflow: hidden;
  border-radius: 40rpx;
  color: #ffffff;
  background: linear-gradient(160deg, #b92c12 0%, #d94a1e 55%, #e07b2a 100%);
}
.iip-hero::after {
  content: '';
  position: absolute;
  right: -60rpx;
  top: -60rpx;
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.iip-hero::before {
  content: '';
  position: absolute;
  right: 68rpx;
  bottom: -92rpx;
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.07);
}

/* 深色卡（B 端工作台 / 积分条）：墨底白字，金色强调 */
.iip-dark-card {
  background-color: var(--iip-color-ink);
  color: #ffffff;
  border-radius: 36rpx;
}
.iip-dark-card .iip-gold {
  color: #ffd98a;
}

/* pill 选项卡：白底灰字，选中 = 墨底白字 */
.iip-pilltabs {
  display: flex;
  gap: var(--iip-sp-16);
}
.iip-pilltabs__item {
  flex-shrink: 0;
  white-space: nowrap;
  padding: 16rpx 36rpx;
  font-size: var(--iip-fs-26);
  font-weight: 700;
  color: var(--iip-color-text-secondary);
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-pill);
}
.iip-pilltabs__item.is-on {
  color: #ffffff;
  background-color: var(--iip-color-ink);
  border-color: var(--iip-color-ink);
}

/* 券票卡：左浅金 stub + 右 4rpx 虚线分隔 + 上下半圆缺口 */
.iip-ticket {
  display: flex;
  position: relative;
  overflow: hidden;
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-32);
}
.iip-ticket__stub {
  flex: 0 0 192rpx;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 24rpx 12rpx;
  background-color: var(--iip-color-cream);
  border-right: 4rpx dashed var(--iip-color-line);
}
/* 缺口圆用页面底色 + 1rpx 边，模拟打孔 */
.iip-ticket__stub::before,
.iip-ticket__stub::after {
  content: '';
  position: absolute;
  right: -16rpx;
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background-color: var(--iip-color-bg);
  border: 1rpx solid var(--iip-color-line);
}
.iip-ticket__stub::before {
  top: -16rpx;
}
.iip-ticket__stub::after {
  bottom: -16rpx;
}
.iip-ticket__main {
  flex: 1;
  min-width: 0;
  padding: 26rpx 28rpx;
}

/* 居中空状态：图标 96rpx + 标题 + 副文案 */
.iip-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 96rpx 0;
}
.iip-empty__icon {
  width: 96rpx;
  height: 96rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  margin-bottom: 24rpx;
  opacity: 0.7;
}
.iip-empty__title {
  font-size: var(--iip-fs-28);
  font-weight: 600;
  color: var(--iip-color-ink);
}
.iip-empty__desc {
  margin-top: 8rpx;
  font-size: var(--iip-fs-24);
  color: var(--iip-color-text-secondary);
}

/* 骨架屏：浅金灰渐变呼吸条 */
.iip-skel {
  border-radius: 12rpx;
  background: linear-gradient(90deg, #eee6d4 25%, #f8f2e4 50%, #eee6d4 75%);
  background-size: 200% 100%;
  animation: iip-skel-breathe 1.4s ease-in-out infinite;
}
.iip-skel--row {
  height: 32rpx;
}
@keyframes iip-skel-breathe {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 列表页脚：居中 22rpx 灰字（"加载中 / 没有更多了"） */
.iip-listfoot {
  padding: 24rpx 0;
  text-align: center;
  font-size: var(--iip-fs-22);
  color: var(--iip-color-text-secondary);
}

/* 数字等宽（积分 / 金额 / 统计） */
.iip-num {
  font-variant-numeric: tabular-nums;
}

/* 菜单列表卡：行 flex / 图标 36rpx / 标题 28rpx 600 / 右侧 chevron */
.iip-menu {
  background-color: var(--iip-color-surface);
  border: 1rpx solid var(--iip-color-line);
  border-radius: var(--iip-radius-32);
  overflow: hidden;
}
.iip-menu__row {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 30rpx 32rpx;
  border-bottom: 1rpx solid var(--iip-color-line);
}
.iip-menu__row:last-child {
  border-bottom: none;
}
.iip-menu__icon {
  width: 36rpx;
  height: 36rpx;
  flex: 0 0 auto;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}
.iip-menu__title {
  font-size: var(--iip-fs-28);
  font-weight: 600;
  color: var(--iip-color-ink);
}
/* chevron 用边框旋转画，不用字符 */
.iip-menu__chev {
  margin-left: auto;
  width: 16rpx;
  height: 16rpx;
  border-top: 2rpx solid var(--iip-color-text-faint);
  border-right: 2rpx solid var(--iip-color-text-faint);
  transform: rotate(45deg);
}

/* 小程序风格：隐藏 scroll-view 组件的滚动条轨道（H5 下实际滚动盒是 uni-scroll-view 内层 div） */
uni-scroll-view .uni-scroll-view {
  scrollbar-width: none;
}
uni-scroll-view .uni-scroll-view::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

/* 可点元素按压反馈（配合 hover-class 使用） */
.iip-tap {
  opacity: 0.72;
}
.iip-tap-bg {
  background-color: #f3ecdc;
}
</style>
