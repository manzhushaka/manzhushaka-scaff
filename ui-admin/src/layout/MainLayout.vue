<template>
  <a-layout class="layout-shell">
    <a-layout-sider
      :width="264"
      hide-trigger
      breakpoint="lg"
      class="layout-sider"
      :style="{ padding: '18px 16px 16px' }"
    >
      <div class="brand-block">
        <div class="brand-mark">
          <span class="brand-mark-core"></span>
        </div>
        <div class="brand-copy">
          <div class="brand-title">manzhushaka 管理台</div>
          <div class="brand-subtitle">System Console</div>
        </div>
      </div>
      <a-menu
        class="sidebar-menu"
        theme="dark"
        :selected-keys="[selectedKey]"
        :default-open-keys="defaultOpenKeys"
        auto-open
        @menu-item-click="handleMenuClick"
      >
        <SidebarMenuItem
          v-for="menu in visibleMenus"
          :key="menu.id"
          :menu="menu"
          :resolve-icon="resolveIcon"
        />
      </a-menu>
    </a-layout-sider>
    <a-layout class="layout-main">
      <a-layout-header class="layout-header">
        <div class="header-copy">
          <a-breadcrumb class="header-breadcrumb">
            <a-breadcrumb-item>管理台</a-breadcrumb-item>
            <a-breadcrumb-item>{{ route.meta.title ?? '当前页面' }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <a-space size="large" class="header-actions">
          <a-dropdown trigger="click" position="br" @select="handleProfileSelect">
            <div class="profile-chip profile-chip--interactive">
              <div class="profile-avatar">{{ userInitial }}</div>
              <div class="profile-copy">
                <div class="profile-name">{{ authStore.profile?.nickname ?? '未登录' }}</div>
                <div class="profile-role">{{ authStore.profile?.roleCodes?.[0] ?? 'NO_ROLE' }}</div>
              </div>
              <icon-down class="profile-chip__arrow" />
            </div>
            <template #content>
              <a-doption value="logout">退出登录</a-doption>
            </template>
          </a-dropdown>
        </a-space>
      </a-layout-header>
      <a-layout-content class="layout-content">
        <div v-if="tabsStore.items.length" class="visited-tabs-shell">
          <div class="visited-tabs" role="tablist" aria-label="已打开页面">
            <div
              v-for="tab in tabsStore.items"
              :key="tab.path"
              class="visited-tab"
              :class="{ 'visited-tab--active': tab.path === selectedKey }"
              role="tab"
              :aria-selected="tab.path === selectedKey"
              tabindex="0"
              @click="handleTabClick(tab.path)"
              @keydown.enter.prevent="handleTabClick(tab.path)"
              @keydown.space.prevent="handleTabClick(tab.path)"
            >
              <span class="visited-tab__label">{{ tab.title }}</span>
              <button
                v-if="tab.closable"
                type="button"
                class="visited-tab__close"
                aria-label="关闭标签"
                @click.stop="handleTabClose(tab.path)"
              >
                ×
              </button>
            </div>
          </div>
        </div>
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import type { Component } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  IconDashboard,
  IconHistory,
  IconSettings,
  IconApps,
  IconDown,
} from '@arco-design/web-vue/es/icon';
import SidebarMenuItem from '@/components/SidebarMenuItem.vue';
import { resetDynamicRoutes } from '@/router';
import { extractSidebarMenus } from '@/router/dynamic';
import { useAuthStore } from '@/store/auth';
import { useTabsStore } from '@/store/tabs';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const tabsStore = useTabsStore();

const visibleMenus = computed(() => extractSidebarMenus(authStore.menus));
const selectedKey = computed(() => route.path);
const userInitial = computed(() => authStore.profile?.nickname?.slice(0, 1) ?? 'M');
const defaultOpenKeys = computed(() => {
  const keys: string[] = [];
  const walk = (items: typeof visibleMenus.value) => {
    items.forEach((item) => {
      if (item.children?.length) {
        keys.push(item.path);
        walk(item.children);
      }
    });
  };
  walk(visibleMenus.value);
  return keys;
});

const iconMap = {
  'icon-dashboard': IconDashboard,
  'icon-history': IconHistory,
  'icon-settings': IconSettings,
};

function resolveIcon(icon?: string): Component {
  if (!icon) {
    return IconApps;
  }
  return iconMap[icon as keyof typeof iconMap] ?? IconApps;
}

function handleMenuClick(path: string) {
  router.push(path);
}

function handleProfileSelect(value: string | number | Record<string, unknown> | undefined) {
  if (value === 'logout') {
    handleLogout();
  }
}

function handleTabClick(path: string) {
  if (path === route.path) {
    return;
  }
  tabsStore.setActive(path);
  router.push(path);
}

