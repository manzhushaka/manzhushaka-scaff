<template>
  <div class="log-page">
    <PageHeaderCard :title="title" :description="description">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="按操作内容或备注搜索"
          style="width: 240px"
          @search="fetchRows"
        />
        <a-button v-permission="permission" @click="fetchRows">刷新</a-button>
      </a-space>
    </PageHeaderCard>

    <div class="page-card table-card">
      <a-table :data="rows" :loading="loading" :pagination="false" row-key="id">
        <a-table-column data-index="name" title="日志内容" />
        <a-table-column data-index="operator" title="操作人" />
        <a-table-column data-index="ip" title="IP" />
        <a-table-column data-index="remark" title="说明" />
        <a-table-column data-index="updatedAt" title="时间" />
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { getEntityList } from '@/api/system';
import type { LogRecord } from '@/types/system';
import PageHeaderCard from './PageHeaderCard.vue';

const props = defineProps<{
  title: string;
  description: string;
  entityKey: string;
  permission: string;
}>();

const loading = ref(false);
const keyword = ref('');
const rows = ref<LogRecord[]>([]);

async function fetchRows() {
  loading.value = true;
  try {
    rows.value = (await getEntityList(props.entityKey, keyword.value)) as LogRecord[];
  } finally {
    loading.value = false;
  }
}

fetchRows();
</script>

<style scoped>
.log-page {
  display: grid;
  gap: 18px;
}
</style>
