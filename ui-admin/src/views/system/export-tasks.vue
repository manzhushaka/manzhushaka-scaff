<template>
  <div class="system-page">
    <PageHeaderCard
      title="导出任务管理"
      description="统一查看各业务模块提交的异步导出任务。任务完成后可直接复制结果文件下载链接。"
    />

    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索任务名称"
          style="width: 240px"
          @search="handleSearch"
        />
        <a-select
          v-model="sceneFilter"
          :options="sceneOptions"
          allow-clear
          placeholder="全部导出场景"
          style="width: 220px"
          @change="handleSearch"
        />
        <a-select
          v-model="statusFilter"
          :options="importExportTaskStatusOptions"
          allow-clear
          placeholder="全部状态"
          style="width: 140px"
          @change="handleSearch"
        />
        <a-button v-permission="'system:io:export:query'" @click="fetchRows">刷新</a-button>
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
        <template #taskCell="{ record }">
          <div class="primary-cell">
            <div class="primary-cell-title">{{ record.taskName }}</div>
            <div class="primary-cell-sub">{{ record.bizLabel }}</div>
          </div>
        </template>
        <template #statusCell="{ record }">
          <a-tag :color="statusColorMap[record.taskStatusValue] ?? 'gray'">{{ record.taskStatusText }}</a-tag>
        </template>
        <template #fileCell="{ record }">
          <span class="code-text">{{ record.resultFileName }}</span>
        </template>
        <template #actionsCell="{ record }">
          <a-button
            size="mini"
            v-permission="'system:io:export:download'"
            :disabled="record.resultFileName === '--'"
            @click="copyDownloadUrl(record.id)"
          >
            复制下载链接
          </a-button>
        </template>
      </a-table>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { Message, type TableColumnData } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import { copyTextToClipboard } from '@/utils/clipboard';
import type { ImportExportTaskRow, SelectOption } from '@/types/system';
import { importExportTaskStatusOptions, mapImportExportTaskRow } from './import-export-support';

const loading = ref(false);
const keyword = ref('');
const sceneFilter = ref<string | undefined>();
const statusFilter = ref<string | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const rows = ref<ImportExportTaskRow[]>([]);
const sceneOptions = ref<SelectOption[]>([]);

const statusColorMap: Record<string, string> = {
  PENDING: 'gold',
  PROCESSING: 'arcoblue',
  SUCCESS: 'green',
  FAIL: 'red',
};

const columns: TableColumnData[] = [
  { dataIndex: 'taskName', title: '任务', slotName: 'taskCell', width: 240 },
  { dataIndex: 'taskNo', title: '任务编号', width: 220 },
  { dataIndex: 'taskStatusText', title: '状态', width: 120, slotName: 'statusCell' },
  { dataIndex: 'resultFileName', title: '结果文件', slotName: 'fileCell', width: 220 },
  { dataIndex: 'countSummary', title: '总数 / 成功 / 失败', width: 160 },
  { dataIndex: 'taskMessage', title: '说明' },
  { dataIndex: 'createBy', title: '创建人', width: 100 },
  { dataIndex: 'createTimeText', title: '创建时间', width: 180 },
  { dataIndex: 'finishedTimeText', title: '完成时间', width: 180 },
  { dataIndex: 'actions', title: '操作', width: 140, slotName: 'actionsCell' },
];

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

async function fetchSceneOptions() {
  sceneOptions.value = await systemApi.listImportExportSceneOptions('EXPORT');
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listImportExportTasks({
      pageNum: current.value,
      pageSize: pageSize.value,
      taskType: 'EXPORT',
      bizType: sceneFilter.value,
      taskName: keyword.value || undefined,
      taskStatus: statusFilter.value,
    });
    rows.value = response.records.map(mapImportExportTaskRow);
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

async function copyDownloadUrl(id: number) {
  const response = await systemApi.getImportExportDownloadUrl(id, 'RESULT');
  await copyTextToClipboard(response.url);
  Message.success('下载链接已复制，请粘贴到浏览器打开');
}

Promise.all([fetchSceneOptions(), fetchRows()]);
</script>

<style scoped>
.system-page {
  display: grid;
  gap: 18px;
}
</style>
