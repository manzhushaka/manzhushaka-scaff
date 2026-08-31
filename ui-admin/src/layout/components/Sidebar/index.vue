<template>
  <div :class="['sidebar-theme-wrapper', { 'has-logo': showLogo, 'is-collapsed': isCollapse }]" class="sidebar-container">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <div class="sidebar-scrollbar">
      <a-menu
        :selected-keys="[activeMenu]"
        :collapsed="isCollapse"
        :collapsed-width="64"
        :auto-open-selected="true"
        :auto-scroll-into-view="true"
        :level-indent="18"
        theme="light"
        mode="vertical"
        accordion
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path + index"
          :item="route"
          :base-path="route.path"
          :show-submenu-arrow="true"
        />
      </a-menu>
    </div>
  </div>
</template>

<script setup>
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters)
const showLogo = computed(() => settingsStore.sidebarLogo)
const isCollapse = computed(() => !appStore.sidebar.opened)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

</script>

<style lang="scss" scoped>
.sidebar-container {
  background-color: var(--ui-bg-sidebar);
  box-shadow: var(--ui-shadow-sidebar);
  border-right: 1px solid var(--ui-sidebar-border);

  .sidebar-scrollbar {
    height: calc(100% - var(--ui-layout-topbar-height));
    overflow-x: hidden;
    overflow-y: auto;
    background-color: var(--ui-bg-sidebar);
    scrollbar-width: thin;
    scrollbar-color: var(--ui-border-strong) transparent;
  }

  :deep(.arco-menu) {
    border: none;
    min-height: 100%;
    width: 100% !important;
    background-color: transparent !important;

    .arco-menu-item,
    .arco-menu-inline-header {
      min-height: 42px;
      line-height: 42px;
      border-radius: 4px;
      margin: 2px 8px;
      width: calc(100% - 16px);
      color: var(--ui-sidebar-text);
      transition: color var(--ui-transition-fast), background-color var(--ui-transition-fast);

      &:hover {
        background-color: var(--ui-sidebar-item-hover-bg) !important;
        color: var(--ui-sidebar-text-hover) !important;
      }
    }

    .arco-menu-item.arco-menu-selected {
      position: relative;
      color: var(--ui-sidebar-text-active) !important;
      background-color: var(--ui-sidebar-item-active-bg) !important;

      &::before {
        content: '';
        position: absolute;
        top: 7px;
        bottom: 7px;
        left: 0;
        width: 3px;
        border-radius: 0 2px 2px 0;
        background: var(--ui-sidebar-item-active-border);
      }
    }

    .arco-menu-icon,
    .arco-menu-inline-header-suffix {
      color: inherit;
    }

    .arco-menu-inline-content {
      .arco-menu-item,
      .arco-menu-inline-header {
        padding-left: 18px !important;
      }
    }
  }
}
</style>
