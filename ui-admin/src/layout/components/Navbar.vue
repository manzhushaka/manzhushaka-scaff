<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb v-if="showBreadcrumb" id="breadcrumb-container" class="breadcrumb-container" />

    <div class="right-menu">
      <el-tooltip :content="isPageFullscreen ? '退出全屏' : '全屏显示'" placement="bottom">
        <button type="button" class="right-menu-item hover-effect fullscreen-button" @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
        </button>
      </el-tooltip>
      <el-dropdown @command="handleCommand" class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar" />
          <span class="user-nickname"> {{ userStore.nickName }} </span>
          <el-icon class="avatar-caret"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/user/profile">
              <el-dropdown-item>个人中心</el-dropdown-item>
            </router-link>
            <el-dropdown-item command="setLayout" v-if="settingsStore.showSettings">
                <span>布局设置</span>
            </el-dropdown-item>
            <el-dropdown-item command="lockScreen">
                <span>锁定屏幕</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
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

function handleCommand(command) {
  switch (command) {
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
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
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
    margin-right: 8px;

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

    :deep(.el-breadcrumb__item) {
      .el-breadcrumb__inner {
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
    padding-right: 8px;

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
</style>
