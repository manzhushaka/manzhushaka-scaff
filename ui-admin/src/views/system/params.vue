<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-input-search
          v-model="keyword"
          allow-clear
          placeholder="搜索参数名称或参数键"
          style="width: 280px"
          @search="handleSearch"
        />
        <a-select
          v-model="statusFilter"
          :options="statusFilterOptions"
          allow-clear
          placeholder="全部状态"
          style="width: 140px"
          @change="handleSearch"
        />
        <a-button type="primary" v-permission="'system:config:add'" @click="openCreate">新增参数</a-button>
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
        <template #configCell="{ record }">
          <div class="primary-cell">
            <div class="primary-cell-title">{{ record.configName }}</div>
            <div class="primary-cell-sub code-text">{{ record.configKey }}</div>
          </div>
        </template>
        <template #statusCell="{ record }">
          <a-tag :color="record.statusValue === 1 ? 'green' : 'red'">{{ record.statusText }}</a-tag>
        </template>
        <template #actionsCell="{ record }">
          <a-space>
            <a-button size="mini" v-permission="'system:config:update'" @click="openEdit(record.id)">编辑</a-button>
            <a-popconfirm content="确认删除该参数吗？" @ok="handleDelete(record.id)">
              <a-button size="mini" status="danger" v-permission="'system:config:delete'">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </div>

    <a-modal v-model:visible="visible" :title="editingId ? '编辑参数' : '新增参数'" @before-ok="submitForm">
      <a-form :model="form" layout="vertical">
        <a-form-item field="configName" label="参数名称">
          <a-input v-model="form.configName" placeholder="请输入参数名称" />
        </a-form-item>
        <a-form-item field="configKey" label="参数键">
          <a-input v-model="form.configKey" placeholder="请输入参数键" />
        </a-form-item>
        <a-form-item field="configValue" label="参数值">
          <a-textarea v-model="form.configValue" :auto-size="{ minRows: 3, maxRows: 5 }" placeholder="请输入参数值" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="form.status" :options="statusOptions" />
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
import type { ConfigForm, ConfigRow } from '@/types/system';
import { mapConfigRow, statusOptions } from './shared';

const loading = ref(false);
const visible = ref(false);
const keyword = ref('');
const statusFilter = ref<number | undefined>();
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);
const editingId = ref<number | null>(null);
const rows = ref<ConfigRow[]>([]);
const columns: TableColumnData[] = [
  {
    dataIndex: 'configName',
    title: '参数',
    slotName: 'configCell',
  },
  {
    dataIndex: 'configValue',
    title: '参数值',
  },
  {
    dataIndex: 'statusText',
    title: '状态',
    width: 110,
    slotName: 'statusCell',
  },
  {
    dataIndex: 'createTimeText',
    title: '创建时间',
    width: 180,
  },
  {
    dataIndex: 'actions',
    title: '操作',
    width: 220,
    slotName: 'actionsCell',
  },
];

const form = reactive<Required<ConfigForm>>({
  configName: '',
  configKey: '',
  configValue: '',
  status: 1,
});

const statusFilterOptions = [
  { label: '全部状态', value: undefined },
  ...statusOptions,
];

const pagination = computed(() => ({
  current: current.value,
  pageSize: pageSize.value,
  total: total.value,
  showTotal: true,
}));

function resetForm() {
  form.configName = '';
  form.configKey = '';
  form.configValue = '';
  form.status = 1;
  editingId.value = null;
}

async function fetchRows() {
  loading.value = true;
  try {
    const response = await systemApi.listConfigs({
      pageNum: current.value,
      pageSize: pageSize.value,
      configName: keyword.value || undefined,
      configKey: keyword.value || undefined,
      status: statusFilter.value,
    });
    rows.value = response.records.map(mapConfigRow);
    total.value = response.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  current.value = 1;
  void fetchRows();
}

function handlePageChange(page: number) {
  current.value = page;
  void fetchRows();
}

function openCreate() {
  resetForm();
  visible.value = true;
}

async function openEdit(id: number) {
  const detail = await systemApi.getConfig(id);
  editingId.value = id;
  form.configName = detail.configName;
  form.configKey = detail.configKey;
  form.configValue = detail.configValue ?? '';
  form.status = detail.status ?? 1;
  visible.value = true;
}

async function submitForm() {
  if (!form.configName.trim() || !form.configKey.trim()) {
    Message.warning('参数名称和参数键不能为空');
    return false;
  }

  const payload: ConfigForm = {
    configName: form.configName.trim(),
    configKey: form.configKey.trim(),
    configValue: form.configValue,
    status: form.status,
  };

  if (editingId.value) {
    await systemApi.updateConfig(editingId.value, payload);
    Message.success('参数已更新');
  } else {
    await systemApi.createConfig(payload);
    Message.success('参数已创建');
  }

  visible.value = false;
  resetForm();
  await fetchRows();
  return true;
}

async function handleDelete(id: number) {
  await systemApi.deleteConfig(id);
  Message.success('参数已删除');
  if (rows.value.length === 1 && current.value > 1) {
    current.value -= 1;
  }
  await fetchRows();
}

void fetchRows();
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
