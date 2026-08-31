<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <span class="sidebar-logo-mark">
          <img v-if="logo" :src="logo" class="sidebar-logo" alt="" />
        </span>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <span class="sidebar-logo-mark">
          <img v-if="logo" :src="logo" class="sidebar-logo" alt="" />
        </span>
        <span class="sidebar-brand-copy">
          <strong class="sidebar-title">{{ brandName }}</strong>
          <span class="sidebar-subtitle">{{ brandSubtitle }}</span>
        </span>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.png'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const appTitle = import.meta.env.VITE_APP_TITLE || 'manzhushaka'
const brandName = appTitle.split(/\s+-\s+/)[0]
const brandSubtitle = `${brandName} Console`
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: var(--ui-layout-topbar-height, 52px);
  background: var(--ui-bg-sidebar);
  overflow: hidden;
  border-bottom: 1px solid var(--ui-sidebar-border);

  & .sidebar-logo-link {
    display: flex !important;
    align-items: center;
    width: 100%;
    height: 100%;
    padding: 0 18px;
    gap: 12px;

    .sidebar-logo-mark {
      display: grid;
      flex: 0 0 40px;
      width: 40px;
      height: 40px;
      overflow: hidden;
      place-items: center;
      border: 1px solid var(--ui-border-subtle);
      border-radius: 8px;
      background: var(--ui-bg-panel);
    }

    & .sidebar-logo {
      width: 34px;
      height: 34px;
      object-fit: cover;
    }

    .sidebar-brand-copy {
      display: flex;
      min-width: 0;
      flex-direction: column;
      justify-content: center;
    }

    & .sidebar-title {
      overflow: hidden;
      color: var(--ui-text-primary);
      font-weight: 700;
      line-height: 22px;
      font-size: 17px;
      font-family: var(--ui-font-family);
      letter-spacing: 0;
      text-overflow: ellipsis;
      white-space: nowrap;
      text-transform: capitalize;
    }

    .sidebar-subtitle {
      overflow: hidden;
      color: var(--ui-text-muted);
      font-size: 11px;
      line-height: 17px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &.collapse {
    .sidebar-logo-link {
      justify-content: center;
      padding: 0;
    }
  }
}
</style>
