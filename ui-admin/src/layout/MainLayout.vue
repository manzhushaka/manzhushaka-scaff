<template>
  <a-layout class="layout-shell">
    <a-layout-sider :width="240" collapsible breakpoint="lg">
      <div class="brand-block">
        <div class="brand-title">manzhushaka 管理台</div>
        <div class="brand-subtitle">Admin Scaffold V1</div>
      </div>
      <a-menu
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
    <a-layout>
      <a-layout-header class="layout-header">
        <div>
          <div class="header-title">{{ route.meta.title ?? '管理台' }}</div>
          <div class="header-subtitle">先交付可编译骨架，后续可直接接真实接口</div>
        </div>
        <a-space>
          <a-tag color="arcoblue">{{ authStore.profile?.nickname }}</a-tag>
          <a-button type="outline" @click="handleLogout">退出登录</a-button>
        </a-space>
      </a-layout-header>
      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  IconDashboard,
  IconHistory,
  IconSettings,
  IconApps,
} from '@arco-design/web-vue/es/icon';
import SidebarMenuItem from '@/components/SidebarMenuItem.vue';
import { resetDynamicRoutes } from '@/router';
import { extractSidebarMenus } from '@/router/dynamic';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const visibleMenus = computed(() => extractSidebarMenus(authStore.menus));
const selectedKey = computed(() => route.path);
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

function handleLogout() {
  authStore.logout();
  resetDynamicRoutes();
  router.replace('/login');
}
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
}

.brand-block {
  padding: 24px 20px;
  color: #fff;
  background: linear-gradient(135deg, #0f172a 0%, #165dff 100%);
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.75;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
}

.header-title {
  font-size: 20px;
  font-weight: 700;
}

.header-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.layout-content {
  padding: 24px;
}
</style>
