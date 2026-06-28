<template>
  <div class="dashboard">
    <!-- 欢迎区 -->
    <div class="welcome-panel">
      <div class="welcome-bg-glow" />
      <div class="welcome-content">
        <div class="welcome-text">
          <h2 class="welcome-greeting">
            {{ greeting }}，<span class="welcome-user">{{ userStore.nickName || userStore.name }}</span>
          </h2>
          <p class="welcome-sub">欢迎使用满招科技 · 后台管理系统</p>
        </div>
        <div class="welcome-actions">
          <el-button class="welcome-action-btn" @click="goRoute('/system/user')">
            <svg-icon icon-class="user" class="action-icon" />
            用户管理
          </el-button>
          <el-button class="welcome-action-btn" @click="goRoute('/system/role')">
            <svg-icon icon-class="peoples" class="action-icon" />
            角色管理
          </el-button>
          <el-button class="welcome-action-btn" @click="goRoute('/system/notice')">
            <svg-icon icon-class="message" class="action-icon" />
            通知公告
          </el-button>
        </div>
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="kpi-grid">
      <div class="kpi-card" v-for="item in kpiList" :key="item.label">
        <div class="kpi-icon-wrap" :class="'tone-' + item.tone">
          <svg-icon :icon-class="item.icon" class="kpi-icon" />
        </div>
        <div class="kpi-body">
          <span class="kpi-value">{{ item.value }}</span>
          <span class="kpi-label">{{ item.label }}</span>
        </div>
        <div class="kpi-trend" :class="'tone-' + item.tone">
          {{ item.trend }}
        </div>
      </div>
    </div>

    <!-- 内容区：左侧待办 + 右侧快捷入口 -->
    <el-row :gutter="18">
      <!-- 左侧：通知公告 & 待办 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <!-- 通知公告 -->
        <div class="ui-panel-card notice-card">
          <div class="panel-header">
            <div class="panel-header-left">
              <svg-icon icon-class="message" class="panel-header-icon" />
              <span>通知公告</span>
            </div>
            <el-button text type="primary" size="small" @click="goRoute('/system/notice')">
              查看更多
            </el-button>
          </div>
          <div class="panel-body">
            <div v-if="noticeList.length > 0">
              <div
                class="notice-item"
                v-for="item in noticeList"
                :key="item.noticeId"
              >
                <span class="notice-tag" :class="noticeTypeClass(item.noticeType)">{{ noticeTypeText(item.noticeType) }}</span>
                <span class="notice-title">{{ item.noticeTitle }}</span>
                <span class="notice-time">{{ item.createTime }}</span>
              </div>
            </div>
            <div v-else class="panel-empty">
              <svg-icon icon-class="message" class="empty-icon" />
              <span>暂无通知公告</span>
            </div>
          </div>
        </div>

        <!-- 最近动态 -->
        <div class="ui-panel-card recent-card">
          <div class="panel-header">
            <div class="panel-header-left">
              <svg-icon icon-class="log" class="panel-header-icon" />
              <span>最近动态</span>
            </div>
          </div>
          <div class="panel-body">
            <div class="dynamic-list">
              <div class="dynamic-item" v-for="(item, idx) in recentDynamics" :key="idx">
                <div class="dynamic-dot" :class="'tone-' + item.tone" />
                <div class="dynamic-content">
                  <span class="dynamic-text">{{ item.text }}</span>
                  <span class="dynamic-time">{{ item.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：快捷入口 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <div class="ui-panel-card quick-card">
          <div class="panel-header">
            <div class="panel-header-left">
              <svg-icon icon-class="skill" class="panel-header-icon" />
              <span>快捷入口</span>
            </div>
          </div>
          <div class="panel-body">
            <div class="quick-grid">
              <div
                class="quick-item"
                v-for="item in quickLinks"
                :key="item.route"
                @click="goRoute(item.route)"
              >
                <div class="quick-icon" :class="'tone-' + item.tone">
                  <svg-icon :icon-class="item.icon" class="quick-svg" />
                </div>
                <span class="quick-label">{{ item.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { listNotice } from "@/api/system/notice"

const router = useRouter()
const userStore = useUserStore()

// ---------- 问候语 ----------
const now = new Date()
const hour = now.getHours()
let greeting = '您好'
if (hour >= 6 && hour < 12) greeting = '早上好'
else if (hour >= 12 && hour < 14) greeting = '中午好'
else if (hour >= 14 && hour < 18) greeting = '下午好'
else greeting = '晚上好'

// ---------- 核心指标 ----------
const kpiList = ref([
  { label: '用户总数', value: '—', icon: 'user', tone: 'primary', trend: '' },
  { label: '角色总数', value: '—', icon: 'peoples', tone: 'success', trend: '' },
  { label: '通知公告', value: '—', icon: 'message', tone: 'warning', trend: '' },
  { label: '服务状态', value: '正常', icon: 'server', tone: 'accent', trend: '运行中' },
])

// ---------- 通知公告 ----------
const noticeList = ref([])

function loadNotice() {
  listNotice({ pageNum: 1, pageSize: 5 }).then(res => {
    if (res.code === 200) {
      noticeList.value = res.rows || []
      if (kpiList.value[2]) {
        kpiList.value[2].value = res.total || '—'
      }
      if (res.rows && res.rows.length > 0) {
        const total = res.total
        kpiList.value[2] = { ...kpiList.value[2], value: total || '—' }
      }
    }
  }).catch(() => {
    // 接口不可用时静默失败
  })
}

function noticeTypeClass(type) {
  return type === '1' ? 'tag-notice' : 'tag-warn'
}

function noticeTypeText(type) {
  return type === '1' ? '通知' : '公告'
}

// ---------- 最近动态 ----------
const recentDynamics = ref([
  { text: '欢迎使用满招科技后台管理系统', time: '—', tone: 'primary' },
  { text: '系统运行正常，所有服务在线', time: '—', tone: 'success' },
  { text: '建议定期修改密码以保障账户安全', time: '—', tone: 'warning' },
])

// ---------- 快捷入口 ----------
const quickLinks = [
  { label: '用户管理', route: '/system/user', icon: 'user', tone: 'primary' },
  { label: '角色管理', route: '/system/role', icon: 'peoples', tone: 'success' },
  { label: '菜单管理', route: '/system/menu', icon: 'tree-table', tone: 'warning' },
  { label: '通知公告', route: '/system/notice', icon: 'message', tone: 'accent' },
  { label: '服务监控', route: '/monitor/server', icon: 'server', tone: 'supplement' },
  { label: '操作日志', route: '/monitor/operlog', icon: 'log', tone: 'primary' },
  { label: '登录日志', route: '/monitor/logininfor', icon: 'logininfor', tone: 'success' },
  { label: '缓存监控', route: '/monitor/cache', icon: 'redis', tone: 'warning' },
]

// ---------- 路由跳转 ----------
function goRoute(path) {
  router.push(path)
}

// 加载数据
loadNotice()
</script>

<style lang="scss" scoped>
.dashboard {
  min-height: 100%;
}

// ============================================================
// 欢迎区
// ============================================================
.welcome-panel {
  position: relative;
  min-height: 160px;
  padding: 28px 28px;
  border-radius: var(--ui-radius-panel, 8px);
  background: linear-gradient(135deg, var(--ui-bg-sidebar) 0%, var(--ui-primary) 100%);
  overflow: hidden;
  margin-bottom: 18px;

  @media (max-width: 768px) {
    padding: 20px 18px;
  }
}

.welcome-bg-glow {
  position: absolute;
  top: -40%;
  right: -10%;
  width: 50%;
  height: 160%;
  background: radial-gradient(ellipse at center, rgba(255, 255, 255, 0.10) 0%, transparent 70%);
  pointer-events: none;
}

.welcome-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}

.welcome-text {
  .welcome-greeting {
    font-size: 22px;
    font-weight: 700;
    color: #ffffff;
    margin: 0 0 6px;

    @media (max-width: 768px) {
      font-size: 18px;
    }
  }

  .welcome-user {
    color: #e0f2fe;
  }

  .welcome-sub {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.72);
    margin: 0;
  }
}

.welcome-actions {
  display: flex;
  gap: 8px;

  @media (max-width: 768px) {
    flex-wrap: wrap;
  }
}

.welcome-action-btn {
  height: 36px;
  padding: 0 18px;
  font-size: 13px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  color: #ffffff;
  transition: background 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.26);
    color: #ffffff;
    border-color: rgba(255, 255, 255, 0.32);
  }

  .action-icon {
    width: 14px;
    height: 14px;
    margin-right: 4px;
    color: #ffffff;
  }
}

