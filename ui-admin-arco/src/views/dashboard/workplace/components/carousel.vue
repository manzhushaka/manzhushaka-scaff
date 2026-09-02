<template>
  <a-card class="general-card route-card" title="动态菜单">
    <a-carousel
      v-if="routes.length"
      indicator-type="slider"
      show-arrow="hover"
      auto-play
    >
      <a-carousel-item v-for="route in routes" :key="route.name">
        <a-button type="text" class="route-slide" @click="navigate(route.name)">
          <template #icon><component :is="route.icon" /></template>
          <span>{{ route.title }}</span>
        </a-button>
      </a-carousel-item>
    </a-carousel>
    <a-empty v-else description="暂无动态菜单" />
  </a-card>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { useAppStore } from '@/store';

  const appStore = useAppStore();
  const router = useRouter();
  const routes = computed(() => {
    const result: { name: string; title: string; icon: string }[] = [];
    const visit = (items: any[]) => {
      items.forEach((item) => {
        if (item.children?.length) visit(item.children);
        else if (item.name && !item.meta?.hideInMenu) {
          result.push({
            name: item.name,
            title: item.meta?.title || item.name,
            icon: item.meta?.icon || 'icon-menu',
          });
        }
      });
    };
    visit(appStore.serverMenu);
    return result.slice(0, 8);
  });

  /** 跳转到动态菜单叶子页面。 */
  function navigate(name: string) {
    router.push({ name });
  }
</script>

<style lang="less" scoped>
  .route-card {
    min-height: 170px;
  }

  .route-card :deep(.arco-carousel) {
    height: 100px;
  }

  .route-slide {
    width: 100%;
    height: 100px;
    color: rgb(var(--arcoblue-6));
    font-size: 18px;
  }

  .route-slide :deep(.arco-btn-content) {
    margin-left: 8px;
  }
</style>
