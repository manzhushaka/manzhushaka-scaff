<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: 0 }"
      :body-style="{ padding: '17px 20px 21px' }"
      title="近期操作"
    >
      <template #extra>
        <a-link @click="loadData">刷新</a-link>
      </template>
      <a-table
        :data="logs"
        :pagination="false"
        :bordered="false"
        :scroll="{ x: '100%', y: '264px' }"
      >
        <template #columns>
          <a-table-column title="操作时间" data-index="operTime" :width="170" />
          <a-table-column title="系统模块" data-index="title" :width="140" ellipsis tooltip />
          <a-table-column title="操作人员" data-index="operName" :width="120" ellipsis tooltip />
          <a-table-column title="请求方式" data-index="requestMethod" :width="100" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">
              <a-tag :class="['status-tag', record.status === 0 || record.status === '0' ? 'status-tag--success' : 'status-tag--danger']">
                {{ record.status === 0 || record.status === '0' ? '正常' : '异常' }}
              </a-tag>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-empty v-if="!logs.length" description="暂无操作日志" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { queryDashboardData } from '@/api/dashboard';

  const { loading, setLoading } = useLoading(true);
  const logs = ref<Record<string, any>[]>([]);

  /** 加载最近的后台操作。 */
  async function loadData() {
    setLoading(true);
    try {
      const snapshot = await queryDashboardData();
      logs.value = snapshot.operationLogs.rows.slice(0, 10);
    } finally {
      setLoading(false);
    }
  }

  onMounted(loadData);
</script>

<style scoped lang="less">
  .general-card {
    min-height: 395px;
  }
</style>