function handleTabClose(path: string) {
  if (tabsStore.items.length === 1 && path === route.path) {
    return;
  }
  const nextPath = tabsStore.close(path);
  if (path === route.path && nextPath && nextPath !== route.path) {
    router.push(nextPath);
  }
}

function shouldTrackTab() {
  return Boolean(route.meta.tab && route.meta.title);
}

function syncVisitedTab() {
  if (!shouldTrackTab()) {
    return;
  }
  tabsStore.visit({
    path: route.path,
    title: String(route.meta.title),
    closable: !route.meta.affix,
  });
}

function handleLogout() {
  authStore.logout();
  tabsStore.reset();
  resetDynamicRoutes();
  router.replace('/login');
}

watch(
  () => route.fullPath,
  () => {
    syncVisitedTab();
  },
  { immediate: true },
);
</script>

<style scoped>
.layout-shell {
  min-height: 100dvh;
  padding: 0 18px 0 0;
}

.layout-sider {
  min-height: 100dvh;
  border-radius: 0 0 24px 0;
  background: linear-gradient(180deg, #0d1b34 0%, #13264b 100%);
  box-shadow: 0 26px 70px rgba(8, 17, 38, 0.22);
}

.layout-sider :deep(.arco-layout-sider-children) {
  display: flex;
  flex-direction: column;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 6px 18px;
  color: #fff;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(117, 163, 255, 0.95), rgba(44, 107, 255, 0.96));
  box-shadow: 0 18px 32px rgba(40, 105, 255, 0.28);
}

.brand-mark-core {
  width: 18px;
  height: 18px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: -10px 10px 0 -4px rgba(255, 255, 255, 0.46);
}

.brand-copy {
  min-width: 0;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.72;
}

.sidebar-menu {
  flex: 1;
  padding: 12px 8px 8px;
  background: transparent;
}

.sidebar-menu :deep(.arco-menu-inner) {
  background: transparent;
}

.sidebar-menu :deep(.arco-menu-item),
.sidebar-menu :deep(.arco-menu-inline-header) {
  height: 46px;
  margin-bottom: 6px;
  border-radius: 14px;
  font-weight: 700;
  transition: background-color 180ms ease, color 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.sidebar-menu :deep(.arco-menu-item) {
  color: #eef4ff;
}

.sidebar-menu :deep(.arco-menu-item:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-menu :deep(.arco-menu-inner > .arco-menu-item) {
  color: #2a3850;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 18px rgba(8, 17, 38, 0.08);
}

.sidebar-menu :deep(.arco-menu-inner > .arco-menu-item:hover) {
  color: #17233c;
  background: #ffffff;
  border-color: rgba(36, 91, 219, 0.18);
  box-shadow: 0 14px 24px rgba(8, 17, 38, 0.12);
}

.sidebar-menu :deep(.arco-menu-inline-header),
.sidebar-menu :deep(.arco-menu-inline-header:hover) {
  color: #2a3850;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 18px rgba(8, 17, 38, 0.08);
}

.sidebar-menu :deep(.arco-menu-inline-header:hover) {
  border-color: rgba(36, 91, 219, 0.18);
  box-shadow: 0 14px 24px rgba(8, 17, 38, 0.12);
}

.sidebar-menu :deep(.arco-menu-inline-header.arco-menu-selected),
.sidebar-menu :deep(.arco-menu-inline-header.arco-menu-selected:hover) {
  color: #ffffff;
  background: linear-gradient(90deg, rgba(59, 126, 255, 0.96), rgba(87, 163, 255, 0.9));
  border-color: transparent;
  box-shadow: 0 14px 28px rgba(37, 94, 221, 0.26);
}

.sidebar-menu :deep(.arco-menu-inline-header .arco-icon),
.sidebar-menu :deep(.arco-menu-item .arco-icon),
.sidebar-menu :deep(.arco-menu-icon) {
  color: inherit;
}

.sidebar-menu :deep(.arco-menu-inline-header .arco-icon-down) {
  color: #516076;
}

.sidebar-menu :deep(.arco-menu-inline-header.arco-menu-selected .arco-icon-down) {
  color: rgba(255, 255, 255, 0.92);
}

.sidebar-menu :deep(.arco-menu-pop-header),
.sidebar-menu :deep(.arco-menu-pop-item),
.sidebar-menu :deep(.arco-menu-item .arco-menu-pop-header),
.sidebar-menu :deep(.arco-menu-item .arco-menu-pop-item) {
  color: #25324a;
}

.sidebar-menu :deep(.arco-menu-inline-content) {
  margin-top: 6px;
  padding-left: 12px;
  margin-left: 2px;
  border-left: 1px solid rgba(255, 255, 255, 0.12);
}

.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-item) {
  color: #2a3850;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 18px rgba(8, 17, 38, 0.08);
}

.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-item:hover) {
  color: #17233c;
  background: #ffffff;
  border-color: rgba(36, 91, 219, 0.18);
}

.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-selected),
.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-selected:hover) {
  color: #ffffff;
  background: linear-gradient(90deg, rgba(59, 126, 255, 0.96), rgba(87, 163, 255, 0.9));
  border-color: transparent;
  box-shadow: 0 14px 28px rgba(37, 94, 221, 0.26);
}

