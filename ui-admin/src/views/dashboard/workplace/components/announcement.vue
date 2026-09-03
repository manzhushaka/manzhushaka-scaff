<template>
  <a-card
    class="general-card"
    title="系统状态"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ padding: '15px 20px 13px' }"
  >
    <a-list :bordered="false" size="small">
      <a-list-item v-for="item in statusList" :key="item.label">
        <a-list-item-meta :title="item.label" :description="item.value" />
        <template #extra><a-tag :color="item.color">{{ item.status }}</a-tag></template>
      </a-list-item>
    </a-list>
  </a-card>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useAppStore, useUserStore } from '@/store';

  const appStore = useAppStore();
  const userStore = useUserStore();
  const statusList = computed(() => [
    { label: 'Java 服务', value: '已完成身份认证', status: '正常', color: 'green' },
    { label: '当前账号', value: userStore.userName || userStore.name || '-', status: '在线', color: 'blue' },
    { label: '权限集合', value: `${userStore.permissions.length} 项`, status: '已加载', color: 'arcoblue' },
    { label: '动态菜单', value: `${appStore.serverMenu.length} 组`, status: '已加载', color: 'arcoblue' },
  ]);
</script>
