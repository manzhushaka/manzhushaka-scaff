<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb v-if="showBreadcrumb" id="breadcrumb-container" class="breadcrumb-container" />

    <div class="right-menu">
      <a-tooltip content="切换橙色/紫色主题" position="bottom">
        <a-button
          type="text"
          shape="circle"
          class="right-menu-item hover-effect tool-button"
          aria-label="切换橙色/紫色主题"
          @click="toggleUiTheme"
        >
          <template #icon><icon-palette /></template>
        </a-button>
      </a-tooltip>
      <a-tooltip :content="isPageFullscreen ? '退出全屏' : '全屏显示'" position="bottom">
        <a-button
          type="text"
          shape="circle"
          class="right-menu-item hover-effect tool-button fullscreen-button"
          :aria-label="isPageFullscreen ? '退出全屏' : '全屏显示'"
          @click="toggleFullscreen"
        >
          <template #icon><icon-fullscreen-exit v-if="isPageFullscreen" /><icon-fullscreen v-else /></template>
        </a-button>
      </a-tooltip>
      <a-tooltip v-if="settingsStore.showSettings" content="界面设置" position="bottom">
        <a-button
          type="text"
          shape="circle"
          class="right-menu-item hover-effect tool-button"
          aria-label="界面设置"
          @click="setLayout"
        >
          <template #icon><icon-settings /></template>
        </a-button>
      </a-tooltip>
      <a-dropdown trigger="hover" position="br" @select="handleCommand">
        <button type="button" class="avatar-container right-menu-item" aria-label="账号菜单">
          <span class="avatar-wrapper">
            <img :src="userStore.avatar" class="user-avatar" alt="" />
            <span class="user-nickname">{{ userStore.nickName }}</span>
            <icon-down class="avatar-caret" />
          </span>
        </button>
        <template #content>
          <a-doption value="profile"><template #icon><icon-user /></template>个人中心</a-doption>
          <a-doption v-if="settingsStore.showSettings" value="setLayout">
            <template #icon><icon-settings /></template>界面设置
          </a-doption>
          <a-doption value="lockScreen"><template #icon><icon-lock /></template>锁定屏幕</a-doption>
          <a-divider :margin="4" />
          <a-doption value="logout" class="logout-option">
            <template #icon><icon-export /></template>退出登录
          </a-doption>
        </template>
      </a-dropdown>
    </div>
  </div>
</template>

<script setup>
import {
  IconDown,
  IconExport,
  IconFullscreen,
  IconFullscreenExit,
  IconLock,
  IconPalette,
  IconSettings,
  IconUser
} from '@arco-design/web-vue/es/icon'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const lockStore = useLockStore()
const settingsStore = useSettingsStore()
const { proxy } = getCurrentInstance()
const isPageFullscreen = ref(false)
const showBreadcrumb = computed(() => route.path !== '/' && route.path !== '/index')

function syncFullscreenState() {
  isPageFullscreen.value = Boolean(document.fullscreenElement)
}

async function toggleFullscreen() {
  if (document.fullscreenElement) {
    await document.exitFullscreen()
  } else {
    await document.documentElement.requestFullscreen()
  }
}

onMounted(() => document.addEventListener('fullscreenchange', syncFullscreenState))
onBeforeUnmount(() => document.removeEventListener('fullscreenchange', syncFullscreenState))

function toggleSideBar() {
  appStore.toggleSideBar()
}

function toggleUiTheme() {
  settingsStore.setUiTheme(settingsStore.uiTheme === 'arco-purple' ? 'arco-orange' : 'arco-purple')
}

function handleCommand(command) {
  switch (command) {
    case "profile":
      router.push('/user/profile')
      break
    case "setLayout":
      setLayout()
      break
    case "lockScreen":
      lockScreen()
      break
    case "logout":
      logout()
      break
    default:
      break
  }
}

function logout() {
  proxy.$modal.confirm('确定注销并退出系统吗？').then(() => {
    userStore.logOut().then(() => {
      router.push('/')
    })
  }).catch(() => { })
}

const emits = defineEmits(['setLayout'])
function setLayout() {
  emits('setLayout')
}

function lockScreen() {
  const currentPath = route.fullPath
  lockStore.lockScreen(currentPath)
  router.push('/lock')
}
</script>

