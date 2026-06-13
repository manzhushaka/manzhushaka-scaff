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
        <a-button v-permission="'system:log:view'" @click="fetchRows">刷新</a-button>
      </a-space>
    </PageHeaderCard>

    <div class="page-card table-card">
      <a-table
        :data="rows"
        :loading="loading"
        row-key="id"
        :pagination="pagination"
        :columns="columns"
        @page-change="handlePageChange"
      >
        <template #moduleCell="{ record }">
          <div class="primary-cell">
            <div class="primary-cell-title">{{ record.module }}</div>
            <div class="primary-cell-sub">{{ record.action }}</div>
          </div>
        </template>
        <template #successCell="{ record }">
          <a-tag :color="record.successValue === true ? 'green' : record.successValue === false ? 'red' : 'gray'">
            {{ record.successText }}
          </a-tag>
        </template>
        <template #requestUriCell="{ record }">
          <span class="code-text">{{ record.requestUri }}</span>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { TableColumnData } from '@arco-design/web-vue';
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
const columns: TableColumnData[] = [
  {
    dataIndex: 'module',
    title: '模块',
    slotName: 'moduleCell',
  },
  {
    dataIndex: 'operatorName',
    title: '操作人',
  },
  {
    dataIndex: 'successText',
    title: '结果',
    width: 110,
    slotName: 'successCell',
  },
  {
    dataIndex: 'costMsText',
    title: '耗时',
    width: 110,
  },
  {
    dataIndex: 'requestMethod',
    title: '请求方法',
    width: 110,
  },
  {
    dataIndex: 'requestUri',
    title: '请求路径',
    slotName: 'requestUriCell',
  },
  {
    dataIndex: 'errorMsg',
    title: '错误信息',
  },
  {
    dataIndex: 'createTimeText',
    title: '时间',
    width: 180,
  },
];

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
