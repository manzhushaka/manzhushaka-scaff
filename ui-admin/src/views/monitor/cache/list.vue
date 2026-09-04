<template>
  <div class="monitor-page">
    <div class="page-actions"><a-button status="danger" :loading="clearing" @click="clearAll"><template #icon><icon-delete /></template>清理全部</a-button></div>
    <a-grid :cols="24" :col-gap="16" :row-gap="16">
      <a-grid-item :span="{ xs: 24, lg: 8 }"><a-card title="缓存名称" :bordered="false"><template #extra><a-button type="text" aria-label="刷新缓存名称" @click="loadNames"><template #icon><icon-refresh /></template></a-button></template><a-table :data="cacheNames" :loading="loading" :bordered="false" :pagination="false" @row-click="selectName"><template #columns><a-table-column title="缓存名称" data-index="cacheName"><template #cell="{ record }">{{ record.cacheName || record.name }}</template></a-table-column><a-table-column title="备注" data-index="remark"><template #cell="{ record }">{{ record.remark || '-' }}</template></a-table-column><a-table-column title="操作" align="center" :width="60"><template #cell="{ record }"><a-button type="text" class="table-action-button table-action-button--delete" aria-label="清理缓存名称" title="清理缓存名称" @click.stop="clearName(record)"><template #icon><icon-delete /></template></a-button></template></a-table-column></template></a-table></a-card></a-grid-item>
      <a-grid-item :span="{ xs: 24, lg: 8 }"><a-card title="缓存键名" :bordered="false"><template #extra><a-button type="text" aria-label="刷新缓存键名" @click="loadKeys"><template #icon><icon-refresh /></template></a-button></template><a-table :data="cacheKeys" :loading="keyLoading" :bordered="false" :pagination="false" row-key="key" @row-click="selectKey"><template #columns><a-table-column title="缓存键名" data-index="key" ellipsis tooltip /><a-table-column title="操作" align="center" :width="60"><template #cell="{ record }"><a-button type="text" class="table-action-button table-action-button--delete" aria-label="清理缓存键" title="清理缓存键" @click.stop="clearKey(record.key)"><template #icon><icon-delete /></template></a-button></template></a-table-column></template></a-table></a-card></a-grid-item>
      <a-grid-item :span="{ xs: 24, lg: 8 }"><a-card title="缓存内容" :bordered="false"><a-descriptions :column="1" bordered><a-descriptions-item label="缓存名称">{{ selectedName || '-' }}</a-descriptions-item><a-descriptions-item label="缓存键名">{{ selectedKey || '-' }}</a-descriptions-item></a-descriptions><a-textarea class="value-area" :model-value="selectedValue" readonly :auto-size="{ minRows: 10, maxRows: 18 }" /></a-card></a-grid-item>
    </a-grid>
  </div>
</template>

<script lang="ts" setup>
  /* eslint-disable no-use-before-define */
  import { onMounted, ref } from 'vue';
  import { Modal, Message } from '@arco-design/web-vue';
  import { clearCacheAll, clearCacheKey, clearCacheName, getCacheValue, listCacheKeys, listCacheNames } from '@/api/admin';

  const loading = ref(true); const keyLoading = ref(false); const clearing = ref(false);
  const cacheNames = ref<Record<string, any>[]>([]); const cacheKeys = ref<Record<string, string>[]>([]); const selectedName = ref(''); const selectedKey = ref(''); const selectedValue = ref('');
  async function loadNames() { loading.value = true; try { const response = await listCacheNames(); cacheNames.value = response.data || []; } finally { loading.value = false; } }
  function nameOf(record: Record<string, any>) { return record.cacheName || record.name || ''; }
  async function selectName(record: Record<string, any>) { selectedName.value = nameOf(record); selectedKey.value = ''; selectedValue.value = ''; await loadKeys(); }
  async function loadKeys() { if (!selectedName.value) return; keyLoading.value = true; try { const response = await listCacheKeys(selectedName.value); cacheKeys.value = (response.data || []).map((key: string) => ({ key })); } finally { keyLoading.value = false; } }
  async function selectKey(record: Record<string, string>) { selectedKey.value = record.key || ''; if (!selectedName.value || !selectedKey.value) return; const response = await getCacheValue(selectedName.value, selectedKey.value); selectedValue.value = response.data?.cacheValue || ''; }
  function clearName(record: Record<string, any>) { const name = nameOf(record); Modal.confirm({ title: '请确认操作', content: `确认清理缓存名称“${name}”下的全部数据吗？`, onOk: async () => { await clearCacheName(name); Message.success('清理成功'); await loadNames(); if (selectedName.value === name) { selectedName.value = ''; cacheKeys.value = []; } } }); }
  function clearKey(key: string) { Modal.confirm({ title: '请确认操作', content: `确认清理缓存键“${key}”吗？`, onOk: async () => { await clearCacheKey(key); Message.success('清理成功'); await loadKeys(); if (selectedKey.value === key) { selectedKey.value = ''; selectedValue.value = ''; } } }); }
  function clearAll() { Modal.confirm({ title: '请确认操作', content: '确认清理全部缓存吗？该操作可能影响正在运行的业务。', onOk: async () => { clearing.value = true; try { await clearCacheAll(); Message.success('全部缓存已清理'); await loadNames(); cacheKeys.value = []; selectedName.value = ''; selectedKey.value = ''; selectedValue.value = ''; } finally { clearing.value = false; } } }); }
  onMounted(loadNames);
</script>

<style scoped lang="less">
  .monitor-page { min-height: 100%; padding: 20px; background: var(--color-fill-2); }
  .page-actions { display: flex; justify-content: flex-end; margin-bottom: 16px; }
  .value-area { margin-top: 16px; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
  @media (max-width: 640px) { .monitor-page { padding: 12px; } }
</style>