<style lang='scss' scoped>
.navbar {
  height: var(--ui-layout-topbar-height, 52px);
  overflow: hidden;
  position: relative;
  background: var(--ui-bg-topbar);
  border-bottom: 1px solid color-mix(in srgb, var(--ui-border) 74%, var(--ui-bg-topbar));
  display: flex;
  align-items: center;
  box-sizing: border-box;
  box-shadow: none;

  .hamburger-container {
    line-height: var(--ui-layout-topbar-height, 52px);
    height: 100%;
    cursor: pointer;
    transition: background var(--ui-transition-fast);
    -webkit-tap-highlight-color: transparent;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    margin: 0 10px 0 8px;
    border-radius: 6px;

    &:hover {
      background: var(--ui-bg-hover);
    }
  }

  .breadcrumb-container {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    height: 100%;
    overflow: hidden;
    margin-right: 16px;

    :deep(.arco-breadcrumb-item) {
      .arco-breadcrumb-item-link {
        font-size: 13px;
        line-height: 20px;
      }
    }
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    overflow: hidden;
    margin-left: 8px;
  }

  .right-menu {
    height: 100%;
    line-height: var(--ui-layout-topbar-height, 52px);
    display: flex;
    align-items: center;
    margin-left: auto;
    gap: 6px;
    padding-right: 16px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      padding: 0;
      font-size: 18px;
      color: var(--ui-text-secondary, #5a5e66);
      border-radius: 8px;
      border: 0;
      background: transparent;
      transition: background var(--ui-transition-fast), color var(--ui-transition-fast);

      &.hover-effect {
        cursor: pointer;

        &:hover {
          background: var(--ui-bg-hover);
          color: var(--ui-primary);
        }
      }

      &.theme-switch-wrapper {
        display: flex;
        align-items: center;
        justify-content: center;

        svg {
          transition: transform 0.3s;
          
          &:hover {
            transform: scale(1.15);
          }
        }
      }
    }

    .fullscreen-button {
      flex: 0 0 36px;
      line-height: 1;
      color: var(--ui-text-secondary);

      &:hover {
        color: var(--ui-primary);
        background: var(--ui-bg-hover);
      }
    }

    .tool-button {
      border: 1px solid var(--ui-border-subtle);
      background: var(--ui-bg-panel-soft);

      &:hover {
        border-color: color-mix(in srgb, var(--ui-primary) 28%, var(--ui-border));
      }
    }

    .avatar-container {
      width: auto;
      height: auto;
      padding: 0 4px;
      gap: 0;
      display: flex;
      align-items: center;

      &:hover {
        background: transparent;
      }

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 6px;
        position: relative;
        min-height: 36px;
        padding: 0 6px 0 4px;
        border: 1px solid transparent;
        border-radius: var(--ui-radius-control);
        transition: background var(--ui-transition-fast), border-color var(--ui-transition-fast);

        &:hover {
          background: var(--ui-bg-hover);
          border-color: var(--ui-border);
        }

        .user-avatar {
          cursor: pointer;
          width: 32px;
          height: 32px;
          border-radius: 50%;
          flex-shrink: 0;
          border: 2px solid var(--ui-bg-panel);
          box-shadow: 0 0 0 1px var(--ui-border);
        }

        .avatar-caret {
          width: 12px;
          height: 12px;
          color: var(--ui-text-muted);
        }

        .user-nickname{
          font-size: 13px;
          font-weight: 500;
          color: var(--ui-text-primary);
          line-height: 20px;
        }

        i {
          cursor: pointer;
          font-size: 12px;
        }
      }
    }
  }
}

@media screen and (max-width: 640px) {
  .navbar {
    .hamburger-container {
      margin-right: 4px;
    }

    .breadcrumb-container {
      margin-right: 6px;
    }

    .right-menu {
      gap: 4px;
      padding-right: 8px;

      .right-menu-item {
        width: 32px;
        height: 32px;
      }

      .avatar-container .avatar-wrapper {
        padding: 0;
        border: 0;

        .user-nickname,
        .avatar-caret {
          display: none;
        }
      }
    }
  }
}

:global(.logout-option) {
  color: var(--ui-danger);
}
</style>
