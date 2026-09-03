<template>
  <a-card
    class="general-card"
    title="最近访问"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ padding: '20px' }"
  >
    <a-space v-if="links.length" direction="vertical" fill :size="4">
      <a-button
        v-for="link in links"
        :key="link.name"
        type="text"
        long
        class="visited-link"
        @click="navigate(link.name)"
      >
        <template #icon><component :is="link.icon" /></template>
        {{ link.label }}
      </a-button>
    </a-space>
    <a-empty v-else description="暂无可访问页面" />
  </a-card>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const allLinks = [
    { name: 'ServerLogCenter', label: '统一日志', icon: 'icon-file' },
    { name: 'ServerRuntimeLog', label: '运行日志', icon: 'icon-file' },
    { name: 'ServerSlowSql', label: '慢 SQL 日志', icon: 'icon-bar-chart' },
  ];
  const links = computed(() => allLinks.filter((link) => router.hasRoute(link.name)));

  /** 跳转到最近访问的管理页面。 */
  function navigate(name: string) {
    if (router.hasRoute(name)) router.push({ name });
  }
</script>

<style lang="less" scoped>
  .visited-link {
    justify-content: flex-start;
    color: rgb(var(--gray-8));
  }

  .visited-link:hover {
    color: rgb(var(--arcoblue-6));
    background-color: rgb(var(--arcoblue-1));
  }
</style>
