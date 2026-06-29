<template>
  <div :class="['sidebar-theme-wrapper', {'has-logo':showLogo}]" class="sidebar-container">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="var(--ui-bg-sidebar)"
        text-color="var(--ui-sidebar-text)"
        :unique-opened="true"
        active-text-color="var(--ui-sidebar-text-active)"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
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

  .scrollbar-wrapper {
    background-color: var(--ui-bg-sidebar);
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100% !important;
    background-color: transparent !important;

    .el-menu-item, .el-sub-menu__title {
      border-radius: 6px;
      margin: 3px 6px;
      width: calc(100% - 12px);

      &:hover {
        background-color: var(--ui-sidebar-item-hover-bg) !important;
        color: var(--ui-sidebar-text-hover) !important;
      }
    }

    .el-menu-item :deep(.el-menu-tooltip__trigger) {
      border-radius: inherit;
      color: inherit;
    }

    .el-menu-item {
      height: 44px !important;
      line-height: 44px !important;

      &.is-active {
        color: var(--ui-sidebar-text-active) !important;
        background-color: var(--ui-sidebar-item-active-bg) !important;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 20px;
          border-radius: 999px;
          background: var(--ui-sidebar-item-active-border);
          pointer-events: none;
          z-index: 2;
        }
      }
    }

    .el-sub-menu__title {
      height: 44px !important;
      line-height: 44px !important;
    }
  }
}
</style>
