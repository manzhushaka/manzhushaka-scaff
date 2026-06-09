<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
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
        <a-table-column data-index="module" title="模块">
          <template #cell="{ record }">
            <div class="primary-cell">
              <div class="primary-cell-title">{{ record.module }}</div>
              <div class="primary-cell-sub">{{ record.action }}</div>
            </div>
          </template>
        </a-table-column>
        <a-table-column data-index="operatorName" title="操作人" />
        <a-table-column data-index="successText" title="结果" :width="110">
          <template #cell="{ record }">
            <a-tag :color="record.successValue === true ? 'green' : record.successValue === false ? 'red' : 'gray'">
              {{ record.successText }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column data-index="costMsText" title="耗时" :width="110" />
        <a-table-column data-index="requestMethod" title="请求方法" :width="110" />
        <a-table-column data-index="requestUri" title="请求路径">
          <template #cell="{ record }">
            <span class="code-text">{{ record.requestUri }}</span>
          </template>
        </a-table-column>
        <a-table-column data-index="errorMsg" title="错误信息" />
        <a-table-column data-index="createTimeText" title="时间" :width="180" />
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
.system-page {
  display: grid;
  gap: 18px;
}

.primary-cell-title {
  color: #17233c;
  font-weight: 700;
}

.primary-cell-sub {
  margin-top: 4px;
  color: #74839a;
  font-size: 12px;
}
</style>
