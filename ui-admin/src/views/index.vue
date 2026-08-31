<template>
  <div class="dashboard">
    <div class="welcome-panel">
      <div class="welcome-content">
        <div class="welcome-text">
          <span class="welcome-kicker">工作台</span>
          <h2 class="welcome-greeting">
            {{ greeting }}，<span class="welcome-user">{{ userStore.nickName || userStore.name }}</span>
          </h2>
          <p class="welcome-sub">聚合用户、权限与运行状态，保持后台运营节奏清晰可控。</p>
        </div>
        <div class="welcome-actions">
          <a-button type="primary" class="welcome-action-btn is-primary" @click="goRoute('/userAuth/user')">
            <svg-icon icon-class="user" class="action-icon" />
            用户管理
          </a-button>
          <a-button class="welcome-action-btn" @click="goRoute('/userAuth/role')">
            <svg-icon icon-class="peoples" class="action-icon" />
            角色管理
          </a-button>
        </div>
      </div>
    </div>

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

    <div class="quick-card">
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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const now = new Date()
const hour = now.getHours()
let greeting = '您好'
if (hour >= 6 && hour < 12) greeting = '早上好'
else if (hour >= 12 && hour < 14) greeting = '中午好'
else if (hour >= 14 && hour < 18) greeting = '下午好'
else greeting = '晚上好'

const kpiList = ref([
  { label: '用户总数', value: '—', icon: 'user', tone: 'primary', trend: '' },
  { label: '角色总数', value: '—', icon: 'peoples', tone: 'success', trend: '' },
  { label: '服务状态', value: '正常', icon: 'server', tone: 'accent', trend: '运行中' },
])

const quickLinks = [
  { label: '用户管理', route: '/userAuth/user', icon: 'user', tone: 'primary' },
  { label: '角色管理', route: '/userAuth/role', icon: 'peoples', tone: 'success' },
  { label: '菜单管理', route: '/userAuth/menu', icon: 'tree-table', tone: 'warning' },
  { label: '宿主机监控', route: '/monitor/server', icon: 'server', tone: 'supplement' },
  { label: '统一日志', route: '/log/logCenter', icon: 'log', tone: 'primary' },
  { label: '缓存监控', route: '/monitor/cache', icon: 'redis', tone: 'warning' },
]

function goRoute(path) {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.dashboard {
  min-height: 100%;
  width: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
  box-sizing: border-box;

  @media (max-width: 1200px) {
    padding: 20px;
  }

  @media (max-width: 768px) {
    padding: 16px;
  }
}

.welcome-panel {
  position: relative;
  min-height: 118px;
  padding: 24px 28px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: none;
  overflow: hidden;
  margin-bottom: 20px;

  @media (max-width: 768px) {
    padding: 20px;
  }
}

.welcome-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}

.welcome-text {
  max-width: 620px;

  .welcome-kicker {
    display: block;
    margin-bottom: 12px;
    color: var(--ui-primary-active);
    font-family: "IBM Plex Mono", "SFMono-Regular", Consolas, monospace;
    font-size: 11px;
    font-weight: 600;
  }

  .welcome-greeting {
    font-size: 20px;
    font-weight: 700;
    color: var(--ui-text-primary);
    margin: 0 0 6px;
    line-height: 1.35;

    @media (max-width: 768px) {
      font-size: 20px;
    }
  }

  .welcome-user {
    color: var(--ui-primary-active);
  }

  .welcome-sub {
    font-size: 14px;
    color: var(--ui-text-secondary);
    margin: 0;
    line-height: 1.7;
  }
}

.welcome-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;

  @media (max-width: 768px) {
    flex-wrap: wrap;
  }
}

.welcome-action-btn {
  height: 36px;
  padding: 0 18px;
  font-size: 13px;
  border-radius: var(--ui-radius-control);
  background: var(--ui-bg-panel);
  border: 1px solid var(--ui-border);
  color: var(--ui-text-primary);
  transition: background var(--ui-transition-fast), transform var(--ui-transition-fast), border-color var(--ui-transition-fast);

  &:hover {
    background: var(--ui-primary-soft);
    color: var(--ui-primary-active);
    border-color: color-mix(in srgb, var(--ui-primary) 34%, var(--ui-border));
    transform: translateY(-1px);
  }

  &.is-primary {
    background: var(--ui-primary);
    border-color: var(--ui-primary);
    color: var(--ui-text-inverse);

    &:hover {
      background: var(--ui-primary-hover);
      border-color: var(--ui-primary-hover);
    }

    .action-icon {
      color: var(--ui-text-inverse);
    }
  }

  .action-icon {
    width: 14px;
    height: 14px;
    margin-right: 4px;
    color: currentColor;
  }
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 20px;

  @media (max-width: 1100px) {
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  }

  @media (max-width: 480px) {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

.kpi-card {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 106px;
  padding: 20px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: default;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--ui-shadow-panel-hover);
  }
}

.kpi-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--ui-radius-panel);
  flex-shrink: 0;
  background: var(--tone-bg);

  .kpi-icon {
    width: 22px;
    height: 22px;
    color: var(--tone-color);
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
  color: var(--ui-text-primary);
  line-height: 1.2;

  @media (max-width: 480px) {
    font-size: 24px;
  }
}

.kpi-label {
  font-size: 13px;
  color: var(--ui-text-secondary);
  line-height: 1.4;
}

.kpi-trend {
  font-size: 12px;
  white-space: nowrap;
  align-self: flex-start;
  margin-top: 4px;
  color: var(--tone-color);
}

.quick-card {
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: none;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 52px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--ui-divider);
  background: var(--ui-bg-panel-muted);
}

.panel-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ui-text-primary);
}

.panel-header-icon {
  width: 16px;
  height: 16px;
  color: var(--ui-primary);
}

.panel-body {
  padding: 20px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 58px;
  padding: 14px 16px;
  border-radius: var(--ui-radius-control);
  border: 1px solid var(--ui-border);
  background: var(--ui-bg-panel);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease, border-color 0.15s ease;

  &:hover {
    background: var(--ui-bg-hover);
    border-color: color-mix(in srgb, var(--ui-primary) 34%, var(--ui-border));
    transform: translateY(-1px);
  }
}

.quick-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--ui-radius-control);
  flex-shrink: 0;
  background: var(--tone-bg);

  .quick-svg {
    width: 18px;
    height: 18px;
    color: var(--tone-color);
  }
}

.quick-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ui-text-primary);
  line-height: 1.3;
}
</style>
