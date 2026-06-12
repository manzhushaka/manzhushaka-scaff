<template>
  <a-layout class="layout-shell">
    <a-layout-sider
      :width="240"
      hide-trigger
      breakpoint="lg"
      class="layout-sider"
      :style="{ padding: '16px 12px 12px' }"
    >
      <div class="brand-block">
        <img v-if="platformStore.logoUrl" :src="platformStore.logoUrl" alt="平台 Logo" class="brand-logo" />
        <div v-else class="brand-mark">
          <span class="brand-mark-core"></span>
        </div>
        <div class="brand-copy">
          <div class="brand-title">{{ platformStore.platformName }}</div>
          <div class="brand-subtitle">{{ platformStore.platformSubtitle }}</div>
        </div>
      </div>
      <a-menu
        class="sidebar-menu"
        theme="dark"
        v-model:open-keys="openKeys"
        :selected-keys="[selectedKey]"
        @menu-item-click="handleMenuClick"
      >
        <SidebarMenuItem
          v-for="menu in visibleMenus"
          :key="menu.id"
          :menu="menu"
          :resolve-icon="resolveMenuIcon"
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
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { IconDown } from '@arco-design/web-vue/es/icon';
import SidebarMenuItem from '@/components/SidebarMenuItem.vue';
import { collectAncestorMenuPaths, collectExpandableMenuPaths } from '@/layout/sidebar-menu-state';
import { resolveMenuIcon } from '@/layout/menu-icons';
import { resetDynamicRoutes } from '@/router';
import { extractSidebarMenus } from '@/router/dynamic';
import { useAuthStore } from '@/store/auth';
import { usePlatformStore } from '@/store/platform';
import { useTabsStore } from '@/store/tabs';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const platformStore = usePlatformStore();
const tabsStore = useTabsStore();

const visibleMenus = computed(() => extractSidebarMenus(authStore.menus));
const selectedKey = computed(() => route.path);
const openKeys = ref<string[]>([]);
const userInitial = computed(() => authStore.profile?.nickname?.slice(0, 1) ?? 'M');
const expandableMenuKeys = computed(() => collectExpandableMenuPaths(visibleMenus.value));

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

async function handleLogout() {
  await authStore.logout();
  tabsStore.reset();
  resetDynamicRoutes();
  router.replace('/login');
}

watch(
  [expandableMenuKeys, selectedKey],
  ([currentMenuKeys, currentPath], [previousMenuKeys, previousPath]) => {
    const validMenuKeys = new Set(currentMenuKeys);
    const nextOpenKeys = openKeys.value.filter((key) => validMenuKeys.has(key));
    const shouldSyncAncestors =
      openKeys.value.length === 0 ||
      currentPath !== previousPath ||
      currentMenuKeys.toString() !== (previousMenuKeys ?? []).toString();

    if (shouldSyncAncestors) {
      for (const key of collectAncestorMenuPaths(visibleMenus.value, currentPath)) {
        if (validMenuKeys.has(key) && !nextOpenKeys.includes(key)) {
          nextOpenKeys.push(key);
        }
      }
    }

    openKeys.value = nextOpenKeys;
  },
  { immediate: true },
);

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
  --top-rail-height: 42px;
  --top-rail-shell-padding: 8px;
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
  gap: 12px;
  padding: 6px 4px 14px;
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

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 32px rgba(40, 105, 255, 0.18);
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
  flex: 1;
}

.brand-title {
  overflow: hidden;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.02em;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand-subtitle {
  margin-top: 3px;
  overflow: hidden;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-overflow: ellipsis;
  text-transform: uppercase;
  white-space: nowrap;
  opacity: 0.72;
}

.sidebar-menu {
  flex: 1;
  padding: 8px 4px 4px;
  background: transparent;
  --sidebar-menu-text: rgba(238, 244, 255, 0.82);
  --sidebar-menu-text-strong: #ffffff;
  --sidebar-menu-hover-bg: rgba(255, 255, 255, 0.06);
  --sidebar-menu-selected-bg: linear-gradient(90deg, rgba(59, 126, 255, 0.32), rgba(87, 163, 255, 0.18));
  --sidebar-menu-selected-border: rgba(117, 163, 255, 0.28);
  --sidebar-menu-divider: rgba(255, 255, 255, 0.08);
  --sidebar-submenu-shell: rgba(255, 255, 255, 0.045);
  --sidebar-submenu-shell-strong: rgba(255, 255, 255, 0.06);
}

.sidebar-menu :deep(.arco-menu-inner) {
  background: transparent;
}

.sidebar-menu :deep(.arco-menu-inner > .arco-menu-item),
.sidebar-menu :deep(.arco-menu-inner > .arco-menu-inline) {
  position: relative;
}

.sidebar-menu :deep(.arco-menu-inner > .arco-menu-item:not(:first-child)::before),
.sidebar-menu :deep(.arco-menu-inner > .arco-menu-inline:not(:first-child)::before) {
  content: '';
  position: absolute;
  top: -4px;
  left: 10px;
  right: 10px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--sidebar-menu-divider), transparent);
}

.sidebar-menu :deep(.arco-menu-item),
.sidebar-menu :deep(.arco-menu-inline-header) {
  height: 40px;
  margin-bottom: 2px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  color: var(--sidebar-menu-text);
  background: transparent;
  border: 1px solid transparent;
  box-shadow: none;
  transition: background-color 180ms ease, color 180ms ease, border-color 180ms ease;
}

.sidebar-menu :deep(.arco-menu-item),
.sidebar-menu :deep(.arco-menu-inline-header) {
  -webkit-user-select: none;
  user-select: none;
}

