<template>
  <a-card class="general-card" title="登录安全记录">
    <a-list v-if="records.length" :bordered="false">
      <a-list-item v-for="record in records" :key="record.infoId">
        <a-list-item-meta :title="record.userName || '未知账号'" :description="record.msg || '登录记录'">
          <template #avatar><a-avatar><icon-safe /></a-avatar></template>
        </a-list-item-meta>
        <template #extra><a-tag :class="['status-tag', record.status === '0' ? 'status-tag--success' : 'status-tag--danger']">{{ record.status === '0' ? '成功' : '失败' }}</a-tag></template>
      </a-list-item>
    </a-list>
    <a-empty v-else description="暂无异常登录记录" />
  </a-card>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { queryAdminSnapshot } from '@/api/analytics';

  const records = ref<Record<string, any>[]>([]);

  /** 加载 Java 登录审计记录。 */
  async function loadData() {
    const snapshot = await queryAdminSnapshot();
    records.value = snapshot.loginLogs.rows.filter((record) => record.status !== '0' && record.status !== 0).slice(0, 5);
  }

  onMounted(loadData);
</script>
