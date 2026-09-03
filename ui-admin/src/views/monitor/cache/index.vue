<template>
  <div class="monitor-page">
    <div class="page-actions"><a-button type="outline" :loading="loading" @click="loadData"><template #icon><icon-refresh /></template>刷新</a-button></div>
    <a-spin :loading="loading" class="cache-spin">
      <a-grid :cols="24" :col-gap="16" :row-gap="16">
        <a-grid-item :span="{ xs: 24, lg: 8 }"><a-card title="Redis 服务信息" :bordered="false"><a-descriptions :column="1" bordered><a-descriptions-item v-for="item in redisInfo" :key="item.key" :label="item.label">{{ item.value }}</a-descriptions-item></a-descriptions></a-card></a-grid-item>
        <a-grid-item :span="{ xs: 24, lg: 16 }"><a-card title="命令调用统计" :bordered="false"><a-table :data="cache.commandStats || []" :bordered="false" :pagination="false"><template #columns><a-table-column title="命令" data-index="name" /><a-table-column title="调用次数" data-index="value" /><a-table-column title="调用次数占比"><template #cell="{ record }"><a-progress :percent="commandPercent(record)" /></template></a-table-column></template></a-table></a-card></a-grid-item>
      </a-grid>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { getCacheInfo } from '@/api/admin';

  const loading = ref(false);
  const cache = ref<Record<string, any>>({ info: {}, commandStats: [], dbSize: 0 });
  const redisInfo = computed(() => { const info = cache.value.info || {}; return [{ key: 'version', label: 'Redis 版本', value: info.redis_version || '-' }, { key: 'mode', label: '运行模式', value: info.redis_mode || '-' }, { key: 'port', label: '端口', value: info.tcp_port || '-' }, { key: 'clients', label: '连接客户端', value: info.connected_clients || '-' }, { key: 'memory', label: '已用内存', value: info.used_memory_human || '-' }, { key: 'keys', label: '数据库键数', value: cache.value.dbSize ?? '-' }]; });
  const commandPercent = (record: Record<string, any>) => { const total = (cache.value.commandStats || []).reduce((sum: number, item: Record<string, any>) => sum + Number(item.value || 0), 0); return total ? Number(record.value || 0) / total : 0; };
  async function loadData() { loading.value = true; try { const response = await getCacheInfo(); cache.value = response.data || cache.value; } finally { loading.value = false; } }
  onMounted(loadData);
</script>

<style scoped lang="less">
  .monitor-page { min-height: 100%; padding: 20px; background: var(--color-fill-2); }
  .page-actions { display: flex; justify-content: flex-end; margin-bottom: 16px; }
  @media (max-width: 640px) { .monitor-page { padding: 12px; } }
</style>
