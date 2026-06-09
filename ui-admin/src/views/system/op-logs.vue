<template>
  <div>
    <PageHeaderCard title="操作日志" description="操作日志按真实后端分页查询处理，保持只读语义。">
      <a-space>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索模块、动作或操作人"
          style="width: 280px"
          @search="handleSearch"
        />
        <a-select
          v-model="successFilter"
          :options="opSuccessOptions"
          allow-clear
          placeholder="全部结果"
          style="width: 140px"
          @change="handleSearch"
        />
        <a-button v-permission="'system:op-log:query'" @click="fetchRows">刷新</a-button>
      </a-space>
    </PageHeaderCard>

    <div class="page-card table-card">
      <a-table
        :data="rows"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        @page-change="handlePageChange"
      >
        <a-table-column data-index="module" title="模块" />
        <a-table-column data-index="action" title="动作" />
        <a-table-column data-index="operatorName" title="操作人" />
        <a-table-column data-index="successText" title="结果" />
        <a-table-column data-index="costMsText" title="耗时" />
        <a-table-column data-index="requestMethod" title="请求方法" />
        <a-table-column data-index="requestUri" title="请求路径" />
        <a-table-column data-index="errorMsg" title="错误信息" />
        <a-table-column data-index="createTimeText" title="时间" />
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { OpLogRow } from '@/types/system';
import { mapOpLogRow, opSuccessOptions } from './shared';

const loading = ref(false);
const keyword = ref('');
const successFilter = ref<string | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const rows = ref<OpLogRow[]>([]);

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listOpLogs({
      pageNum: current.value,
      pageSize: pageSize.value,
      module: keyword.value || undefined,
      action: keyword.value || undefined,
      operatorName: keyword.value || undefined,
      success: successFilter.value === undefined ? undefined : successFilter.value === 'true',
    });
    rows.value = response.records.map(mapOpLogRow);
    total.value = response.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  current.value = 1;
  fetchRows();
}

function handlePageChange(page: number) {
  current.value = page;
  fetchRows();
}

fetchRows();
</script>

<style scoped>
.table-card {
  padding: 16px;
}
</style>

