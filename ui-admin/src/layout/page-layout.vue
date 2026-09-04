<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
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
  :deep(.fade-enter-active),
  :deep(.fade-leave-active) {
    transition:
      opacity 0.22s ease,
      transform 0.22s cubic-bezier(0.23, 1, 0.32, 1);
  }

  :deep(.fade-enter-from) {
    opacity: 0;
    transform: translateY(6px);
  }

  :deep(.fade-leave-to) {
    opacity: 0;
    transform: translateY(-4px);
  }

  @media (prefers-reduced-motion: reduce) {
    :deep(.fade-enter-active),
    :deep(.fade-leave-active) {
      transition-duration: 0.01ms;
    }
  }
</style>
