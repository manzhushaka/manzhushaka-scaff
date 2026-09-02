<template>
  <a-card
    class="general-card"
    title="管理入口"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ paddingTop: 0 }"
  >
    <a-grid :cols="2" :row-gap="8">
      <a-grid-item v-for="link in links" :key="link.name">
        <a-link v-if="router.hasRoute(link.name)" @click="navigate(link.name)">
          <template #icon><component :is="link.icon" /></template>
          {{ link.label }}
        </a-link>
        <a-typography-text v-else type="secondary">{{ link.label }}</a-typography-text>
      </a-grid-item>
    </a-grid>
  </a-card>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const links = [
    { name: 'ServerUser', label: '用户管理', icon: 'icon-user' },
    { name: 'ServerRole', label: '角色管理', icon: 'icon-user-group' },
    { name: 'ServerMenu', label: '菜单管理', icon: 'icon-menu' },
    { name: 'AccountProfile', label: '个人资料', icon: 'icon-file' },
  ];

  /** 跳转到入口页面。 */
  function navigate(name: string) {
    router.push({ name });
  }
</script>

<style lang="less" scoped>
  .arco-card-body .arco-link,
  .arco-card-body .arco-typography {
    margin: 10px 0;
    color: rgb(var(--gray-8));
  }
</style>
