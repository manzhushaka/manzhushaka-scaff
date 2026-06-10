<template>
  <div class="dashboard-page">
    <section class="overview-grid">
      <a-card
        v-for="item in overviewMetrics"
        :key="item.title"
        class="page-card overview-card"
        :bordered="false"
      >
        <div class="overview-card__label">{{ item.title }}</div>
        <div class="overview-card__value">{{ item.value }}</div>
        <div class="overview-card__note">{{ item.note }}</div>
      </a-card>
    </section>

    <section class="summary-grid">
      <a-card class="page-card identity-card" :bordered="false">
        <div class="identity-card__header">
          <div>
            <div class="dashboard-kicker">当前身份</div>
            <div class="identity-card__title">欢迎回来</div>
            <div class="dashboard-intro">
              {{ authStore.profile?.nickname ?? '管理员' }}，当前会话已经接入菜单、权限、日志和系统配置能力。
            </div>
          </div>

          <div class="identity-card__meta">
            <a-tag color="arcoblue" bordered>{{ authStore.profile?.username ?? 'SYSTEM' }}</a-tag>
          </div>
        </div>

        <div class="identity-grid">
          <div v-for="item in identityItems" :key="item.label" class="identity-pill">
            <div class="identity-pill__label">{{ item.label }}</div>
            <div class="identity-pill__value" :class="{ 'code-text': item.code }">{{ item.value }}</div>
          </div>
        </div>

        <div class="identity-band">
          <div v-for="item in identityBandItems" :key="item.label" class="identity-band__item">
            <div class="identity-band__label">{{ item.label }}</div>
            <div class="identity-band__value">{{ item.value }}</div>
          </div>
        </div>
      </a-card>

      <a-card class="page-card reminder-card" :bordered="false">
        <template #title>
          <div class="reminder-card__title">系统提醒</div>
        </template>

        <div class="reminder-list">
          <div v-for="(item, index) in dashboardReminders" :key="item.title" class="reminder-item">
            <div class="reminder-item__index">{{ String(index + 1).padStart(2, '0') }}</div>
            <div>
              <div class="reminder-item__title">{{ item.title }}</div>
              <div class="reminder-item__desc">{{ item.description }}</div>
            </div>
          </div>
        </div>
      </a-card>
    </section>

    <a-card class="page-card quick-entry-panel" :bordered="false" title="常用入口">
      <div class="quick-entry-description">把高频系统维护页集中展示，减少来回寻找导航的成本。</div>

      <div v-if="quickEntryCards.length" class="quick-entry-grid">
        <div
          v-for="item in quickEntryCards"
          :key="item.title"
          class="quick-entry-card"
          :class="`quick-entry-card--${item.tone}`"
          role="button"
          tabindex="0"
          @click="goToEntry(item.path)"
          @keydown.enter.prevent="goToEntry(item.path)"
          @keydown.space.prevent="goToEntry(item.path)"
        >
          <div class="quick-entry-card__head">
            <div class="quick-entry-card__icon">
              <component :is="resolveQuickEntryIcon(item.title)" />
            </div>
            <a-tag bordered size="small">{{ item.category }}</a-tag>
          </div>

          <div class="quick-entry-card__title">{{ item.title }}</div>
          <div class="quick-entry-card__desc">{{ item.description }}</div>

          <div class="quick-entry-card__footer">
            <span class="quick-entry-card__badge code-text">{{ item.badge }}</span>
            <span class="quick-entry-card__link">进入</span>
          </div>
        </div>
      </div>

      <a-empty v-else description="当前账号暂无可见快捷入口" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue';
import { useRouter } from 'vue-router';
import { IconBook, IconMenu, IconSafe, IconSettings, IconUser } from '@arco-design/web-vue/es/icon';
import { useAuthStore } from '@/store/auth';
import {
  buildDashboardMetrics,
  dashboardQuickEntries,
  dashboardReminders,
  filterQuickEntriesByPermission,
  formatDashboardRoleCodes,
} from './dashboard-support';

const authStore = useAuthStore();
const router = useRouter();

const quickEntryCards = computed(() =>
  filterQuickEntriesByPermission(dashboardQuickEntries, authStore.permissions, authStore.menus),
);

const overviewMetrics = computed(() =>
  buildDashboardMetrics({
    menuCount: authStore.menus.length,
    permissionCount: authStore.permissions.length,
    roleCount: authStore.profile?.roleCodes?.length ?? 0,
    quickEntryCount: quickEntryCards.value.length,
  }),
);

const roleCodeText = computed(() => formatDashboardRoleCodes(authStore.profile?.roleCodes));

const identityItems = computed(() => [
  { label: '当前用户', value: authStore.profile?.nickname ?? '--', code: false },
  { label: '所属部门', value: authStore.profile?.deptName ?? '--', code: false },
  { label: '角色编码', value: roleCodeText.value, code: true },
]);

const identityBandItems = computed(() => [
  { label: '权限点', value: authStore.permissions.length },
  { label: '菜单挂载', value: authStore.menus.length },
  { label: '角色数', value: authStore.profile?.roleCodes?.length ?? 0 },
]);

const quickEntryIcons: Record<string, Component> = {
  用户管理: IconUser,
  角色管理: IconSafe,
  菜单管理: IconMenu,
  日志管理: IconBook,
};

function resolveQuickEntryIcon(title: string) {
  return quickEntryIcons[title] ?? IconSettings;
}

function goToEntry(path: string) {
  if (router.currentRoute.value.path === path) {
    return;
  }
  router.push(path);
}
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 18px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.overview-card {
  min-height: 152px;
}

