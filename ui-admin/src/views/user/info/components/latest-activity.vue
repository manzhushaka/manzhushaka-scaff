<template>
  <a-card class="general-card" title="最新操作">
    <a-list v-if="activities.length" :bordered="false">
      <a-list-item v-for="activity in activities" :key="activity.operId">
        <a-list-item-meta
          :title="activity.title || '系统操作'"
          :description="`${activity.requestMethod || '-'} ${activity.operUrl || ''}`"
        >
          <template #avatar><a-avatar><icon-file /></a-avatar></template>
        </a-list-item-meta>
        <template #extra><a-typography-text type="secondary">{{ activity.operTime || '-' }}</a-typography-text></template>
      </a-list-item>
    </a-list>
    <a-empty v-else description="暂无操作记录" />
  </a-card>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { queryAdminSnapshot } from '@/api/analytics';

  const activities = ref<Record<string, any>[]>([]);

  /** 加载 Java 操作日志。 */
  async function loadData() {
    const snapshot = await queryAdminSnapshot();
    activities.value = snapshot.operationLogs.rows.slice(0, 7);
  }

  onMounted(loadData);
</script>