.sidebar-menu :deep(.arco-menu-selected),
.sidebar-menu :deep(.arco-menu-selected:hover) {
  color: #fff;
  background: linear-gradient(90deg, rgba(59, 126, 255, 0.92), rgba(87, 163, 255, 0.82));
  box-shadow: 0 14px 28px rgba(37, 94, 221, 0.28);
}

.sidebar-menu :deep(.arco-menu-pop-header),
.sidebar-menu :deep(.arco-menu-pop-item) {
  border-radius: 12px;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: auto;
  padding: 16px 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(247, 250, 255, 0.88));
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 26px;
  box-shadow: 0 14px 38px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(18px);
}

.header-copy {
  display: flex;
  align-items: center;
  min-width: 0;
  min-height: 38px;
  padding-left: 2px;
}

.header-breadcrumb {
  color: #8a97ac;
  font-size: 15px;
  line-height: 1.2;
  transform: translateY(1px);
}

.header-breadcrumb :deep(.arco-breadcrumb-item),
.header-breadcrumb :deep(.arco-breadcrumb-item-separator) {
  font-size: inherit;
}

.header-breadcrumb :deep(.arco-breadcrumb-item:last-child) {
  font-weight: 600;
}

.profile-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding: 8px 12px 8px 8px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(36, 91, 219, 0.1);
  border-radius: 999px;
}

.profile-chip--interactive {
  padding-right: 10px;
  cursor: pointer;
  transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.profile-chip--interactive:hover {
  border-color: rgba(36, 91, 219, 0.2);
  box-shadow: 0 14px 24px rgba(36, 91, 219, 0.08);
}

.profile-avatar {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(180deg, #2d74ff 0%, #1949b8 100%);
  border-radius: 50%;
  box-shadow: 0 12px 22px rgba(36, 91, 219, 0.22);
}

.profile-copy {
  min-width: 0;
}

.profile-name {
  color: #1a2740;
  font-size: 13px;
  font-weight: 700;
}

.profile-role {
  margin-top: 2px;
  color: #7a88a0;
  font-size: 11px;
  letter-spacing: 0.06em;
}

.profile-chip__arrow {
  flex: 0 0 auto;
  color: #7d8ba3;
  font-size: 12px;
}

:deep(.arco-dropdown-option) {
  min-width: 132px;
  font-weight: 600;
}

.layout-content {
  padding: 18px 0 0;
}

.visited-tabs-shell {
  margin-bottom: 18px;
  padding: 8px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(247, 250, 255, 0.9));
  border: 1px solid rgba(15, 23, 42, 0.05);
  border-radius: 24px;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.visited-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow-x: auto;
  scrollbar-width: none;
}

.visited-tabs::-webkit-scrollbar {
  display: none;
}

.visited-tab {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
  min-height: 42px;
  padding: 0 16px;
  color: #4c5d78;
  font-size: 14px;
  font-weight: 700;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 999px;
  cursor: pointer;
  transition: color 180ms ease, background-color 180ms ease, box-shadow 180ms ease;
}

.visited-tab:hover {
  color: #1f2b43;
  background: rgba(36, 91, 219, 0.07);
}

.visited-tab--active {
  color: #ffffff;
  background: linear-gradient(90deg, rgba(42, 104, 255, 0.96), rgba(64, 128, 255, 0.9));
  box-shadow: 0 14px 24px rgba(36, 91, 219, 0.22);
}

.visited-tab__label {
  white-space: nowrap;
}

.visited-tab__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  padding: 0;
  color: currentColor;
  background: transparent;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
  opacity: 0.72;
  transition: background-color 180ms ease, opacity 180ms ease;
}

.visited-tab__close:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.18);
}

.layout-main {
  margin-left: 18px;
  padding-top: 12px;
}

@media (max-width: 1024px) {
  .layout-shell {
    padding: 0 10px 0 0;
  }

  .layout-main {
    margin-left: 10px;
    padding-top: 10px;
  }

  .layout-header {
    flex-wrap: wrap;
    padding: 14px 18px;
  }
}

@media (max-width: 768px) {
  .layout-shell {
    padding: 0;
  }

  .layout-sider {
    border-radius: 0;
  }

  .layout-main {
    margin-left: 0;
    padding-top: 8px;
  }

  .layout-header {
    border-radius: 0;
  }

  .layout-content {
    padding-top: 14px;
  }

  .visited-tab {
    min-height: 40px;
    padding: 0 14px;
    font-size: 13px;
  }

  .header-actions {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
