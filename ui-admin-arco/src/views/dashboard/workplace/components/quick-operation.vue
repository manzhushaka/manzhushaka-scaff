<template>
  <a-card
    class="general-card"
    title="快捷操作"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ padding: '20px' }"
  >
    <a-grid v-if="links.length" :cols="2" :row-gap="8">
      <a-grid-item v-for="link in links" :key="link.name">
        <a-button type="text" class="operation" @click="navigate(link.name)">
          <template #icon><component :is="link.icon" /></template>
          {{ link.label }}
        </a-button>
      </a-grid-item>
    </a-grid>
    <a-empty v-else description="暂无可用操作" />
  </a-card>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const allLinks = [
    { name: 'ServerUser', label: '用户管理', icon: 'icon-user' },
    { name: 'ServerRole', label: '角色管理', icon: 'icon-user-group' },
    { name: 'ServerOnline', label: '在线用户', icon: 'icon-computer' },
    { name: 'ServerJob', label: '定时任务', icon: 'icon-calendar-clock' },
    { name: 'ServerCache', label: '缓存监控', icon: 'icon-storage' },
  ];
  const links = computed(() =>
    allLinks.filter((link) => router.hasRoute(link.name))
  );

  /** 跳转到动态菜单中的业务页面。 */
  function navigate(name: string) {
    if (router.hasRoute(name)) router.push({ name });
  }
</script>

<style lang="less" scoped>
  .operation {
    width: 100%;
    height: 44px;
    justify-content: flex-start;
    color: rgb(var(--gray-8));
    text-align: left;
    white-space: nowrap;
  }

  .operation:hover {
    color: rgb(var(--arcoblue-6));
    background-color: rgb(var(--arcoblue-1));
  }
</style>