// ============================================================
// KPI 指标网格
// ============================================================
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 18px;

  @media (max-width: 991px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 480px) {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 96px;
  padding: 16px;
  border: 1px solid var(--ui-border, #d8e6ef);
  border-radius: var(--ui-radius-panel, 8px);
  background: var(--ui-bg-panel, #ffffff);
  box-shadow: var(--ui-shadow-panel, 0 12px 28px rgba(15, 59, 96, 0.10));
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: default;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 16px 36px rgba(15, 59, 96, 0.14);
  }
}

.kpi-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  flex-shrink: 0;
  background: var(--tone-bg);

  .kpi-icon {
    width: 22px;
    height: 22px;
    color: #ffffff;
  }
}

.kpi-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--ui-text-primary, #0f172a);
  line-height: 1.2;

  @media (max-width: 480px) {
    font-size: 24px;
  }
}

.kpi-label {
  font-size: 13px;
  color: var(--ui-text-secondary, #64748b);
  line-height: 1.4;
}

.kpi-trend {
  font-size: 12px;
  white-space: nowrap;
  align-self: flex-start;
  margin-top: 4px;
  color: var(--tone-color);
}

// ============================================================
// 面板通用
// ============================================================
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px;
  border-bottom: 1px solid var(--ui-border, #d8e6ef);
}

.panel-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ui-text-primary, #0f172a);
}

