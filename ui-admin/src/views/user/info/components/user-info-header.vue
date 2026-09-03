<template>
  <a-card class="header" :bordered="false">
    <a-space :size="12" direction="vertical" align="center">
      <a-avatar :size="64">
        <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="用户头像" />
        <icon-user />
      </a-avatar>
      <a-typography-title :heading="6" style="margin: 0">
        {{ userInfo.name || '-' }}
      </a-typography-title>
      <a-space class="user-msg" :size="18" wrap justify="center">
        <a-typography-text>
          <icon-user /> {{ userInfo.userName || '-' }}
        </a-typography-text>
        <a-typography-text>
          <icon-home /> {{ userInfo.organizationName || '未分配部门' }}
        </a-typography-text>
        <a-typography-text>
          <icon-safe /> {{ userInfo.roles.length ? userInfo.roles.join('、') : '暂无角色' }}
        </a-typography-text>
      </a-space>
    </a-space>
  </a-card>
</template>

<script lang="ts" setup>
  import { useUserStore } from '@/store';

  const userInfo = useUserStore();
</script>

<style scoped lang="less">
  .header {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 204px;
    background-color: var(--color-bg-2);
  }

  .user-msg {
    color: rgb(var(--gray-8));
  }

  .user-msg :deep(.arco-icon) {
    color: rgb(var(--arcoblue-6));
  }
</style>
