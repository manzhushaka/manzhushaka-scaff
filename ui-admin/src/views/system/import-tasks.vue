<template>
  <div class="system-page">
    <PageHeaderCard
      title="导入任务管理"
      description="统一管理异步导入任务。上传文件后会先进入异步校验流程，源文件和结果回执都会落到 BOS，并支持复制下载链接。"
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
          placeholder="全部导入场景"
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
        <a-button v-permission="'system:io:import:query'" @click="fetchRows">刷新</a-button>
        <a-button type="primary" v-permission="'system:io:import:create'" @click="openCreate">新建导入任务</a-button>
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
        <template #sourceCell="{ record }">
          <span class="code-text">{{ record.sourceFileName }}</span>
        </template>
        <template #resultCell="{ record }">
          <span class="code-text">{{ record.resultFileName }}</span>
        </template>
        <template #actionsCell="{ record }">
          <a-space>
            <a-button
              size="mini"
              v-permission="'system:io:import:download'"
              :disabled="record.sourceFileName === '--'"
              @click="copyDownloadUrl(record.id, 'SOURCE')"
            >
              复制源文件链接
            </a-button>
            <a-button
              size="mini"
              v-permission="'system:io:import:download'"
              :disabled="record.resultFileName === '--'"
              @click="copyDownloadUrl(record.id, 'RESULT')"
            >
              复制结果链接
            </a-button>
          </a-space>
        </template>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" title="新建导入任务" @before-ok="submitCreate">
      <a-form :model="form" layout="vertical">
        <a-form-item field="bizType" label="导入场景">
          <a-select v-model="form.bizType" :options="sceneOptions" placeholder="请选择导入场景" />
        </a-form-item>
        <a-form-item field="taskName" label="任务名称">
          <a-input v-model="form.taskName" placeholder="可选，不填则使用默认任务名称" />
        </a-form-item>
        <a-form-item field="file" label="导入文件">
          <input type="file" accept=".csv,text/csv" @change="handleFileChange" />
          <div class="upload-hint">{{ selectedFileName }}</div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { Message, type TableColumnData } from '@arco-design/web-vue';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import { copyTextToClipboard } from '@/utils/clipboard';
import type { ImportExportTaskRow, SelectOption } from '@/types/system';
import { importExportTaskStatusOptions, mapImportExportTaskRow } from './import-export-support';

const loading = ref(false);
const visible = ref(false);
const keyword = ref('');
const sceneFilter = ref<string | undefined>();
const statusFilter = ref<string | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const rows = ref<ImportExportTaskRow[]>([]);
const sceneOptions = ref<SelectOption[]>([]);
const selectedFile = ref<File | null>(null);
const form = reactive({
  bizType: '',
  taskName: '',
});

const selectedFileName = computed(() => selectedFile.value?.name ?? '请选择一个 CSV 文件');
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
  { dataIndex: 'sourceFileName', title: '源文件', slotName: 'sourceCell', width: 220 },
  { dataIndex: 'resultFileName', title: '结果文件', slotName: 'resultCell', width: 220 },
  { dataIndex: 'countSummary', title: '总数 / 成功 / 失败', width: 160 },
  { dataIndex: 'taskMessage', title: '说明' },
  { dataIndex: 'createBy', title: '创建人', width: 100 },
  { dataIndex: 'createTimeText', title: '创建时间', width: 180 },
  { dataIndex: 'finishedTimeText', title: '完成时间', width: 180 },
  { dataIndex: 'actions', title: '操作', width: 240, slotName: 'actionsCell' },
];

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

async function fetchSceneOptions() {
  sceneOptions.value = await systemApi.listImportExportSceneOptions('IMPORT');
  if (!form.bizType && sceneOptions.value.length > 0) {
    form.bizType = String(sceneOptions.value[0].value);
  }
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listImportExportTasks({
      pageNum: current.value,
      pageSize: pageSize.value,
      taskType: 'IMPORT',
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

function openCreate() {
  form.taskName = '';
  selectedFile.value = null;
  if (!form.bizType && sceneOptions.value.length > 0) {
    form.bizType = String(sceneOptions.value[0].value);
  }
  visible.value = true;
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement;
  selectedFile.value = target.files?.[0] ?? null;
}

async function submitCreate() {
  if (!form.bizType) {
    Message.warning('请选择导入场景');
    return false;
  }
  if (!selectedFile.value) {
    Message.warning('请选择导入文件');
    return false;
  }
  await systemApi.createImportTask({
    bizType: form.bizType,
    taskName: form.taskName?.trim() || undefined,
    file: selectedFile.value,
  });
  Message.success('导入任务已创建');
  visible.value = false;
  selectedFile.value = null;
  fetchRows();
  return true;
}

async function copyDownloadUrl(id: number, fileRole: 'SOURCE' | 'RESULT') {
  const response = await systemApi.getImportExportDownloadUrl(id, fileRole);
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

.upload-hint {
  margin-top: 8px;
  color: rgb(var(--gray-6));
  font-size: 12px;
}
</style>