.overview-card::before {
  content: '';
  position: absolute;
  inset: 0 auto auto 0;
  width: 100%;
  height: 4px;
  opacity: 0.95;
}

.overview-card:nth-child(1)::before {
  background: linear-gradient(90deg, #245bdb, #5f8dff);
}

.overview-card:nth-child(2)::before {
  background: linear-gradient(90deg, #127c74, #37b6a6);
}

.overview-card:nth-child(3)::before {
  background: linear-gradient(90deg, #b86a12, #f4a53a);
}

.overview-card:nth-child(4)::before {
  background: linear-gradient(90deg, #4a5a72, #7f90aa);
}

.overview-card__label {
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.overview-card__value {
  margin-top: 14px;
  color: var(--text-strong);
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.overview-card__note {
  margin-top: 12px;
  color: var(--text-body);
  font-size: 13px;
  line-height: 1.6;
}

.summary-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 1fr);
  gap: 18px;
}

.identity-card {
  background:
    radial-gradient(circle at right top, rgba(36, 91, 219, 0.12), transparent 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(244, 249, 255, 0.96));
}

.identity-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.identity-card__meta {
  flex-shrink: 0;
}

.identity-card__title {
  margin-top: 6px;
  color: var(--text-strong);
  font-size: 28px;
  font-weight: 700;
}

.dashboard-kicker {
  color: #73829a;
  font-size: 12px;
  font-weight: 700;
}

.dashboard-intro {
  max-width: 720px;
  margin-top: 10px;
  color: var(--text-body);
  font-size: 14px;
  line-height: 1.7;
}

.identity-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 20px;
}

.identity-pill {
  padding: 16px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(247, 250, 254, 0.92), rgba(241, 246, 253, 0.92));
}

.identity-pill__label {
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 600;
}

.identity-pill__value {
  margin-top: 10px;
  color: var(--text-strong);
  font-size: 16px;
  font-weight: 700;
  line-height: 1.5;
}

.identity-band {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
  padding: 14px 16px;
  border: 1px solid rgba(36, 91, 219, 0.08);
  border-radius: 18px;
  background: linear-gradient(90deg, rgba(237, 244, 255, 0.9), rgba(247, 250, 255, 0.95));
}

.identity-band__label {
  color: #5f6f86;
  font-size: 12px;
  font-weight: 600;
}

.identity-band__value {
  margin-top: 6px;
  color: var(--text-strong);
  font-size: 15px;
  font-weight: 700;
}

.reminder-card__title {
  color: var(--text-strong);
  font-weight: 700;
}

.reminder-list {
  display: grid;
  gap: 12px;
}

.reminder-item {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 14px;
  align-items: flex-start;
  padding: 14px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(249, 251, 255, 0.95), rgba(243, 247, 252, 0.92));
}

.reminder-item__index {
  display: grid;
  place-items: center;
  height: 52px;
  border-radius: 14px;
  background: rgba(36, 91, 219, 0.1);
  color: var(--primary);
  font-size: 16px;
  font-weight: 700;
}

.reminder-item__title {
  color: var(--text-strong);
  font-weight: 700;
}

.reminder-item__desc {
  margin-top: 6px;
  color: var(--text-body);
  font-size: 13px;
  line-height: 1.65;
}

.quick-entry-panel :deep(.arco-card-body) {
  display: grid;
  gap: 14px;
}

.quick-entry-description {
  color: var(--text-body);
  font-size: 14px;
  line-height: 1.6;
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.quick-entry-card {
  --entry-accent: #245bdb;
  --entry-soft: rgba(36, 91, 219, 0.12);
  position: relative;
  display: grid;
  gap: 12px;
  min-height: 176px;
  padding: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(245, 249, 255, 0.95));
  cursor: pointer;
  outline: none;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.quick-entry-card:hover,
.quick-entry-card:focus-visible {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--entry-accent) 26%, white);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.1);
}

.quick-entry-card__head,
.quick-entry-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.quick-entry-card__icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: var(--entry-soft);
  color: var(--entry-accent);
  font-size: 18px;
}

.quick-entry-card__title {
  color: var(--text-strong);
  font-size: 17px;
  font-weight: 700;
}

.quick-entry-card__desc {
  color: var(--text-body);
  font-size: 13px;
  line-height: 1.65;
}

.quick-entry-card__badge {
  color: var(--entry-accent);
  font-size: 12px;
  font-weight: 700;
}

.quick-entry-card__link {
  color: var(--entry-accent);
  font-size: 13px;
  font-weight: 700;
}

.quick-entry-card--blue {
  --entry-accent: #245bdb;
  --entry-soft: rgba(36, 91, 219, 0.12);
}

.quick-entry-card--teal {
  --entry-accent: #127c74;
  --entry-soft: rgba(18, 124, 116, 0.12);
}

.quick-entry-card--amber {
  --entry-accent: #b86a12;
  --entry-soft: rgba(184, 106, 18, 0.12);
}

.quick-entry-card--slate {
  --entry-accent: #4a5a72;
  --entry-soft: rgba(74, 90, 114, 0.12);
}

@media (max-width: 1200px) {
  .overview-grid,
  .quick-entry-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .overview-grid,
  .identity-grid,
  .identity-band,
  .quick-entry-grid {
    grid-template-columns: 1fr;
  }

  .identity-card__header,
  .reminder-item {
    grid-template-columns: 1fr;
  }

  .identity-card__header {
    flex-direction: column;
  }

  .identity-card__title {
    font-size: 24px;
  }
}
</style>
