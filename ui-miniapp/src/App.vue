<script>
export default {
  onLaunch() {
    // 应用启动：用户态由 store 初始化时从 storage 恢复，无需额外处理
  }
}
</script>

<style>
/* 全局视觉基线：暖白底 + 主橙 + 深棕文字（主色对齐 ui-admin theme-tokens --ui-primary） */
page {
  --iip-primary: #ff6a2a;
  --iip-primary-deep: #e44d12;
  --iip-primary-soft: #fff0e8;
  --iip-bg: #faf8f5;
  --iip-panel: #ffffff;
  --iip-text: #3a2f28;
  --iip-text-secondary: #6f6357;
  --iip-text-muted: #9a8f83;
  --iip-border: #ece5dc;
  --iip-success: #1f8a5b;
  --iip-success-soft: #e6f4ed;
  --iip-danger: #c73515;
  --iip-danger-soft: #fbeae5;
  --iip-pending: #d97b29;
  --iip-pending-soft: #fdf1e3;
  background-color: var(--iip-bg);
  color: var(--iip-text);
  font-size: 28rpx;
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

/* 通用卡片 */
.iip-card {
  background-color: var(--iip-panel);
  border-radius: 16rpx;
  border: 1rpx solid var(--iip-border);
  padding: 28rpx;
}

/* 主按钮 */
.iip-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border-radius: 12rpx;
  font-size: 30rpx;
  background-color: var(--iip-primary);
  color: #ffffff;
  border: none;
}
.iip-btn::after {
  border: none;
}
.iip-btn.is-disabled {
  background-color: #d8d0c6;
  color: #ffffff;
}
.iip-btn--plain {
  background-color: var(--iip-primary-soft);
  color: var(--iip-primary-deep);
}
.iip-btn--small {
  height: 64rpx;
  padding: 0 32rpx;
  font-size: 26rpx;
  display: inline-flex;
}

/* 状态色签 */
.iip-tag {
  display: inline-flex;
  align-items: center;
  height: 44rpx;
  padding: 0 18rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
}
.iip-tag--pending {
  background-color: var(--iip-pending-soft);
  color: var(--iip-pending);
}
.iip-tag--approved {
  background-color: var(--iip-success-soft);
  color: var(--iip-success);
}
.iip-tag--rejected {
  background-color: var(--iip-danger-soft);
  color: var(--iip-danger);
}
.iip-tag--muted {
  background-color: #f1ede8;
  color: var(--iip-text-muted);
}

/* 空状态 */
.iip-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 96rpx 0;
  color: var(--iip-text-muted);
  font-size: 26rpx;
}
.iip-empty__icon {
  width: 120rpx;
  height: 120rpx;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  margin-bottom: 24rpx;
  opacity: 0.7;
}

/* tab 筛选条 */
.iip-tabs {
  display: flex;
  background-color: var(--iip-panel);
  border-bottom: 1rpx solid var(--iip-border);
}
.iip-tabs__item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: var(--iip-text-secondary);
  position: relative;
}
.iip-tabs__item.is-active {
  color: var(--iip-primary-deep);
  font-weight: 600;
}
.iip-tabs__item.is-active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 56rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background-color: var(--iip-primary);
}
</style>
