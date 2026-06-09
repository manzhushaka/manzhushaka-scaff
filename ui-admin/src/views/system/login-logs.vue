<template>
  <div>
    <PageHeaderCard title="登录日志" description="日志页按真实后端分页查询处理，只提供筛选和列表查看。">
      <a-space>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索登录账号"
          style="width: 240px"
          @search="handleSearch"
        />
        <a-select
          v-model="statusFilter"
          :options="loginStatusOptions"
          allow-clear
          placeholder="全部结果"
          style="width: 140px"
          @change="handleSearch"
        />
        <a-button v-permission="'system:login-log:query'" @click="fetchRows">刷新</a-button>
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
        <a-table-column data-index="username" title="登录账号" />
        <a-table-column data-index="loginStatus" title="登录结果" />
        <a-table-column data-index="ip" title="来源 IP" />
        <a-table-column data-index="message" title="结果说明" />
        <a-table-column data-index="userAgent" title="User Agent" />
        <a-table-column data-index="createTimeText" title="时间" />
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { LoginLogRow } from '@/types/system';
import { loginStatusOptions, mapLoginLogRow } from './shared';

const loading = ref(false);
const keyword = ref('');
const statusFilter = ref<string | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const rows = ref<LoginLogRow[]>([]);

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listLoginLogs({
      pageNum: current.value,
      pageSize: pageSize.value,
      username: keyword.value || undefined,
      loginStatus: statusFilter.value,
    });
    rows.value = response.records.map(mapLoginLogRow);
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

