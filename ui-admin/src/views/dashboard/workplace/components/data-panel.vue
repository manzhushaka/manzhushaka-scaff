<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-grid :cols="24" :row-gap="16" class="panel">
      <a-grid-item
        v-for="item in statistics"
        :key="item.key"
        class="panel-col"
        :span="{ xs: 12, sm: 12, md: 12, lg: 6, xl: 6, xxl: 6 }"
      >
        <a-space>
          <a-avatar :size="44" class="col-avatar">
            <component :is="item.icon" />
          </a-avatar>
          <a-statistic :title="item.label" :value="item.value" show-group-separator />
        </a-space>
      </a-grid-item>
      <a-grid-item :span="24">
        <a-divider class="panel-border" />
      </a-grid-item>
    </a-grid>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { queryDashboardData, DashboardSnapshot } from '@/api/dashboard';
  import { useUserStore } from '@/store';

  const userStore = useUserStore();
  const loading = ref(true);
  const snapshot = ref<DashboardSnapshot>();
  const statistics = computed(() => [
    {
      key: 'users',
      label: '用户总数',
      value: snapshot.value?.users.total || 0,
      icon: 'icon-user-group',
    },
    {
      key: 'roles',
      label: '角色总数',
      value: snapshot.value?.roles.total || 0,
      icon: 'icon-user',
    },
    {
      key: 'departments',
      label: '部门总数',
      value: snapshot.value?.departments.total || 0,
      icon: 'icon-mind-mapping',
    },
    {
      key: 'permissions',
      label: '当前权限',
      value: userStore.permissions.length,
      icon: 'icon-safe',
    },
  ]);

  /** 查询工作台统计数据。 */
  async function loadData() {
    loading.value = true;
    try {
      snapshot.value = await queryDashboardData();
    } finally {
      loading.value = false;
    }
  }

  onMounted(loadData);
</script>

<style lang="less" scoped>
  .arco-grid.panel {
    margin-bottom: 0;
    padding: 16px 20px 0;
  }

  .panel-col {
    padding-left: 24px;
    border-right: 1px solid rgb(var(--gray-2));
  }

  .panel-col:last-of-type {
    border-right: none;
  }

  .col-avatar {
    margin-right: 12px;
    color: rgb(var(--arcoblue-6));
    background-color: rgb(var(--arcoblue-1));
  }

  :deep(.panel-border) {
    margin: 4px 0 0;
  }
</style>
