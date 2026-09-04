<template>
  <div class="monitor-page">
    <a-spin :loading="loading" class="monitor-spin">
      <a-alert v-if="error" type="error" show-icon>服务器监控数据暂时无法获取，请确认后端服务状态。</a-alert>
      <template v-else>
        <a-grid :cols="24" :col-gap="16" :row-gap="16">
          <a-grid-item v-for="item in usageCards" :key="item.label" :span="{ xs: 24, sm: 12, lg: 6 }">
            <a-card :bordered="false" class="metric-card">
              <a-statistic :title="item.label" :value="item.value" :precision="2" :suffix="item.suffix" />
              <a-progress :percent="Math.min(Number(item.percent) / 100, 1)" :status="Number(item.percent) > 85 ? 'danger' : 'normal'" />
              <a-typography-text type="secondary">{{ item.detail }}</a-typography-text>
            </a-card>
          </a-grid-item>
        </a-grid>
        <a-grid :cols="24" :col-gap="16" :row-gap="16" class="detail-grid">
          <a-grid-item :span="{ xs: 24, lg: 12 }"><a-card title="系统信息" :bordered="false"><a-descriptions :column="1" bordered><a-descriptions-item label="计算机名称">{{ server.sys?.computerName || '-' }}</a-descriptions-item><a-descriptions-item label="计算机 IP">{{ server.sys?.computerIp || '-' }}</a-descriptions-item><a-descriptions-item label="操作系统">{{ server.sys?.osName || '-' }}</a-descriptions-item><a-descriptions-item label="系统架构">{{ server.sys?.osArch || '-' }}</a-descriptions-item><a-descriptions-item label="项目路径">{{ server.sys?.userDir || '-' }}</a-descriptions-item></a-descriptions></a-card></a-grid-item>
          <a-grid-item :span="{ xs: 24, lg: 12 }"><a-card title="JVM 信息" :bordered="false"><a-descriptions :column="1" bordered><a-descriptions-item label="Java 名称">{{ server.jvm?.name || '-' }}</a-descriptions-item><a-descriptions-item label="Java 版本">{{ server.jvm?.version || '-' }}</a-descriptions-item><a-descriptions-item label="Java 路径">{{ server.jvm?.home || '-' }}</a-descriptions-item><a-descriptions-item label="启动时间">{{ server.jvm?.startTime || '-' }}</a-descriptions-item><a-descriptions-item label="运行时间">{{ server.jvm?.runTime || '-' }}</a-descriptions-item></a-descriptions></a-card></a-grid-item>
          <a-grid-item :span="24"><a-card title="磁盘使用情况" :bordered="false"><a-table :data="server.sysFiles || []" :bordered="false" :pagination="false"><template #columns><a-table-column title="盘符路径" data-index="dirName" /><a-table-column title="文件系统" data-index="sysTypeName" /><a-table-column title="类型" data-index="typeName" /><a-table-column title="总容量" data-index="total" /><a-table-column title="已用" data-index="used" /><a-table-column title="剩余" data-index="free" /><a-table-column title="使用率"><template #cell="{ record }"><a-progress :percent="Number(record.usage) / 100" :status="Number(record.usage) > 85 ? 'danger' : 'normal'" /></template></a-table-column></template></a-table></a-card></a-grid-item>
        </a-grid>
      </template>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { getServerInfo } from '@/api/admin';

  const loading = ref(true);
  const error = ref(false);
  const server = ref<Record<string, any>>({});
  const usageCards = computed(() => [
    { label: 'CPU 使用率', value: server.value.cpu?.total || 0, percent: server.value.cpu?.total || 0, suffix: '%', detail: `${server.value.cpu?.cpuNum || 0} 个逻辑处理器` },
    { label: '内存使用率', value: server.value.mem?.usage || 0, percent: server.value.mem?.usage || 0, suffix: '%', detail: `${server.value.mem?.used || 0} GB / ${server.value.mem?.total || 0} GB` },
    { label: 'JVM 使用率', value: server.value.jvm?.usage || 0, percent: server.value.jvm?.usage || 0, suffix: '%', detail: `${server.value.jvm?.used || 0} MB / ${server.value.jvm?.total || 0} MB` },
    { label: '磁盘数量', value: server.value.sysFiles?.length || 0, percent: 0, suffix: ' 个', detail: '已发现的文件系统' },
  ]);
  async function loadData() { loading.value = true; error.value = false; try { const response = await getServerInfo(); server.value = response.data || {}; } catch { error.value = true; } finally { loading.value = false; } }
  onMounted(loadData);
</script>

<style scoped lang="less">
  .monitor-page { min-height: 100%; padding: 12px 20px 20px; background: var(--color-fill-2); }
  .monitor-spin { display: block; }
  .metric-card, :deep(.arco-card) { border-radius: 6px; }
  .metric-card :deep(.arco-progress) { margin: 16px 0 8px; }
  .detail-grid { margin-top: 16px; }
  @media (max-width: 640px) { .monitor-page { padding: 12px; } }
</style>
