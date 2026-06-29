<template>
  <div class="navbar" :class="'nav' + settingsStore.navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb v-if="settingsStore.navType == 1" id="breadcrumb-container" class="breadcrumb-container" />
    <top-nav v-if="settingsStore.navType == 2" id="topmenu-container" class="topmenu-container" />
    <template v-if="settingsStore.navType == 3">
      <logo v-show="settingsStore.sidebarLogo" :collapse="false"></logo>
      <top-bar id="topbar-container" class="topbar-container" />
    </template>

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
        <header-search id="header-search" class="right-menu-item" />

        <el-tooltip content="源码地址" effect="dark" placement="bottom">
          <ruo-yi-git id="manzhushaka-git" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip content="文档地址" effect="dark" placement="bottom">
          <ruo-yi-doc id="manzhushaka-doc" class="right-menu-item hover-effect" />
        </el-tooltip>

        <screenfull id="screenfull" class="right-menu-item hover-effect" />

        <!-- 暗黑模式兼容入口不作为品牌主题主入口展示 -->

        <el-tooltip content="布局大小" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>

        <el-tooltip content="消息通知" effect="dark" placement="bottom">
          <header-notice id="header-notice" class="right-menu-item hover-effect" />
        </el-tooltip>
      </template>

      <el-dropdown @command="handleCommand" class="avatar-container right-menu-item hover-effect" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="userStore.avatar" class="user-avatar" />
          <span class="user-nickname"> {{ userStore.nickName }} </span>
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
import TopNav from './TopNav'
import TopBar from './TopBar'
import Logo from './Sidebar/Logo'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import HeaderSearch from '@/components/HeaderSearch'
import RuoYiGit from '@/components/RuoYi/Git'
import RuoYiDoc from '@/components/RuoYi/Doc'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import HeaderNotice from './HeaderNotice'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const lockStore = useLockStore()
const settingsStore = useSettingsStore()

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
      location.href = '/index'
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

async function toggleTheme(event) {
  const x = event?.clientX || window.innerWidth / 2
  const y = event?.clientY || window.innerHeight / 2
  const wasDark = settingsStore.isDark

  const isReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches
  const isSupported = document.startViewTransition && !isReducedMotion

  if (!isSupported) {
    settingsStore.toggleTheme()
    return
  }

  try {
    const transition = document.startViewTransition(async () => {
      await new Promise((resolve) => setTimeout(resolve, 10))
      settingsStore.toggleTheme()
      await nextTick()
    })
    await transition.ready

    const endRadius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))
    const clipPath = [`circle(0px at ${x}px ${y}px)`, `circle(${endRadius}px at ${x}px ${y}px)`]
    document.documentElement.animate(
      {
        clipPath: !wasDark ? [...clipPath].reverse() : clipPath
      }, {
        duration: 650,
        easing: "cubic-bezier(0.4, 0, 0.2, 1)",
        fill: "forwards",
        pseudoElement: !wasDark ? "::view-transition-old(root)" : "::view-transition-new(root)"
      }
    )
    await transition.finished
  } catch (error) {
    console.warn("View transition failed, falling back to immediate toggle:", error)
    settingsStore.toggleTheme()
  }
}
</script>

<style lang='scss' scoped>
.navbar.nav3 {
  .hamburger-container {
    display: none !important;
  }
}

.navbar {
  height: 60px;
  overflow: hidden;
  position: relative;
  background: var(--ui-bg-topbar);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--ui-border);
  display: flex;
  align-items: center;
  box-sizing: border-box;
  box-shadow: 0 1px 0 var(--ui-divider);

  .hamburger-container {
    line-height: 56px;
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
    flex-shrink: 0;
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
    line-height: 60px;
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
      border-radius: 6px;
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