.sidebar-menu :deep(.arco-menu-item:hover),
.sidebar-menu :deep(.arco-menu-inline-header:hover) {
  color: var(--sidebar-menu-text-strong);
  background: var(--sidebar-menu-hover-bg);
  border-color: transparent;
}

.sidebar-menu :deep(.arco-menu-inner > .arco-menu-item.arco-menu-selected),
.sidebar-menu :deep(.arco-menu-item.arco-menu-selected),
.sidebar-menu :deep(.arco-menu-inline-header.arco-menu-selected),
.sidebar-menu :deep(.arco-menu-inner > .arco-menu-item.arco-menu-selected:hover),
.sidebar-menu :deep(.arco-menu-item.arco-menu-selected:hover),
.sidebar-menu :deep(.arco-menu-inline-header.arco-menu-selected:hover) {
  color: var(--sidebar-menu-text-strong);
  background: var(--sidebar-menu-selected-bg);
  border-color: var(--sidebar-menu-selected-border);
}

.sidebar-menu :deep(.arco-menu-inline-header .arco-icon),
.sidebar-menu :deep(.arco-menu-item .arco-icon),
.sidebar-menu :deep(.arco-menu-icon) {
  color: inherit;
}

.sidebar-menu :deep(.arco-menu-inline-header .arco-icon-down) {
  color: rgba(238, 244, 255, 0.56);
}

.sidebar-menu :deep(.arco-menu-inline-header.arco-menu-selected .arco-icon-down) {
  color: rgba(255, 255, 255, 0.92);
}

.sidebar-menu :deep(.arco-menu-pop-header),
.sidebar-menu :deep(.arco-menu-pop-item),
.sidebar-menu :deep(.arco-menu-item .arco-menu-pop-header),
.sidebar-menu :deep(.arco-menu-item .arco-menu-pop-item) {
  color: #25324a;
  -webkit-user-select: none;
  user-select: none;
}

.sidebar-menu :deep(.arco-menu-inline-content) {
  margin: 4px 0 8px;
  padding: 4px;
  background: var(--sidebar-submenu-shell);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
  transition:
    height 0.2s cubic-bezier(0.34, 0.69, 0.1, 1),
    margin 0.2s cubic-bezier(0.34, 0.69, 0.1, 1),
    padding 0.2s cubic-bezier(0.34, 0.69, 0.1, 1),
    border-width 0.2s cubic-bezier(0.34, 0.69, 0.1, 1),
    background-color 0.16s ease,
    box-shadow 0.16s ease,
    opacity 0.16s ease;
}

.sidebar-menu :deep(.arco-menu-inline-content.v-enter-from),
.sidebar-menu :deep(.arco-menu-inline-content.v-leave-to) {
  margin-top: 0;
  margin-bottom: 0;
  padding-top: 0;
  padding-right: 0;
  padding-bottom: 0;
  padding-left: 0;
  border-top: 0 none transparent;
  border-right: 0 none transparent;
  border-bottom: 0 none transparent;
  border-left: 0 none transparent;
  background: transparent;
  box-shadow: none;
  opacity: 0;
}

.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-item),
.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-inline-header) {
  height: 36px;
  margin-bottom: 0;
  border-radius: 9px;
  font-size: 13px;
  font-weight: 600;
}

.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-inline-content) {
  margin: 2px 0 4px;
  background: var(--sidebar-submenu-shell-strong);
  border-color: rgba(255, 255, 255, 0.05);
}

.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-item:hover) {
  color: var(--sidebar-menu-text-strong);
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-selected),
.sidebar-menu :deep(.arco-menu-inline-content .arco-menu-selected:hover) {
  color: var(--sidebar-menu-text-strong);
  background: rgba(59, 126, 255, 0.18);
  border-color: transparent;
}

.sidebar-menu :deep(.arco-menu-selected),
.sidebar-menu :deep(.arco-menu-selected:hover) {
  color: var(--sidebar-menu-text-strong);
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
  padding: var(--top-rail-shell-padding) 24px;
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
  min-height: var(--top-rail-height);
  padding-left: 2px;
}

.header-actions {
  min-height: var(--top-rail-height);
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
  min-height: var(--top-rail-height);
  padding: 0 12px 0 8px;
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
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  color: #1c57d6;
  font-size: 13px;
  font-weight: 700;
  background: linear-gradient(180deg, rgba(90, 145, 255, 0.18), rgba(42, 104, 255, 0.12));
  border: 1px solid rgba(42, 104, 255, 0.16);
  border-radius: 10px;
}

.profile-copy {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-name {
  color: #1a2740;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.profile-role {
  padding: 3px 8px;
  color: #4d6488;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: 0.08em;
  white-space: nowrap;
  background: rgba(42, 104, 255, 0.08);
  border: 1px solid rgba(42, 104, 255, 0.12);
  border-radius: 999px;
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
  padding: var(--top-rail-shell-padding);
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
  min-height: var(--top-rail-height);
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
    padding-right: 18px;
    padding-left: 18px;
  }
}

@media (max-width: 768px) {
  .layout-shell {
    --top-rail-height: 40px;
    --top-rail-shell-padding: 6px;
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

  .header-actions {
    flex-wrap: wrap;
    gap: 12px;
  }

  .profile-copy {
    gap: 8px;
  }

  .profile-name {
    font-size: 12px;
  }

  .profile-role {
    padding-right: 7px;
    padding-left: 7px;
    font-size: 9px;
  }

  .visited-tab {
    padding: 0 14px;
    font-size: 13px;
  }
}
</style>
