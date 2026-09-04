<template>
  <a-card :loading="loading" class="general-card" title="权限范围">
    <a-row :gutter="16">
      <a-col
        v-for="item in resourceCards"
        :key="item.key"
        :xs="12"
        :sm="12"
        :md="12"
        :lg="12"
        :xl="8"
        :xxl="8"
        class="resource-item"
      >
        <div class="resource-tile">
          <a-statistic
            :title="item.label"
            :value="item.value"
            show-group-separator
          >
            <template #prefix><component :is="item.icon" /></template>
          </a-statistic>
          <a-typography-text type="secondary">{{
            item.description
          }}</a-typography-text>
        </div>
      </a-col>
    </a-row>
    <a-empty v-if="!resourceCards.length" description="暂无权限范围数据" />
  </a-card>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { queryAdminSnapshot, AdminSnapshot } from '@/api/analytics';

  const loading = ref(true);
  const snapshot = ref<AdminSnapshot>();
  const resourceCards = computed(() => {
    if (!snapshot.value) return [];
    return [
      {
        key: 'users',
        label: '可见用户',
        value: snapshot.value.users.total,
        description: 'Java 用户目录',
        icon: 'icon-user',
      },
      {
        key: 'roles',
        label: '可见角色',
        value: snapshot.value.roles.total,
        description: 'Java 角色目录',
        icon: 'icon-user-group',
      },
      {
        key: 'departments',
        label: '可见部门',
        value: snapshot.value.departments.total,
        description: 'Java 部门目录',
        icon: 'icon-mind-mapping',
      },
      {
        key: 'menus',
        label: '可见菜单',
        value: snapshot.value.menus.total,
        description: '动态路由菜单',
        icon: 'icon-menu',
      },
      {
        key: 'operationLogs',
        label: '操作记录',
        value: snapshot.value.operationLogs.total,
        description: '审计日志记录',
        icon: 'icon-file',
      },
      {
        key: 'loginLogs',
        label: '登录记录',
        value: snapshot.value.loginLogs.total,
        description: '登录审计记录',
        icon: 'icon-safe',
      },
    ];
  });

  /** 加载当前账号可见的后台资源统计。 */
  async function loadData() {
    loading.value = true;
    try {
      snapshot.value = await queryAdminSnapshot();
    } finally {
      loading.value = false;
    }
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .resource-item {
    margin-bottom: 16px;
    height: 100%;
  }

  .resource-tile {
    height: 100%;
    padding: 16px;
    border: 1px solid var(--color-border-2);
    border-radius: 4px;
    background-color: var(--color-bg-2);
  }

  .resource-tile :deep(.arco-statistic-title) {
    white-space: nowrap;
  }

  .resource-tile :deep(.arco-statistic-value) {
    line-height: 1.4;
  }

  .resource-tile :deep(.arco-typography) {
    display: block;
    margin-top: 8px;
  }
</style>
