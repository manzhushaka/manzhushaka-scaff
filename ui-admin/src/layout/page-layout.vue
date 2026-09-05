<template>
  <router-view v-slot="{ Component, route }">
    <transition name="page-fade" appear>
      <component
        :is="Component"
        v-if="route.meta.ignoreCache"
        :key="route.fullPath"
      />
      <keep-alive v-else :include="cacheList">
        <component :is="Component" :key="route.fullPath" />
      </keep-alive>
    </transition>
  </router-view>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useTabBarStore } from '@/store';

  const tabBarStore = useTabBarStore();

  const cacheList = computed(() => tabBarStore.getCacheList);
</script>

<style scoped lang="less">
  :global(.page-fade-enter-active),
  :global(.page-fade-leave-active) {
    will-change: opacity, transform;
    transition:
      opacity var(--ui-motion-standard) ease,
      transform var(--ui-motion-standard) var(--ui-motion-ease);
  }

  :global(.page-fade-enter-from) {
    opacity: 0;
    transform: translateY(6px);
  }

  :global(.page-fade-leave-to) {
    opacity: 0;
    transform: translateY(-4px);
  }

  @media (prefers-reduced-motion: reduce) {
    :global(.page-fade-enter-active),
    :global(.page-fade-leave-active) {
      transition-duration: 0.01ms;
    }
  }
</style>