.panel-header-icon {
  width: 16px;
  height: 16px;
  color: var(--ui-primary, #0ea5e9);
}

.panel-body {
  padding: 14px 18px 16px;
}

.panel-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px 0;
  color: var(--ui-text-secondary, #64748b);
  font-size: 13px;

  .empty-icon {
    width: 36px;
    height: 36px;
    opacity: 0.40;
    color: var(--ui-text-secondary, #64748b);
  }
}

// ============================================================
// 通知公告
// ============================================================
.notice-card,
.recent-card,
.quick-card {
  border: 1px solid var(--ui-border, #d8e6ef);
  border-radius: var(--ui-radius-panel, 8px);
  background: var(--ui-bg-panel, #ffffff);
  box-shadow: var(--ui-shadow-panel, 0 12px 28px rgba(15, 59, 96, 0.10));
  margin-bottom: 18px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--ui-border, #d8e6ef);

  &:last-child {
    border-bottom: none;
  }
}

.notice-tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.tag-notice {
  background: color-mix(in srgb, var(--ui-primary, #0ea5e9) 14%, transparent);
  color: var(--ui-primary, #0ea5e9);
}

.tag-warn {
  background: color-mix(in srgb, var(--ui-warning, #f59e0b) 14%, transparent);
  color: var(--ui-warning, #f59e0b);
}

.notice-title {
  flex: 1;
  font-size: 13px;
  color: var(--ui-text-regular, #334155);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 12px;
  color: var(--ui-text-secondary, #64748b);
  flex-shrink: 0;
}

// ============================================================
// 最近动态
// ============================================================
.dynamic-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dynamic-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--ui-border, #d8e6ef);

  &:last-child {
    border-bottom: none;
  }
}

.dynamic-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 5px;
  background: var(--tone-color);
}

.dynamic-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-width: 0;
}

.dynamic-text {
  font-size: 13px;
  color: var(--ui-text-regular, #334155);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dynamic-time {
  font-size: 12px;
  color: var(--ui-text-secondary, #64748b);
  flex-shrink: 0;
  margin-left: 8px;
}

// ============================================================
// 快捷入口
// ============================================================
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid var(--ui-border, #d8e6ef);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;

  &:hover {
    background: var(--ui-primary-soft, #e0f2fe);
    transform: translateY(-1px);
  }
}

.quick-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  flex-shrink: 0;
  background: var(--tone-bg);

  .quick-svg {
    width: 18px;
    height: 18px;
    color: #ffffff;
  }
}

.quick-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ui-text-primary, #0f172a);
  line-height: 1.3;
}
</style>
